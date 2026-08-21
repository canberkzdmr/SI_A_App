package com.cbo.notes.presentation.screen

import android.annotation.SuppressLint
import android.location.Location
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cbo.notes.presentation.viewmodel.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onNavigateToEditNote: (noteId: Int) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val coroutineScope = rememberCoroutineScope()
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.92077, 32.85411), 6f)
    }

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var filterSearchQuery by remember { mutableStateOf("") }

    val activeFilterCount = uiState.selectedCategories.size + uiState.selectedTags.size

    LaunchedEffect(Unit) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                viewModel.updateCurrentLocation(location)
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(location.latitude, location.longitude), 12f
                )
            }
        }
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            viewModel.updateCurrentLocation(location)
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(location.latitude, location.longitude), 15f
                                    )
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 70.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Benim Konumum")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Harita
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false),
                onMapClick = {
                    viewModel.selectNote(null)
                }
            ) {
                uiState.filteredNotes.forEach { note ->
                    val lat = note.reminderLatitude ?: return@forEach
                    val lng = note.reminderLongitude ?: return@forEach
                    val hue = getHueFromHexColor(note.category?.color)
                    
                    Marker(
                        state = MarkerState(position = LatLng(lat, lng)),
                        title = note.title,
                        snippet = note.category?.name ?: "Kategorisiz",
                        icon = BitmapDescriptorFactory.defaultMarker(hue),
                        onClick = {
                            viewModel.selectNote(note)
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
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrele")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Filtreler",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // Yakınımdakiler Çipi
                    FilterChip(
                        selected = uiState.isNearbyFilterEnabled,
                        onClick = {
                            if (!uiState.isNearbyFilterEnabled) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                    if (loc != null) viewModel.updateCurrentLocation(loc)
                                    viewModel.toggleNearbyFilter()
                                }
                            } else {
                                viewModel.toggleNearbyFilter()
                            }
                        },
                        label = { Text("Yakınımdakiler (5km)") },
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
        val note = uiState.selectedNote!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectNote(null) },
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
                    IconButton(onClick = { viewModel.selectNote(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat")
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
                        viewModel.selectNote(null)
                        onNavigateToEditNote(note.id) 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nota Git")
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
                        text = "Filtrele",
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (activeFilterCount > 0) {
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Temizle ($activeFilterCount)")
                        }
                    }
                }

                OutlinedTextField(
                    value = filterSearchQuery,
                    onValueChange = { filterSearchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    placeholder = { Text("Kategori veya etiket ara...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara") },
                    trailingIcon = {
                        if (filterSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { filterSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Temizle")
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
                            text = "Kategoriler",
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
                                    onClick = { viewModel.toggleCategory(category) },
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
                            text = "Etiketler",
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
                                    onClick = { viewModel.toggleTag(tag) },
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
                                text = "Sonuç bulunamadı",
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
