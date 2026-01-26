use once_cell::sync::Lazy;
use serde::{Deserialize, Serialize};
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_int, c_void};
use std::sync::{mpsc, Mutex};
use std::time::{Duration, Instant};
use tungstenite::stream::MaybeTlsStream;
use chacha20poly1305::aead::{Aead, Payload};
use chacha20poly1305::{KeyInit, XChaCha20Poly1305, XNonce};
use rand::RngCore;
use sha2::{Digest, Sha256};
use base64::Engine;
use automerge::{transaction::Transactable, AutoCommit, ObjType, ReadDoc, ROOT};
use std::collections::HashMap;
use std::collections::VecDeque;
use std::fs;
use std::path::PathBuf;

/// Must match `MemCloud.Relay.Protocol.CurrentVersion`
const RELAY_PROTOCOL_VERSION: u32 = 1;

/// Current sync-core ABI version (increment when breaking C ABI).
const SYNC_CORE_ABI_VERSION: u32 = 1;

static GREETING: &[u8] = b"MemCloud Sync Core (Rust)\0";

type EventCallback = extern "C" fn(event_json_utf8: *const c_char, user_data: *mut c_void);

static EVENT_SINK: Lazy<Mutex<Option<(EventCallback, usize)>>> = Lazy::new(|| Mutex::new(None));
static EVENT_QUEUE: Lazy<Mutex<VecDeque<String>>> = Lazy::new(|| Mutex::new(VecDeque::new()));

#[derive(Clone)]
struct RelayHandle {
    tx: mpsc::Sender<Outbound>,
}

static RELAY_HANDLE: Lazy<Mutex<Option<RelayHandle>>> = Lazy::new(|| Mutex::new(None));

// MVP: one active vault key (we'll expand to per-vault keys when we add real multi-vault state in core).
static ACTIVE_VAULT_KEY: Lazy<Mutex<Option<[u8; 32]>>> = Lazy::new(|| Mutex::new(None));

static DOC_SNAPSHOTS: Lazy<Mutex<HashMap<String, Vec<u8>>>> = Lazy::new(|| Mutex::new(HashMap::new()));

static DOC_LAST_BROADCAST_HEADS: Lazy<Mutex<HashMap<String, Vec<automerge::ChangeHash>>>> =
    Lazy::new(|| Mutex::new(HashMap::new()));

static STORAGE_DIR: Lazy<Mutex<Option<String>>> = Lazy::new(|| Mutex::new(None));

#[no_mangle]
pub extern "C" fn memcloud_sc_protocol_version() -> u32 {
    RELAY_PROTOCOL_VERSION
}

#[no_mangle]
pub extern "C" fn memcloud_sc_abi_version() -> u32 {
    SYNC_CORE_ABI_VERSION
}

/// Returns a pointer to a null-terminated UTF-8 string. Static storage; do not free.
#[no_mangle]
pub extern "C" fn memcloud_sc_greeting_utf8() -> *const c_char {
    GREETING.as_ptr() as *const c_char
}

#[derive(Debug, Serialize)]
#[serde(tag = "type")]
enum SyncCoreEvent<'a> {
    #[serde(rename = "RelayStatusChanged")]
    RelayStatusChanged {
        v: u32,
        status: &'a str,
        detail: Option<&'a str>,
    },
    #[serde(rename = "RelayFrame")]
    RelayFrame {
        v: u32,
        frameType: &'a str,
        vaultId: Option<&'a str>,
        code: Option<&'a str>,
        message: Option<&'a str>,
    },
    #[serde(rename = "RelayMsgReceived")]
    RelayMsgReceived {
        v: u32,
        vaultId: &'a str,
        fromDeviceId: &'a str,
        docId: &'a str,
        nonce: &'a str,
        ciphertext: &'a str,
    },
}

fn emit_event(event: SyncCoreEvent<'_>) {
    if let Ok(json) = serde_json::to_string(&event) {
        if let Ok(mut q) = EVENT_QUEUE.lock() {
            q.push_back(json.clone());
        }

        if let Ok(guard) = EVENT_SINK.lock() {
            if let Some((cb, user_data)) = *guard {
                if let Ok(cstr) = CString::new(json) {
                    cb(cstr.as_ptr(), user_data as *mut c_void);
                }
            }
        }
    }
}

fn emit_json(json: serde_json::Value) {
    if let Ok(s) = serde_json::to_string(&json) {
        if let Ok(mut q) = EVENT_QUEUE.lock() {
            q.push_back(s.clone());
        }

        if let Ok(guard) = EVENT_SINK.lock() {
            if let Some((cb, user_data)) = *guard {
                if let Ok(cstr) = CString::new(s) {
                    cb(cstr.as_ptr(), user_data as *mut c_void);
                }
            }
        }
    }
}

