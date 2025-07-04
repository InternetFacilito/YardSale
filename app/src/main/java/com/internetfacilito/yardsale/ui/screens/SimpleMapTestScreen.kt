package com.internetfacilito.yardsale.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.internetfacilito.yardsale.R
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@Composable
fun SimpleMapTestScreen(
    onBack: () -> Unit
) {
    var mapLoaded by remember { mutableStateOf(false) }
    var mapError by remember { mutableStateOf<String?>(null) }
    
    // Posición fija en Ciudad de México
    val mexicoCity = LatLng(19.4326, -99.1332)
    
    // Estado de la cámara
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(mexicoCity, 10f)
    }
    
    // Propiedades del mapa
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.NORMAL
            )
        )
    }
    
    // Configuración de UI del mapa
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = true,
                compassEnabled = true
            )
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header con información de debug
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "🗺️ Prueba de Mapa Simple",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Estado: ${if (mapLoaded) "✅ Cargado" else "⏳ Cargando..."}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (mapError != null) {
                    Text(
                        text = "Error: $mapError",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = "Ubicación: Ciudad de México (19.4326, -99.1332)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        // Mapa simple
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = {
                    println("🗺️ MAPA SIMPLE: Cargado exitosamente")
                    mapLoaded = true
                    mapError = null
                },
                onMapClick = { latLng ->
                    println("🗺️ MAPA SIMPLE: Click en ${latLng.latitude}, ${latLng.longitude}")
                }
            ) {
                // Un marcador simple
                Marker(
                    state = MarkerState(position = mexicoCity),
                    title = "Ciudad de México",
                    snippet = "Ubicación de prueba",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }
            
            // Botón de regreso
            FloatingActionButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                Text("←")
            }
        }
    }
} 