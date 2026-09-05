package com.cbo.notes.presentation.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cbo.notes.R
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.presentation.viewmodel.MapViewModel
import com.cbo.notes.presentation.viewmodel.MapUiState
import com.cbo.ui.theme.MemCloudApplicationTheme
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

private fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onNavigateToEditNote: (noteId: Int) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    MapScreenContent(
        uiState = uiState,
        onNavigateToEditNote = onNavigateToEditNote,
        onSelectNote = { viewModel.selectNote(it) },
        onUpdateCurrentLocation = { viewModel.updateCurrentLocation(it) },
        onToggleCategory = { viewModel.toggleCategory(it) },
        onToggleTag = { viewModel.toggleTag(it) },
        onToggleNearbyFilter = { viewModel.toggleNearbyFilter() },
        onClearFilters = { viewModel.clearFilters() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreenContent(
    uiState: MapUiState,
    onNavigateToEditNote: (noteId: Int) -> Unit,
    onSelectNote: (com.cbo.notes.domain.model.Note?) -> Unit,
    onUpdateCurrentLocation: (Location) -> Unit,
    onToggleCategory: (com.cbo.notes.domain.model.Category) -> Unit,
    onToggleTag: (com.cbo.notes.domain.model.Tag) -> Unit,
    onToggleNearbyFilter: () -> Unit,
    onClearFilters: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val fusedLocationClient = remember { 
        if (isPreview) null else LocationServices.getFusedLocationProviderClient(context) 
    }
    val coroutineScope = rememberCoroutineScope()
    
    var hasLocationPermission by remember {
        mutableStateOf(context.hasLocationPermission())
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.92077, 32.85411), 6f)
    }

    val fetchCurrentLocation = remember(fusedLocationClient, hasLocationPermission) {
        { onLocationFound: ((Location) -> Unit)? ->
            if (!isPreview && hasLocationPermission) {
                try {
                    fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            onUpdateCurrentLocation(location)
                            onLocationFound?.invoke(location)
                        }
                    }?.addOnFailureListener {
                        // ignore failure
                    }
                } catch (_: SecurityException) {
                    hasLocationPermission = false
                }
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted && !isPreview) {
            fetchCurrentLocation { location ->
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(location.latitude, location.longitude), 12f
                )
            }
        }
    }

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var filterSearchQuery by remember { mutableStateOf("") }

    val activeFilterCount = uiState.selectedCategories.size + uiState.selectedTags.size

    LaunchedEffect(Unit) {
        if (!isPreview) {
            if (context.hasLocationPermission()) {
                hasLocationPermission = true
                fetchCurrentLocation { location ->
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(location.latitude, location.longitude), 12f
                    )
                }
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!isPreview) {
                        if (hasLocationPermission) {
                            fetchCurrentLocation { location ->
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(location.latitude, location.longitude), 15f
                                        )
                                    )
                                }
                            }
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 70.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.map_my_location))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Harita
            if (isPreview) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Google Map (Preview'da yüklenmez)", color = Color.Gray)
                }
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
                    uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                    onMapClick = {
                        onSelectNote(null)
                    }
                ) {
                    uiState.filteredNotes.forEach { note ->
                        val lat = note.reminderLatitude ?: return@forEach
                        val lng = note.reminderLongitude ?: return@forEach
                        val hue = getHueFromHexColor(note.category?.color)
                        
                        Marker(
                            state = MarkerState(position = LatLng(lat, lng)),
                            title = note.title,
                            snippet = note.category?.name ?: stringResource(R.string.uncategorized),
                            icon = BitmapDescriptorFactory.defaultMarker(hue),
                            onClick = {
                                onSelectNote(note)
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLng(LatLng(lat, lng))
                                    )
                                }
                                true
                            }
                        )
                    }
                }
            }
            
            // Kompakt Üst Filtreleme Barı
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filtrele Butonu
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isFilterSheetOpen = true }
                    ) {
                        BadgedBox(
                            badge = {
                                if (activeFilterCount > 0) {
                                    Badge { Text(activeFilterCount.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filter))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.filters),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Yakınımdakiler Çipi
                    FilterChip(
                        selected = uiState.isNearbyFilterEnabled,
                        onClick = {
                            if (!uiState.isNearbyFilterEnabled && !isPreview) {
                                if (hasLocationPermission) {
                                    fetchCurrentLocation { loc ->
                                        onToggleNearbyFilter()
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            } else {
                                onToggleNearbyFilter()
                            }
                        },
                        label = { Text(stringResource(R.string.map_nearby_filter)) },
                        leadingIcon = {
                            if (uiState.isNearbyFilterEnabled) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    )
                }
            }
        }
    }
    
    // Not Önizleme BottomSheet'i (Seçili not varsa açılır)
    if (uiState.selectedNote != null) {
        val note = uiState.selectedNote
        ModalBottomSheet(
            onDismissRequest = { onSelectNote(null) },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = { onSelectNote(null) }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (note.reminderLocationName != null) {
                    Text(
                        text = "📍 ${note.reminderLocationName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { 
                        onSelectNote(null)
                        onNavigateToEditNote(note.id) 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.map_go_to_note))
                }
            }
        }
    }

    // Filtreleme BottomSheet'i (isFilterSheetOpen true ise açılır)
    if (isFilterSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f) // Ekranın %80'i kadar alan kaplasın
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.filter),
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (activeFilterCount > 0) {
                        TextButton(onClick = { onClearFilters() }) {
                            Text(stringResource(R.string.clear_count, activeFilterCount))
                        }
                    }
                }

                OutlinedTextField(
                    value = filterSearchQuery,
                    onValueChange = { filterSearchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.map_search_cat_tag_placeholder)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search)) },
                    trailingIcon = {
                        if (filterSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { filterSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                            }
                        }
                    },
                    singleLine = true
                )
                
                val filteredCategories = uiState.categories.filter {
                    it.name.contains(filterSearchQuery, ignoreCase = true)
                }
                val filteredTags = uiState.tags.filter {
                    it.name.contains(filterSearchQuery, ignoreCase = true)
                }

                // Liste içeriği scroll edilebilir olacak
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 32.dp)
                ) {
                    if (filteredCategories.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.categories),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filteredCategories.forEach { category ->
                                val isSelected = uiState.selectedCategories.contains(category.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleCategory(category) },
                                    label = { Text(category.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                )
                            }
                        }
                    }

                    if (filteredTags.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.tags_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            filteredTags.forEach { tag ->
                                val isSelected = uiState.selectedTags.contains(tag.id)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onToggleTag(tag) },
                                    label = { Text("#${tag.name}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                        }
                    }
                    
                    if (filteredCategories.isEmpty() && filteredTags.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_results_found),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getHueFromHexColor(hexColor: String?): Float {
    if (hexColor.isNullOrBlank()) return BitmapDescriptorFactory.HUE_RED
    return try {
        val color = android.graphics.Color.parseColor(if (!hexColor.startsWith("#")) "#$hexColor" else hexColor)
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hsv[0]
    } catch (e: Exception) {
        BitmapDescriptorFactory.HUE_RED
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun MapScreenPreview() {
    MemCloudApplicationTheme {
        MapScreenContent(
            uiState = MapUiState(
                categories = listOf(
                    com.cbo.notes.domain.model.Category(id = 1, userId = 1, name = "Kişisel", color = "#FF0000")
                ),
                tags = listOf(
                    com.cbo.notes.domain.model.Tag(id = 1, userId = 1, name = "Acil")
                )
            ),
            onNavigateToEditNote = {},
            onSelectNote = {},
            onUpdateCurrentLocation = {},
            onToggleCategory = {},
            onToggleTag = {},
            onToggleNearbyFilter = {},
            onClearFilters = {}
        )
    }
}