/// Registers a push-event callback. Pass null to clear.
#[no_mangle]
pub extern "C" fn memcloud_sc_set_event_callback(cb: Option<EventCallback>, user_data: *mut c_void) {
    let mut guard = EVENT_SINK.lock().expect("event sink lock poisoned");
    *guard = cb.map(|f| (f, user_data as usize));
}

/// Polls a queued event JSON string.
///
/// Returns a heap-allocated null-terminated UTF-8 string pointer, or null if none.
/// Caller must free using `memcloud_sc_free_utf8`.
#[no_mangle]
pub extern "C" fn memcloud_sc_poll_event_json_utf8() -> *mut c_char {
    let Ok(mut q) = EVENT_QUEUE.lock() else {
        return std::ptr::null_mut();
    };
    let Some(s) = q.pop_front() else {
        return std::ptr::null_mut();
    };
    CString::new(s).map(|c| c.into_raw()).unwrap_or(std::ptr::null_mut())
}

/// Frees a UTF-8 string allocated by sync-core (e.g. `memcloud_sc_poll_event_json_utf8`).
#[no_mangle]
pub extern "C" fn memcloud_sc_free_utf8(ptr: *mut c_char) {
    if ptr.is_null() {
        return;
    }
    unsafe {
        let _ = CString::from_raw(ptr);
    }
}

/// Sets the active vault symmetric key (UTF-8).
///
/// MVP KDF: SHA-256(key_utf8) -> 32 bytes.
/// We'll replace this with a proper KDF + salt during vault creation/pairing.
#[no_mangle]
pub extern "C" fn memcloud_sc_set_active_vault_key(vault_key_utf8: *const c_char) -> c_int {
    let key_str = match cstr_arg(vault_key_utf8) {
        Ok(v) => v,
        Err(_) => return 2,
    };

    let mut hasher = Sha256::new();
    hasher.update(key_str.as_bytes());
    let digest = hasher.finalize();

    let mut key = [0u8; 32];
    key.copy_from_slice(&digest[..32]);

    let mut guard = ACTIVE_VAULT_KEY.lock().expect("vault key lock poisoned");
    *guard = Some(key);
    0
}

/// Sets the directory where sync-core may persist local state (UTF-8 path).
///
/// If not set, sync-core operates in-memory only.
#[no_mangle]
pub extern "C" fn memcloud_sc_set_storage_dir(storage_dir_utf8: *const c_char) -> c_int {
    let dir = match cstr_arg(storage_dir_utf8) {
        Ok(v) => v,
        Err(_) => return 2,
    };

    let mut g = STORAGE_DIR.lock().expect("storage dir lock poisoned");
    *g = Some(dir);
    0
}

fn get_active_key() -> Option<[u8; 32]> {
    ACTIVE_VAULT_KEY.lock().ok().and_then(|g| *g)
}

fn make_doc_key(vault_id: &str, doc_id: &str) -> String {
    format!("{}::{}", vault_id, doc_id)
}

fn storage_dir_path() -> Option<PathBuf> {
    let s = STORAGE_DIR.lock().ok().and_then(|g| g.clone())?;
    Some(PathBuf::from(s))
}

fn hex_encode(bytes: &[u8]) -> String {
    const HEX: &[u8; 16] = b"0123456789abcdef";
    let mut out = String::with_capacity(bytes.len() * 2);
    for &b in bytes {
        out.push(HEX[(b >> 4) as usize] as char);
        out.push(HEX[(b & 0x0f) as usize] as char);
    }
    out
}

fn doc_file_name(doc_key: &str) -> String {
    let mut h = Sha256::new();
    h.update(doc_key.as_bytes());
    let digest = h.finalize();
    format!("doc_{}.am", hex_encode(&digest[..]))
}

fn load_doc_bytes_from_disk(doc_key: &str) -> Option<Vec<u8>> {
    let dir = storage_dir_path()?;
    let path = dir.join(doc_file_name(doc_key));
    fs::read(path).ok()
}

fn save_doc_bytes_to_disk(doc_key: &str, bytes: &[u8]) {
    let Some(dir) = storage_dir_path() else { return; };
    let _ = fs::create_dir_all(&dir);
    let path = dir.join(doc_file_name(doc_key));
    let _ = fs::write(path, bytes);
}

