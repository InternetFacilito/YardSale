package com.internetfacilito.yardsale.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.internetfacilito.yardsale.data.model.*
import com.internetfacilito.yardsale.data.repository.FirebaseRepository
import com.internetfacilito.yardsale.data.util.DeviceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.internetfacilito.yardsale.R

class MainViewModel : ViewModel() {
    
    private val repository = FirebaseRepository()
    private val locationViewModel = LocationViewModel()
    
    // Getter para el LocationViewModel
    fun getLocationViewModel(): LocationViewModel = locationViewModel
    
    // Estados
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _guestSession = MutableStateFlow<GuestSession?>(null)
    val guestSession: StateFlow<GuestSession?> = _guestSession.asStateFlow()
    
    private val _yardSales = MutableStateFlow<List<YardSale>>(emptyList())
    val yardSales: StateFlow<List<YardSale>> = _yardSales.asStateFlow()
    
    // Inicializar la aplicación
    fun initializeApp(context: Context) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                // Verificar si ya hay un usuario autenticado
                val currentUser = repository.getCurrentUser()
                if (currentUser != null) {
                    // Usuario ya autenticado - ir directamente al mapa
                    _currentUser.value = currentUser
                    _uiState.value = UiState.Authenticated(currentUser)
                    loadYardSales()
                    return@launch
                }
                
