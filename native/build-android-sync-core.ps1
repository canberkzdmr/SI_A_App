param(
    [ValidateSet("Debug", "Release")]
    [string]$Configuration = "Release",
    [string]$NdkHome = $env:ANDROID_NDK_HOME
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($NdkHome)) {
    throw "ANDROID_NDK_HOME is not set. Set it to your Android NDK folder (e.g. C:\Android\Sdk\ndk\26.3.11579264)."
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$crateDir = Join-Path $repoRoot "native\sync-core"
$outDir = Join-Path $repoRoot "core-sync\src\main\jniLibs"

Write-Host "Building memcloud_sync_core for Android ($Configuration)..." -ForegroundColor Cyan
Write-Host "crateDir=$crateDir"
Write-Host "outDir=$outDir"

if (-not (Get-Command cargo -ErrorAction SilentlyContinue)) {
    throw "cargo not found. Install Rust (rustup) and ensure cargo is on PATH."
}

if (-not (Get-Command cargo-ndk -ErrorAction SilentlyContinue)) {
    throw "cargo-ndk not found. Install it with: cargo install cargo-ndk"
}

$profileFlag = if ($Configuration -eq "Release") { "--release" } else { "" }

# Build common ABIs. Adjust as needed.
#
# NOTE: cargo-ndk expects Android ABI names for `-t` (e.g. arm64-v8a),
# not Rust target triples.
$abis = @(
    @{ Abi="arm64-v8a"; RustTarget="aarch64-linux-android" },
    @{ Abi="armeabi-v7a"; RustTarget="armv7-linux-androideabi" },
    @{ Abi="x86_64"; RustTarget="x86_64-linux-android" }
)

foreach ($a in $abis) {
    $abi = $a.Abi
    $rustTarget = $a.RustTarget

    Write-Host "-> $abi ($rustTarget)" -ForegroundColor Yellow
    Push-Location $crateDir
    try {
        # NOTE: `-o` should point to the *root* jniLibs folder.
        # cargo-ndk will create the `<abi>/` subfolder itself.
        cargo ndk -t $abi -o $outDir build $profileFlag
    } finally {
        Pop-Location
    }

    # Verify output exists where Android expects it.
    $expected = Join-Path $outDir (Join-Path $abi "libmemcloud_sync_core.so")
    if (-not (Test-Path $expected)) {
        throw "Expected output not found: $expected"
    }
}

Write-Host "Done. Native libs copied under core-sync/src/main/jniLibs/." -ForegroundColor Green