fn encrypt_for_relay(vault_id: &str, from_device_id: &str, doc_id: &str, plaintext: &[u8]) -> Option<(String, String)> {
    let key = get_active_key()?;
    let cipher = XChaCha20Poly1305::new((&key).into());

    let mut nonce_bytes = [0u8; 24];
    rand::thread_rng().fill_bytes(&mut nonce_bytes);
    let nonce = XNonce::from_slice(&nonce_bytes);

    let aad = format!("v1|{}|{}|{}", vault_id, from_device_id, doc_id);
    let ct = cipher
        .encrypt(nonce, Payload { msg: plaintext, aad: aad.as_bytes() })
        .ok()?;

    let nonce_b64 = base64::engine::general_purpose::STANDARD.encode(nonce_bytes);
    let ct_b64 = base64::engine::general_purpose::STANDARD.encode(ct);
    Some((nonce_b64, ct_b64))
}

fn decrypt_from_relay(
    vault_id: &str,
    from_device_id: &str,
    doc_id: &str,
    nonce_b64: &str,
    ct_b64: &str,
) -> Option<Vec<u8>> {
    let key = get_active_key()?;
    let cipher = XChaCha20Poly1305::new((&key).into());

    let nonce_bytes = base64::engine::general_purpose::STANDARD.decode(nonce_b64).ok()?;
    if nonce_bytes.len() != 24 {
        return None;
    }
    let mut nb = [0u8; 24];
    nb.copy_from_slice(&nonce_bytes);
    let nonce = XNonce::from_slice(&nb);

    let ct = base64::engine::general_purpose::STANDARD.decode(ct_b64).ok()?;
    let aad = format!("v1|{}|{}|{}", vault_id, from_device_id, doc_id);
    cipher
        .decrypt(nonce, Payload { msg: ct.as_slice(), aad: aad.as_bytes() })
        .ok()
}

fn doc_apply_snapshot(doc_key: &str, incoming: &[u8]) -> Option<String> {
    let incoming_doc = AutoCommit::load(incoming).ok()?;

    let mut guard = DOC_SNAPSHOTS.lock().ok()?;
    let merged_bytes = if let Some(existing) = guard.get(doc_key) {
        let mut local = AutoCommit::load(existing).ok()?;
        let mut remote = incoming_doc;
        local.merge(&mut remote).ok()?;
        local.save()
    } else {
        incoming.to_vec()
    };

    // Store merged snapshot
    guard.insert(doc_key.to_string(), merged_bytes.clone());
    save_doc_bytes_to_disk(doc_key, &merged_bytes);

    // Extract content (if present)
    let merged_doc = AutoCommit::load(&merged_bytes).ok()?;
    let content = match merged_doc.get(ROOT, "content").ok().flatten() {
        Some((automerge::Value::Object(ObjType::Text), obj)) => merged_doc.text(&obj).ok()?,
        Some((automerge::Value::Scalar(s), _)) => s.to_string(),
        _ => String::new(),
    };
    Some(content)
}

fn doc_set_content(doc: &mut AutoCommit, content: &str) {
    // Ensure we have a text object at root.content; if not, create it.
    match doc.get(ROOT, "content").ok().flatten() {
        Some((automerge::Value::Object(ObjType::Text), obj)) => {
            let existing = doc.text(&obj).unwrap_or_default();
            // Replace whole text for MVP (later we'll do incremental edits from UI).
            let _ = doc.splice_text(&obj, 0, existing.len() as isize, content);
        }
        _ => {
            if let Ok(text_obj) = doc.put_object(ROOT, "content", ObjType::Text) {
                let _ = doc.splice_text(&text_obj, 0, 0, content);
            } else {
                let _ = doc.put(ROOT, "content", content);
            }
        }
    }
}

#[derive(Serialize, Deserialize)]
struct AutomergeChangesEnvelope {
    t: String, // "am_changes_v1"
    docId: String,
    changesB64: Vec<String>,
}

fn encode_changes_envelope(doc_id: &str, changes: Vec<automerge::Change>) -> Option<Vec<u8>> {
    let changes_b64 = changes
        .into_iter()
        .map(|c| base64::engine::general_purpose::STANDARD.encode(c.raw_bytes()))
        .collect::<Vec<_>>();

    let env = AutomergeChangesEnvelope {
        t: "am_changes_v1".to_string(),
        docId: doc_id.to_string(),
        changesB64: changes_b64,
    };

    serde_json::to_vec(&env).ok()
}