                // Si no hay usuario autenticado, mostrar pantalla de selección
                _uiState.value = UiState.SessionChoice
            } catch (e: Exception) {
                _uiState.value = UiState.Error("error_initialization")
            }
        }
    }
    
    // Inicializar sesión de invitado
    private suspend fun initializeGuestSession(context: Context) {
        try {
            val deviceId = DeviceUtils.getDeviceId(context)
            
            // Siempre llamar a getOrCreateGuestSession para incrementar el contador
            val sessionResult = repository.getOrCreateGuestSession(deviceId)
            
            sessionResult.fold(
                onSuccess = { session ->
                    _guestSession.value = session
                    
                    if (session.limiteAlcanzado) {
                        _uiState.value = UiState.GuestLimitReached(session)
                    } else {
                        _uiState.value = UiState.Guest(session)
                        loadYardSales()
                    }
                },
                onFailure = { exception ->
                    // Si Firebase está offline, crear sesión local temporal
                    if (exception.message?.contains("offline") == true) {
                        val tempSession = GuestSession(
                            deviceId = deviceId,
                            contadorSesiones = 1,
                            primeraSesion = com.google.firebase.Timestamp.now(),
                            ultimaSesion = com.google.firebase.Timestamp.now(),
                            limiteAlcanzado = false
                        )
                        _guestSession.value = tempSession
                        _uiState.value = UiState.Guest(tempSession)
                        loadYardSales()
                    } else {
                        _uiState.value = UiState.Error("error_guest_session")
                    }
                }
            )
        } catch (e: Exception) {
            _uiState.value = UiState.Error("error_device_id")
        }
    }
    
    // Registrar usuario
    fun registerUser(email: String, password: String, nombre: String, tipoUsuario: UserType) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val userData = User(
                    nombre = nombre,
                    tipoUsuario = tipoUsuario
                )
                
                val result = repository.registerUser(email, password, userData)
                
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _guestSession.value = null
                        _uiState.value = UiState.Success("success_registration")
                        // Cambiar a Authenticated después de un breve delay para que se muestre el Snackbar
                        viewModelScope.launch {
                            kotlinx.coroutines.delay(2000) // 2 segundos de delay
                            _uiState.value = UiState.Authenticated(user)
                            loadYardSales()
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("email address is already in use") == true -> 
                                "error_email_already_exists"
                            exception.message?.contains("password is invalid") == true -> 
                                "error_invalid_password"
                            exception.message?.contains("network") == true -> 
                                "error_network"
                            exception.message?.contains("timeout") == true -> 
                                "error_timeout"
                            exception.message?.contains("error_recaptcha_config") == true -> 
                                "error_recaptcha_config"
                            else -> "error_registration_failed"
                        }
                        _uiState.value = UiState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("error_unexpected")
            }
        }
    }
    
    // Iniciar sesión
    fun signInUser(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val result = repository.signInUser(email, password)
                
                result.fold(
                    onSuccess = { user ->
                        _currentUser.value = user
                        _guestSession.value = null
                        _uiState.value = UiState.Authenticated(user)
                        loadYardSales()
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("no user record") == true -> 
                                "error_user_not_found"
                            exception.message?.contains("password is invalid") == true -> 
                                "error_wrong_password"
                            exception.message?.contains("network") == true -> 
                                "error_network"
                            exception.message?.contains("timeout") == true -> 
                                "error_timeout"
                            else -> "error_login_failed"
                        }
                        _uiState.value = UiState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error("error_unexpected")
            }
        }
    }
    
    // Cerrar sesión
    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _currentUser.value = null
            _guestSession.value = null
            // Mostrar mensaje de éxito y luego pantalla de selección
            _uiState.value = UiState.Success("success_sign_out")
            // Después de mostrar el mensaje, cambiar a pantalla de selección
            delay(1500) // Esperar 1.5 segundos para que se vea el mensaje
            _uiState.value = UiState.SessionChoice
        }
    }
    
    // Cargar yard sales
    private suspend fun loadYardSales() {
        try {
            val result = repository.getActiveYardSales()
            
            result.fold(
                onSuccess = { yardSales ->
                    _yardSales.value = yardSales
                },
                onFailure = { exception ->
                    // No cambiar el estado de la UI, solo log del error
                    println("Error al cargar yard sales: ${exception.message}")
                }
            )
        } catch (e: Exception) {
            println("Error inesperado al cargar yard sales: ${e.message}")
        }
    }
    
    // Limpiar error
    fun clearError() {
        if (_uiState.value is UiState.Error) {
            // Mantener el estado actual pero sin error
            val currentUser = _currentUser.value
            val guestSession = _guestSession.value
            
            _uiState.value = when {
                currentUser != null -> UiState.Authenticated(currentUser)
                guestSession != null -> {
                    if (guestSession.limiteAlcanzado) {
                        UiState.GuestLimitReached(guestSession)
                    } else {
                        UiState.Guest(guestSession)
                    }
                }
                else -> UiState.SessionChoice
            }
        }
    }
    
    /**
     * Elimina la sesión de invitado actual (llamar al cerrar la app)
     */
    fun deleteGuestSession(context: Context) {
        viewModelScope.launch {
            try {
                val deviceId = DeviceUtils.getDeviceId(context)
                repository.deleteGuestSession(deviceId)
                _guestSession.value = null
            } catch (e: Exception) {
                // Ignorar errores al eliminar sesión
            }
        }
    }
    
    /**
     * Limpia la sesión de invitado del estado local (no elimina de Firebase)
     */
    fun clearGuestSessionLocally() {
        _guestSession.value = null
    }
    
    /**
     * Reinicializa solo la sesión de invitado (para cuando la app vuelve a primer plano)
     */
    fun reinitializeGuestSession(context: Context) {
        viewModelScope.launch {
            try {
                // Solo reinicializar si hay un usuario autenticado
                val currentUser = repository.getCurrentUser()
                if (currentUser != null) {
                    // Si hay usuario autenticado, asegurar que esté en el estado correcto
                    _currentUser.value = currentUser
                    if (_uiState.value !is UiState.Authenticated) {
                        _uiState.value = UiState.Authenticated(currentUser)
                        loadYardSales()
                    }
                }
                // Si no hay usuario autenticado, mantener la pantalla de selección
            } catch (e: Exception) {
                // Ignorar errores al reinicializar
            }
        }
    }
    
    /**
     * Permite al usuario elegir continuar como invitado después de cerrar sesión
     */
    fun continueAsGuest(context: Context) {
        viewModelScope.launch {
            try {
                initializeGuestSession(context)
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error al continuar como invitado: ${e.message}")
            }
        }
    }
    
    /**
     * Actualiza el radio de búsqueda y unidad de distancia del usuario actual
     */
    fun updateUserSearchRadius(radius: Float, unit: DistanceUnit) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value
                if (currentUser != null) {
                    println("🔄 Actualizando radio de búsqueda: $radius ${unit.symbol}")
                    
                    // Convertir el radio a kilómetros para almacenamiento
                    val radiusInKm = radius * unit.conversionToKm
                    println("📏 Radio convertido a km: $radiusInKm")
                    
                    val result = repository.updateUserSearchRadius(currentUser.id, radiusInKm, unit)
                    
                    result.fold(
                        onSuccess = {
                            println("✅ Radio actualizado exitosamente")
                            // Actualizar el usuario local con el nuevo radio y unidad
                            _currentUser.value = currentUser.copy(
                                radioBusquedaKm = radiusInKm,
                                unidadDistancia = unit
                            )
                            _uiState.value = UiState.Success("success_radius_updated")
                        },
                        onFailure = { exception ->
                            println("❌ Error al actualizar radio: ${exception.message}")
                            _uiState.value = UiState.Error("error_radius_update_failed")
                        }
                    )
                } else {
                    println("❌ No hay usuario actual")
                    _uiState.value = UiState.Error("error_radius_update_failed")
                }
            } catch (e: Exception) {
                println("❌ Excepción inesperada: ${e.message}")
                _uiState.value = UiState.Error("error_unexpected")
            }
        }
    }
    

}

// Estados de la UI
sealed class UiState {
    object Loading : UiState()
    data class Success(val message: String) : UiState()
    data class Authenticated(val user: User) : UiState()
    data class Guest(val session: GuestSession?) : UiState()
    data class GuestLimitReached(val session: GuestSession) : UiState()
    object SessionChoice : UiState()
    data class Error(val message: String) : UiState()
} 