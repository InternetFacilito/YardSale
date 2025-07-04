package com.internetfacilito.yardsale.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.internetfacilito.yardsale.R
import com.internetfacilito.yardsale.ui.viewmodel.MainViewModel
import com.internetfacilito.yardsale.ui.viewmodel.LocationViewModel
import com.internetfacilito.yardsale.ui.viewmodel.rememberLocationPermissionLauncher
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.internetfacilito.yardsale.data.model.GuestSession
import com.internetfacilito.yardsale.data.model.User
import com.internetfacilito.yardsale.data.model.YardSale

@Composable
fun MapScreen(
    yardSales: List<YardSale>,
    currentUser: User?,
    guestSession: GuestSession?,
    onSignOut: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToLogin: () -> Unit,
    onTestSimpleMap: () -> Unit = {},
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val locationViewModel = mainViewModel.getLocationViewModel()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Estados del mapa
    val currentLocation by locationViewModel.currentLocation.collectAsState()
    val hasLocationPermission by locationViewModel.hasLocationPermission.collectAsState()
    val isLoadingLocation by locationViewModel.isLoadingLocation.collectAsState()
    val locationError by locationViewModel.locationError.collectAsState()
    
    // Estado del mapa
    var mapProperties by remember { 
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            )
        ) 
    }
    
    var cameraPositionState by remember {
        mutableStateOf(
            CameraPositionState(
                CameraPosition.fromLatLngZoom(
                    LatLng(19.4326, -99.1332), // Ciudad de México por defecto
                    10f
                )
            )
        )
    }
    
    // Estado para ubicación explorada
    var exploredLocation by remember { mutableStateOf<LatLng?>(null) }
    var isExploring by remember { mutableStateOf(false) }
    

    

    
    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLocationPermissionLauncher(
        onPermissionGranted = {
            println("✅ Permisos de ubicación concedidos")
            locationViewModel.checkLocationPermission(context)
        },
        onPermissionDenied = {
            println("❌ Permisos de ubicación denegados")
        }
    )
    
    // Solicitar permisos automáticamente al iniciar la app
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            println("🔐 Solicitando permisos de ubicación automáticamente...")
            locationPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            println("✅ Permisos de ubicación ya concedidos")
            locationViewModel.initializeLocationClient(context)
            locationViewModel.getCurrentLocation(context)
        }
    }
    
    // Observar el ciclo de vida para inicializar la ubicación
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                locationViewModel.initializeLocationClient(context)
                if (hasLocationPermission) {
                    locationViewModel.getCurrentLocation(context)
                }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Actualizar la posición de la cámara cuando se obtiene la ubicación
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            if (!isExploring) {
                println("🗺️ Centrando mapa en ubicación real: ${location.latitude}, ${location.longitude}")
                cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                mapProperties = mapProperties.copy(isMyLocationEnabled = true)
            }
        }
    }
    

    

    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Debug: Imprimir información del estado
        LaunchedEffect(Unit) {
            println("🔍 DEBUG MAPA:")
            println("   - Permisos de ubicación: $hasLocationPermission")
            println("   - Cargando ubicación: $isLoadingLocation")
            println("   - Error de ubicación: $locationError")
            println("   - Ubicación actual: $currentLocation")
            println("   - Propiedades del mapa: $mapProperties")
            println("   - Intentando cargar Google Maps...")
        }
        
        // Debug: Imprimir información de yard sales
        LaunchedEffect(yardSales) {
            println("🏪 YARD SALES DEBUG:")
            println("   - Total cargadas: ${yardSales.size}")
            println("   - Con ubicación: ${yardSales.count { it.ubicacion != null }}")
            yardSales.forEach { yardSale ->
                println("   - ${yardSale.titulo}: ${yardSale.ubicacion ?: "Sin ubicación"}")
            }
        }
        
        // Mapa de Google
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true,
                mapToolbarEnabled = false,
                compassEnabled = true
            ),
            onMapLoaded = {
                println("🗺️ Mapa cargado exitosamente")
            },
            onMapClick = { latLng ->
                println("🗺️ Click en mapa: ${latLng.latitude}, ${latLng.longitude}")
            },
            onMapLongClick = { latLng ->
                exploredLocation = latLng
                isExploring = true
                // Centrar la cámara en la ubicación explorada
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 15f)
                println("📍 Explorando ubicación: ${latLng.latitude}, ${latLng.longitude}")
            }
        ) {
            // Marcador de ubicación (real o explorada) con pin personalizado
            val locationToShow = if (isExploring && exploredLocation != null) exploredLocation!! else currentLocation
            locationToShow?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = if (isExploring) stringResource(R.string.exploring_location) else stringResource(R.string.your_location),
                    snippet = if (isExploring) stringResource(R.string.exploring_snippet) else stringResource(R.string.current_location_snippet),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
                
                // Círculo de radio alrededor de la ubicación (como radar)
                Circle(
                    center = location,
                    radius = 1000.0, // 1 km de radio
                    fillColor = Color.Blue.copy(alpha = 0.1f),
                    strokeColor = Color.Blue.copy(alpha = 0.3f),
                    strokeWidth = 2f
                )
                
                // Círculo adicional más pequeño para efecto de radar
                Circle(
                    center = location,
                    radius = 500.0, // 500 metros de radio
                    fillColor = Color.Blue.copy(alpha = 0.05f),
                    strokeColor = Color.Blue.copy(alpha = 0.2f),
                    strokeWidth = 1f
                )
            }
            
            // Marcadores de yard sales
            yardSales.forEach { yardSale ->
                // Usar ubicación real de Firebase
                yardSale.ubicacion?.let { geoPoint ->
                    val yardSaleLocation = LatLng(geoPoint.latitude, geoPoint.longitude)
                    
                    Marker(
                        state = MarkerState(position = yardSaleLocation),
                        title = yardSale.titulo,
                        snippet = yardSale.direccion,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
            }
        }
        
        // Overlay superior con información
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Debug info (temporal)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                // Botón de prueba de mapa simple
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔍 Debug Mapa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = onTestSimpleMap,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("🗺️ Probar Mapa Simple")
                    }
                }
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "🔍 Debug Mapa",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "API Key: ✅ (definida en build.gradle)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Estado del mapa: Cargando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Permisos: ${if (hasLocationPermission) "✅" else "❌"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Ubicación: ${if (currentLocation != null) "✅" else "❌"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Error: ${locationError ?: "Ninguno"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Yard Sales cargadas: ${yardSales.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Con ubicación: ${yardSales.count { it.ubicacion != null }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Solicitar permisos si no están concedidos
            if (!hasLocationPermission && !isLoadingLocation && locationError == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.location_permission_required),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.location_permission_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.grant_permissions))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Información de ubicación
            if (isLoadingLocation) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.getting_location),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (locationError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (locationError) {
                                "location_permission_denied" -> stringResource(R.string.location_permission_denied)
                                "location_error_generic" -> stringResource(R.string.location_error_generic)
                                "location_error_generic_with_message" -> stringResource(R.string.location_error_generic_with_message)
                                else -> locationError!!
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = {
                                locationViewModel.clearLocationError()
                                if (!hasLocationPermission) {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    locationViewModel.getCurrentLocation(context)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Información de yard sales
            if (yardSales.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${yardSales.size} Yard Sales activas",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Mostrar las primeras 3 yard sales
                        yardSales.take(3).forEach { yardSale ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = yardSale.titulo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = yardSale.direccion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "★ ${yardSale.ratingPromedio}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        if (yardSales.size > 3) {
                            Text(
                                text = "... y ${yardSales.size - 3} más",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Botón flotante para volver a la ubicación real
        if (hasLocationPermission && currentLocation != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        isExploring = false
                        exploredLocation = null
                        currentLocation?.let { location ->
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 15f)
                        }
                    },
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.my_location)
                    )
                }
            }
        }
    }
} 