fn try_apply_changes_envelope(doc_key: &str, doc_id: &str, plaintext: &[u8]) -> Option<String> {
    let env: AutomergeChangesEnvelope = serde_json::from_slice(plaintext).ok()?;
    if env.t != "am_changes_v1" || env.docId != doc_id {
        return None;
    }

    let changes = env
        .changesB64
        .into_iter()
        .filter_map(|b64| base64::engine::general_purpose::STANDARD.decode(b64).ok())
        .filter_map(|bytes| automerge::Change::from_bytes(bytes).ok())
        .collect::<Vec<_>>();

    // Apply into a persistent doc snapshot store (reuse DOC_SNAPSHOTS as serialized doc bytes for now).
    let existing_bytes = DOC_SNAPSHOTS
        .lock()
        .ok()
        .and_then(|g| g.get(doc_key).cloned())
        .or_else(|| load_doc_bytes_from_disk(doc_key));

    let mut doc = if let Some(existing) = existing_bytes {
        AutoCommit::load(&existing).ok()?
    } else {
        AutoCommit::new()
    };

    // Apply changes
    doc.apply_changes(changes).ok()?;

    // Save updated doc bytes back
    let bytes = doc.save();
    if let Ok(mut g) = DOC_SNAPSHOTS.lock() {
        g.insert(doc_key.to_string(), bytes.clone());
    }
    save_doc_bytes_to_disk(doc_key, &bytes);

    // Return content projection
    let content = match doc.get(ROOT, "content").ok().flatten() {
        Some((automerge::Value::Object(ObjType::Text), obj)) => doc.text(&obj).ok()?,
        Some((automerge::Value::Scalar(s), _)) => s.to_string(),
        _ => String::new(),
    };

    Some(content)
}

#[derive(Debug, Serialize)]
#[serde(tag = "type")]
enum ClientFrame<'a> {
    #[serde(rename = "join")]
    Join {
        #[serde(rename = "v")]
        v: u32,
        #[serde(rename = "vaultId")]
        vault_id: &'a str,
        #[serde(rename = "deviceId")]
        device_id: &'a str,
        #[serde(rename = "joinToken")]
        join_token: &'a str,
        #[serde(rename = "register")]
        register: bool,
    },
    #[serde(rename = "msg")]
    Msg {
        #[serde(rename = "v")]
        v: u32,
        #[serde(rename = "vaultId")]
        vault_id: &'a str,
        #[serde(rename = "fromDeviceId")]
        from_device_id: &'a str,
        #[serde(rename = "docId")]
        doc_id: &'a str,
        #[serde(rename = "nonce")]
        nonce: &'a str,
        #[serde(rename = "ciphertext")]
        ciphertext: &'a str,
    },
}

#[derive(Debug, Deserialize)]
struct ServerFrame {
    v: u32,
    #[serde(rename = "type")]
    frame_type: String,
    code: Option<String>,
    message: Option<String>,
    #[serde(rename = "vaultId")]
    vault_id: Option<String>,
}

#[derive(Debug, Deserialize)]
#[serde(tag = "type")]
enum InboundFrame {
    #[serde(rename = "msg")]
    Msg(MsgFrame),
    // Fallback for "joined"/"pong"/"error"
    #[serde(other)]
    Other,
}

#[derive(Debug, Deserialize)]
struct MsgFrame {
    v: u32,
    #[serde(rename = "vaultId")]
    vault_id: String,
    #[serde(rename = "fromDeviceId")]
    from_device_id: String,
    #[serde(rename = "docId")]
    doc_id: String,
    nonce: String,
    ciphertext: String,
}

#[derive(Debug)]
enum Outbound {
    SendMsg {
        vault_id: String,
        from_device_id: String,
        doc_id: String,
        nonce: String,
        ciphertext: String,
    },
    Disconnect,
}

fn cstr_arg(ptr: *const c_char) -> Result<String, &'static str> {
    if ptr.is_null() {
        return Err("null ptr");
    }
    let s = unsafe { CStr::from_ptr(ptr) }.to_string_lossy().to_string();
    if s.is_empty() {
        return Err("empty string");
    }
    Ok(s)
}

