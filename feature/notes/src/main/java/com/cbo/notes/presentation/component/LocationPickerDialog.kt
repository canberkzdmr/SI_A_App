package com.cbo.notes.presentation.component

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.R
import com.cbo.notes.presentation.viewmodel.LocationSearchViewModel
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun LocationPickerDialog(
    initialLatitude: Double? = null,
    initialLongitude: Double? = null,
    initialRadius: Float = 250f,
    initialIsReminder: Boolean = false,
    onDismissRequest: () -> Unit,
    onLocationSelected: (latitude: Double, longitude: Double, locationName: String, isReminder: Boolean, radius: Float) -> Unit,
    viewModel: LocationSearchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isLoading.collectAsStateWithLifecycle()

    var selectedLocation by remember { 
        mutableStateOf(
            if (initialLatitude != null && initialLongitude != null) 
                LatLng(initialLatitude, initialLongitude) 
            else null
        ) 
    }
    var locationName by remember { mutableStateOf("Seçilen Konum") }
    var isReminder by remember { mutableStateOf(initialIsReminder) }
    var selectedRadius by remember { mutableStateOf(if (initialRadius > 0) initialRadius else 250f) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isReminder = true
        } else {
            isReminder = false
            Toast.makeText(
                context,
                context.getString(R.string.notification_permission_required),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val onToggleReminder: (Boolean) -> Unit = { shouldEnable ->
        if (shouldEnable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    isReminder = true
                } else {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                isReminder = true
            }
        } else {
            isReminder = false
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = if (initialLatitude != null && initialLongitude != null) {
            CameraPosition.fromLatLngZoom(LatLng(initialLatitude, initialLongitude), 15f)
        } else {
            CameraPosition.fromLatLngZoom(LatLng(39.92077, 32.85411), 6f) // Default to Turkey
        }
    }

    LaunchedEffect(Unit) {
        if (selectedLocation == null) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    selectedLocation = latLng
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header & Search
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Konum Seç",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Adres veya mekan ara...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Temizle")
                                }
                            }
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    // Map Content
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(isMyLocationEnabled = true),
                        uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false),
                        onMapClick = { latLng ->
                            selectedLocation = latLng
                            locationName = "Seçilen Konum"
                        }
                    ) {
                        selectedLocation?.let { pos ->
                            Marker(
                                state = MarkerState(position = pos),
                                title = locationName
                            )
                            if (isReminder) {
                                Circle(
                                    center = pos,
                                    radius = selectedRadius.toDouble(),
                                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    strokeColor = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2f
                                )
                            }
                        }
                    }

                    // Arama sonuçları listesi (Haritanın üstüne biner)
                    if (searchResults.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(0.5f) // Haritanın yarısını kaplasın
                                .padding(horizontal = 16.dp),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 8.dp,
                            shadowElevation = 8.dp
                        ) {
                            LazyColumn {
                                items(searchResults) { result ->
                                    ListItem(
                                        headlineContent = { Text(result.primaryText) },
                                        supportingContent = { Text(result.secondaryText) },
                                        modifier = Modifier.clickable {
                                            viewModel.onLocationSelected(result.id) { details ->
                                                details?.let {
                                                    locationName = it.primaryText
                                                    if (it.latitude != null && it.longitude != null) {
                                                        val latLng = LatLng(it.latitude, it.longitude)
                                                        selectedLocation = latLng
                                                        coroutineScope.launch {
                                                            cameraPositionState.animate(
                                                                CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }

                    // Loading Indicator
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        )
                    }

                    // My Location Button
                    FloatingActionButton(
                        onClick = {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                if (location != null) {
                                    val latLng = LatLng(location.latitude, location.longitude)
                                    selectedLocation = latLng
                                    locationName = "Mevcut Konumum"
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Mevcut Konumum")
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable {
                            onToggleReminder(!isReminder)
                        }
                ) {
                    Checkbox(
                        checked = isReminder,
                        onCheckedChange = { checked ->
                            onToggleReminder(checked)
                        }
                    )
                    Text("Buraya geldiğimde bana hatırlat")
                }

                if (isReminder) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Bildirim Yarıçapı: ${selectedRadius.toInt()} metre",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            listOf(150f, 250f, 500f, 1000f).forEach { radiusOption ->
                                FilterChip(
                                    selected = selectedRadius == radiusOption,
                                    onClick = { selectedRadius = radiusOption },
                                    label = {
                                        Text(
                                            if (radiusOption == 250f) "250m (Önerilen)"
                                            else if (radiusOption >= 1000f) "1 km"
                                            else "${radiusOption.toInt()} m"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("İptal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedLocation?.let {
                                if (isReminder && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (!hasPermission) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        return@Button
                                    }
                                }
                                onLocationSelected(
                                    it.latitude,
                                    it.longitude,
                                    locationName,
                                    isReminder,
                                    selectedRadius
                                )
                            }
                        },
                        enabled = selectedLocation != null
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationReminderRowPreview() {
    MemCloudApplicationTheme {
        Surface {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Checkbox(
                    checked = true,
                    onCheckedChange = {}
                )
                Text("Buraya geldiğimde bana hatırlat")
            }
        }
    }
}
