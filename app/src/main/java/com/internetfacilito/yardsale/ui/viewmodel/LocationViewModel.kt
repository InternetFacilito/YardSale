package com.internetfacilito.yardsale.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationViewModel : ViewModel() {
    
    private val _currentLocation = MutableStateFlow<LatLng?>(null)
    val currentLocation: StateFlow<LatLng?> = _currentLocation.asStateFlow()
    
    private val _hasLocationPermission = MutableStateFlow(false)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()
    
    private val _isLoadingLocation = MutableStateFlow(false)
    val isLoadingLocation: StateFlow<Boolean> = _isLoadingLocation.asStateFlow()
    
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()
    
    private var fusedLocationClient: FusedLocationProviderClient? = null
    
    fun initializeLocationClient(context: Context) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        }
    }
    
    fun checkLocationPermission(context: Context) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        println("🔐 Permisos de ubicación: $hasPermission")
        _hasLocationPermission.value = hasPermission
        
        if (hasPermission) {
            getCurrentLocation(context)
        } else {
            println("❌ No hay permisos de ubicación")
        }
    }
    
    fun getCurrentLocation(context: Context) {
        if (fusedLocationClient == null) {
            initializeLocationClient(context)
        }
        
        viewModelScope.launch {
            try {
                println("🔍 Iniciando obtención de ubicación...")
                _isLoadingLocation.value = true
                _locationError.value = null
                
                if (_hasLocationPermission.value) {
                    println("✅ Permisos concedidos, obteniendo ubicación...")
                    val location = fusedLocationClient?.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        object : CancellationToken() {
                            override fun onCanceledRequested(listener: OnTokenCanceledListener) = CancellationTokenSource().token
                            override fun isCancellationRequested() = false
                        }
                    )?.await()
                    
                    location?.let {
                        _currentLocation.value = LatLng(it.latitude, it.longitude)
                        println("📍 Ubicación obtenida exitosamente: ${it.latitude}, ${it.longitude}")
                    } ?: run {
                        println("⚠️ No se pudo obtener ubicación (null)")
                        _locationError.value = "location_error_generic"
                    }
                } else {
                    println("❌ No hay permisos de ubicación")
                    _locationError.value = "location_permission_denied"
                }
            } catch (e: Exception) {
                println("❌ Error al obtener ubicación: ${e.message}")
                _locationError.value = "location_error_generic_with_message"
            } finally {
                _isLoadingLocation.value = false
                println("🏁 Finalizada obtención de ubicación")
            }
        }
    }
    
    fun clearLocationError() {
        _locationError.value = null
    }
}

@Composable
fun rememberLocationPermissionLauncher(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    val locationGranted = permissions.entries.all { it.value }
    if (locationGranted) {
        onPermissionGranted()
    } else {
        onPermissionDenied()
    }
} 