/// Connects to the relay WebSocket and sends a `join` frame.
///
/// relay_url example: ws://localhost:5055/ws
///
/// Returns 0 on success (thread spawned), non-zero on immediate argument error.
#[no_mangle]
pub extern "C" fn memcloud_sc_relay_connect_and_join(
    relay_url: *const c_char,
    vault_id: *const c_char,
    device_id: *const c_char,
    join_token: *const c_char,
    register: bool,
) -> c_int {
    let relay_url = match cstr_arg(relay_url) {
        Ok(v) => v,
        Err(_) => return 2,
    };
    let vault_id = match cstr_arg(vault_id) {
        Ok(v) => v,
        Err(_) => return 3,
    };
    let device_id = match cstr_arg(device_id) {
        Ok(v) => v,
        Err(_) => return 4,
    };
    let join_token = match cstr_arg(join_token) {
        Ok(v) => v,
        Err(_) => return 5,
    };

    // Create an outbound channel for this connection thread.
    let (tx, rx) = mpsc::channel::<Outbound>();

    // Store handle globally (replaces any previous connection handle).
    {
        let mut guard = RELAY_HANDLE.lock().expect("relay handle lock poisoned");
        *guard = Some(RelayHandle { tx: tx.clone() });
    }

    std::thread::spawn(move || {
        emit_event(SyncCoreEvent::RelayStatusChanged {
            v: 1,
            status: "connecting",
            detail: Some("opening websocket"),
        });

        let (mut socket, _response) = match tungstenite::connect(relay_url.as_str()) {
            Ok(v) => v,
            Err(_) => {
                emit_event(SyncCoreEvent::RelayStatusChanged {
                    v: 1,
                    status: "error",
                    detail: Some("connect failed"),
                });
                return;
            }
        };

        // Make reads time out periodically so we can flush outbound queue even if server is quiet.
        // Works for non-TLS connections (our relay is ws://).
        if let MaybeTlsStream::Plain(stream) = socket.get_mut() {
            let _ = stream.set_read_timeout(Some(Duration::from_millis(50)));
        }

        emit_event(SyncCoreEvent::RelayStatusChanged {
            v: 1,
            status: "connected",
            detail: None,
        });

        let join = ClientFrame::Join {
            v: RELAY_PROTOCOL_VERSION,
            vault_id: vault_id.as_str(),
            device_id: device_id.as_str(),
            join_token: join_token.as_str(),
            register,
        };

        let join_json = match serde_json::to_string(&join) {
            Ok(s) => s,
            Err(_) => {
                emit_event(SyncCoreEvent::RelayStatusChanged {
                    v: 1,
                    status: "error",
                    detail: Some("join serialize failed"),
                });
                return;
            }
        };

        if socket.send(tungstenite::Message::Text(join_json)).is_err() {
            emit_event(SyncCoreEvent::RelayStatusChanged {
                v: 1,
                status: "error",
                detail: Some("join send failed"),
            });
            return;
        }

        // Combined read + outbound loop.
        let mut last_outbound_poll = Instant::now();

        loop {
            // Outbound poll (every ~25ms) to keep UI-triggered sends responsive.
            if last_outbound_poll.elapsed() >= Duration::from_millis(25) {
                last_outbound_poll = Instant::now();
                while let Ok(msg) = rx.try_recv() {
                    match msg {
                        Outbound::SendMsg {
                            vault_id,
                            from_device_id,
                            doc_id,
                            nonce,
                            ciphertext,
                        } => {
                            let frame = ClientFrame::Msg {
                                v: RELAY_PROTOCOL_VERSION,
                                vault_id: vault_id.as_str(),
                                from_device_id: from_device_id.as_str(),
                                doc_id: doc_id.as_str(),
                                nonce: nonce.as_str(),
                                ciphertext: ciphertext.as_str(),
                            };

                            if let Ok(json) = serde_json::to_string(&frame) {
                                let _ = socket.send(tungstenite::Message::Text(json));
                            }
                        }
                        Outbound::Disconnect => {
                            let _ = socket.close(None);
                            emit_event(SyncCoreEvent::RelayStatusChanged {
                                v: 1,
                                status: "disconnected",
                                detail: Some("client disconnect"),
                            });
                            return;
                        }
                    }
                }
            }

            match socket.read() {
                Ok(tungstenite::Message::Text(txt)) => {
                    // Detect type first to avoid "msg" being parsed as ServerFrame (extra fields are ignored).
                    if let Ok(inbound) = serde_json::from_str::<InboundFrame>(&txt) {
                        if let InboundFrame::Msg(msg) = inbound {
                            let doc_key = make_doc_key(msg.vault_id.as_str(), msg.doc_id.as_str());
                            emit_event(SyncCoreEvent::RelayMsgReceived {
                                v: msg.v,
                                vaultId: msg.vault_id.as_str(),
                                fromDeviceId: msg.from_device_id.as_str(),
                                docId: msg.doc_id.as_str(),
                                nonce: msg.nonce.as_str(),
                                ciphertext: msg.ciphertext.as_str(),
                            });

                            if let Some(pt) = decrypt_from_relay(
                                msg.vault_id.as_str(),
                                msg.from_device_id.as_str(),
                                msg.doc_id.as_str(),
                                msg.nonce.as_str(),
                                msg.ciphertext.as_str(),
                            ) {
                                // Prefer incremental changes envelope, fallback to snapshot.
                                if let Some(content) = try_apply_changes_envelope(&doc_key, msg.doc_id.as_str(), &pt) {
                                    emit_json(serde_json::json!({
                                        "type": "DocContentChanged",
                                        "v": msg.v,
                                        "docId": msg.doc_id,
                                        "content": content
                                    }));
                                } else if let Some(content) = doc_apply_snapshot(&doc_key, &pt) {
                                    emit_json(serde_json::json!({
                                        "type": "DocContentChanged",
                                        "v": msg.v,
                                        "docId": msg.doc_id,
                                        "content": content
                                    }));
                                } else if let Ok(s) = String::from_utf8(pt) {
                                    emit_json(serde_json::json!({
                                        "type": "RelayMsgDecrypted",
                                        "v": msg.v,
                                        "vaultId": msg.vault_id,
                                        "fromDeviceId": msg.from_device_id,
                                        "docId": msg.doc_id,
                                        "plaintext": s
                                    }));
                                }
                            }
                            continue;
                        }
                    }

                    if let Ok(frame) = serde_json::from_str::<ServerFrame>(&txt) {
                        emit_event(SyncCoreEvent::RelayFrame {
                            v: frame.v,
                            frameType: frame.frame_type.as_str(),
                            vaultId: frame.vault_id.as_deref(),
                            code: frame.code.as_deref(),
                            message: frame.message.as_deref(),
                        });
                    }
                }
                Ok(tungstenite::Message::Close(_)) => {
                    emit_event(SyncCoreEvent::RelayStatusChanged {
                        v: 1,
                        status: "disconnected",
                        detail: Some("closed"),
                    });
                    break;
                }
                Ok(_) => {}
                Err(tungstenite::Error::Io(e))
                    if e.kind() == std::io::ErrorKind::WouldBlock
                        || e.kind() == std::io::ErrorKind::TimedOut =>
                {
                    // Expected due to read timeout; allows outbound flush.
                    continue;
                }
                Err(_) => {
                    emit_event(SyncCoreEvent::RelayStatusChanged {
                        v: 1,
                        status: "disconnected",
                        detail: Some("read error"),
                    });
                    break;
                }
            }
        }

        emit_event(SyncCoreEvent::RelayStatusChanged {
            v: 1,
            status: "disconnected",
            detail: Some("loop ended"),
        });
    });

    0
}

/// Sends an opaque `msg` frame over the relay (typically encrypted payload bytes).
/// Returns 0 if queued, non-zero if not connected or bad args.
#[no_mangle]
pub extern "C" fn memcloud_sc_relay_send_msg(
    vault_id: *const c_char,
    from_device_id: *const c_char,
    doc_id: *const c_char,
    nonce: *const c_char,
    ciphertext: *const c_char,
) -> c_int {
    let vault_id = match cstr_arg(vault_id) {
        Ok(v) => v,
        Err(_) => return 2,
    };
    let from_device_id = match cstr_arg(from_device_id) {
        Ok(v) => v,
        Err(_) => return 3,
    };
    let doc_id = match cstr_arg(doc_id) {
        Ok(v) => v,
        Err(_) => return 4,
    };
    // For now, the `nonce` argument is ignored when encryption is enabled.
    let _nonce_in = cstr_arg(nonce).unwrap_or_default();
    let plaintext = match cstr_arg(ciphertext) {
        Ok(v) => v,
        Err(_) => return 6,
    };

    let (nonce_out, ciphertext_out) =
        match encrypt_for_relay(&vault_id, &from_device_id, &doc_id, plaintext.as_bytes()) {
            Some(v) => v,
            None => return 20, // no vault key set
        };

    let handle = RELAY_HANDLE.lock().ok().and_then(|g| g.clone());
    let Some(handle) = handle else {
        return 10;
    };

    if handle
        .tx
        .send(Outbound::SendMsg {
            vault_id,
            from_device_id,
            doc_id,
            nonce: nonce_out,
            ciphertext: ciphertext_out,
        })
        .is_err()
    {
        return 11;
    }

    0
}

/// Requests the relay thread to disconnect. Safe to call even if not connected.
#[no_mangle]
pub extern "C" fn memcloud_sc_relay_disconnect() {
    let handle = RELAY_HANDLE.lock().ok().and_then(|g| g.clone());
    if let Some(handle) = handle {
        let _ = handle.tx.send(Outbound::Disconnect);
    }
    if let Ok(mut g) = RELAY_HANDLE.lock() {
        *g = None;
    }
}

/// Convenience: sets a doc "content" field and broadcasts an encrypted Automerge snapshot as a relay msg.
/// Returns 0 if queued, 20 if no vault key set, 10/11 if not connected, 2+ for bad args.
#[no_mangle]
pub extern "C" fn memcloud_sc_doc_set_content_and_broadcast(
    vault_id: *const c_char,
    from_device_id: *const c_char,
    doc_id: *const c_char,
    content_utf8: *const c_char,
) -> c_int {
    let vault_id = match cstr_arg(vault_id) {
        Ok(v) => v,
        Err(_) => return 2,
    };
    let from_device_id = match cstr_arg(from_device_id) {
        Ok(v) => v,
        Err(_) => return 3,
    };
    let doc_id = match cstr_arg(doc_id) {
        Ok(v) => v,
        Err(_) => return 4,
    };
    let content = match cstr_arg(content_utf8) {
        Ok(v) => v,
        Err(_) => return 5,
    };

    let handle = RELAY_HANDLE.lock().ok().and_then(|g| g.clone());
    let Some(handle) = handle else {
        return 10;
    };

    // Update local doc bytes (in-memory) and compute changes since last broadcast.
    let doc_key = make_doc_key(&vault_id, &doc_id);

    let existing_bytes = DOC_SNAPSHOTS
        .lock()
        .ok()
        .and_then(|g| g.get(&doc_key).cloned())
        .or_else(|| load_doc_bytes_from_disk(&doc_key));

    let mut doc = if let Some(existing) = existing_bytes {
        AutoCommit::load(&existing).unwrap_or_else(|_| AutoCommit::new())
    } else {
        AutoCommit::new()
    };

    // If this is the first broadcast in this process for an existing doc loaded from disk,
    // initialize last_heads to current heads so we only send *new* changes.
    let mut last_heads = DOC_LAST_BROADCAST_HEADS
        .lock()
        .ok()
        .and_then(|g| g.get(&doc_key).cloned())
        .unwrap_or_default();
    if last_heads.is_empty() {
        last_heads = doc.get_heads();
    }

    doc_set_content(&mut doc, &content);

    // Try to export changes since last broadcast (broadcast-safe incremental payload).
    let changes = doc
        .get_changes(&last_heads)
        .into_iter()
        .cloned()
        .collect::<Vec<_>>();
    let payload = encode_changes_envelope(&doc_id, changes).unwrap_or_else(|| doc.save());

    // Persist updated doc bytes + new heads
    let bytes = doc.save();
    if let Ok(mut g) = DOC_SNAPSHOTS.lock() {
        g.insert(doc_key.clone(), bytes.clone());
    }
    save_doc_bytes_to_disk(&doc_key, &bytes);
    if let Ok(mut g) = DOC_LAST_BROADCAST_HEADS.lock() {
        g.insert(doc_key, doc.get_heads());
    }

    let (nonce_out, ciphertext_out) =
        match encrypt_for_relay(&vault_id, &from_device_id, &doc_id, payload.as_slice()) {
            Some(v) => v,
            None => return 20,
        };

    if handle
        .tx
        .send(Outbound::SendMsg {
            vault_id,
            from_device_id,
            doc_id,
            nonce: nonce_out,
            ciphertext: ciphertext_out,
        })
        .is_err()
    {
        return 11;
    }

    0
}

/// Loads an existing local doc (if any) and emits a `DocContentChanged` event.
///
/// Returns:
/// - 0: loaded and emitted
/// - 1: no local doc found
/// - 2+: bad args / internal error
#[no_mangle]
pub extern "C" fn memcloud_sc_doc_load_and_emit(
    vault_id: *const c_char,
    doc_id: *const c_char,
) -> c_int {
    let vault_id = match cstr_arg(vault_id) {
        Ok(v) => v,
        Err(_) => return 2,
    };
    let doc_id = match cstr_arg(doc_id) {
        Ok(v) => v,
        Err(_) => return 3,
    };

    let doc_key = make_doc_key(&vault_id, &doc_id);
    let bytes = load_doc_bytes_from_disk(&doc_key);
    let Some(bytes) = bytes else { return 1; };

    // Cache in-memory and emit projection
    if let Ok(mut g) = DOC_SNAPSHOTS.lock() {
        g.insert(doc_key, bytes.clone());
    }

    let doc = AutoCommit::load(&bytes).map_err(|_| ()).ok();
    let Some(doc) = doc else { return 4; };

    let content = match doc.get(ROOT, "content").ok().flatten() {
        Some((automerge::Value::Object(ObjType::Text), obj)) => doc.text(&obj).ok().unwrap_or_default(),
        Some((automerge::Value::Scalar(s), _)) => s.to_string(),
        _ => String::new(),
    };

    emit_json(serde_json::json!({
        "type": "DocContentChanged",
        "v": 1,
        "docId": doc_id,
        "content": content
    }));

    0
}

// ---------- Android JNI exports ----------
// Android calls into sync-core via JNI to avoid shipping a separate C shim layer.
#[cfg(target_os = "android")]
mod android_jni {
    use super::*;
    use jni::objects::{JClass, JString};
    use jni::sys::{jboolean, jint, jstring};
    use jni::JNIEnv;

    fn jstring_to_string(env: &mut JNIEnv<'_>, s: JString<'_>) -> String {
        env.get_string(&s)
            .map(|v| v.to_string_lossy().to_string())
            .unwrap_or_default()
    }

    fn to_jstring(env: &mut JNIEnv<'_>, s: &str) -> jstring {
        env.new_string(s).map(|js| js.into_raw()).unwrap_or(std::ptr::null_mut())
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_protocolVersion(
        _env: JNIEnv,
        _clazz: JClass,
    ) -> jint {
        memcloud_sc_protocol_version() as jint
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_abiVersion(
        _env: JNIEnv,
        _clazz: JClass,
    ) -> jint {
        memcloud_sc_abi_version() as jint
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_setStorageDir(
        mut env: JNIEnv,
        _clazz: JClass,
        storage_dir: JString,
    ) -> jint {
        let dir = jstring_to_string(&mut env, storage_dir);
        let c = CString::new(dir).unwrap_or_else(|_| CString::new("").unwrap());
        memcloud_sc_set_storage_dir(c.as_ptr()) as jint
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_setActiveVaultKey(
        mut env: JNIEnv,
        _clazz: JClass,
        vault_key: JString,
    ) -> jint {
        let key = jstring_to_string(&mut env, vault_key);
        let c = CString::new(key).unwrap_or_else(|_| CString::new("").unwrap());
        memcloud_sc_set_active_vault_key(c.as_ptr()) as jint
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_relayConnectAndJoin(
        mut env: JNIEnv,
        _clazz: JClass,
        relay_url: JString,
        vault_id: JString,
        device_id: JString,
        join_token: JString,
        register: jboolean,
    ) -> jint {
        let relay_url = CString::new(jstring_to_string(&mut env, relay_url)).unwrap();
        let vault_id = CString::new(jstring_to_string(&mut env, vault_id)).unwrap();
        let device_id = CString::new(jstring_to_string(&mut env, device_id)).unwrap();
        let join_token = CString::new(jstring_to_string(&mut env, join_token)).unwrap();

        memcloud_sc_relay_connect_and_join(
            relay_url.as_ptr(),
            vault_id.as_ptr(),
            device_id.as_ptr(),
            join_token.as_ptr(),
            register != 0,
        ) as jint
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_relayDisconnect(
        _env: JNIEnv,
        _clazz: JClass,
    ) {
        memcloud_sc_relay_disconnect();
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_docSetContentAndBroadcast(
        mut env: JNIEnv,
        _clazz: JClass,
        vault_id: JString,
        from_device_id: JString,
        doc_id: JString,
        content: JString,
    ) -> jint {
        let vault_id = CString::new(jstring_to_string(&mut env, vault_id)).unwrap();
        let from_device_id = CString::new(jstring_to_string(&mut env, from_device_id)).unwrap();
        let doc_id = CString::new(jstring_to_string(&mut env, doc_id)).unwrap();
        let content = CString::new(jstring_to_string(&mut env, content)).unwrap();

        memcloud_sc_doc_set_content_and_broadcast(
            vault_id.as_ptr(),
            from_device_id.as_ptr(),
            doc_id.as_ptr(),
            content.as_ptr(),
        ) as jint
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_docLoadAndEmit(
        mut env: JNIEnv,
        _clazz: JClass,
        vault_id: JString,
        doc_id: JString,
    ) -> jint {
        let vault_id = CString::new(jstring_to_string(&mut env, vault_id)).unwrap();
        let doc_id = CString::new(jstring_to_string(&mut env, doc_id)).unwrap();
        memcloud_sc_doc_load_and_emit(vault_id.as_ptr(), doc_id.as_ptr()) as jint
    }

    #[no_mangle]
    pub extern "system" fn Java_com_cbo_core_sync_SyncCoreNative_pollEventJson(
        mut env: JNIEnv,
        _clazz: JClass,
    ) -> jstring {
        let ptr = memcloud_sc_poll_event_json_utf8();
        if ptr.is_null() {
            return std::ptr::null_mut();
        }
        let s = unsafe { CStr::from_ptr(ptr) }.to_string_lossy().to_string();
        memcloud_sc_free_utf8(ptr);
        to_jstring(&mut env, &s)
    }
}

