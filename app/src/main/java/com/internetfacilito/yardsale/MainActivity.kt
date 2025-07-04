package com.internetfacilito.yardsale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.internetfacilito.yardsale.R
import androidx.compose.ui.res.stringResource
import com.internetfacilito.yardsale.ui.screens.LoginScreen
import com.internetfacilito.yardsale.ui.screens.MapScreen
import com.internetfacilito.yardsale.ui.screens.RegisterScreen
import com.internetfacilito.yardsale.ui.screens.SearchRadiusScreen
import com.internetfacilito.yardsale.ui.screens.SessionChoiceScreen
import com.internetfacilito.yardsale.ui.screens.SimpleMapTestScreen
import com.internetfacilito.yardsale.ui.screens.UserMenu
import com.internetfacilito.yardsale.ui.theme.YardSaleTheme
import com.internetfacilito.yardsale.ui.viewmodel.MainViewModel
import com.internetfacilito.yardsale.ui.viewmodel.UiState
import com.internetfacilito.yardsale.ui.screens.FloatingGuestMenu
import com.internetfacilito.yardsale.data.model.DistanceUnit
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.Icons.Default
import android.widget.Toast
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Snackbar

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    private var backPressedTime = 0L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            YardSaleTheme {
                YardSaleApp(viewModel = viewModel)
            }
        }
        
        // Inicializar la aplicación
        viewModel.initializeApp(this)
    }
    
    override fun onResume() {
        super.onResume()
        // Solo reinicializar si hay un usuario autenticado
        // Si no hay usuario, mantener la pantalla de selección
        viewModel.reinitializeGuestSession(this)
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            // Limpiar sesión de invitado del estado local antes de salir
            clearGuestSessionLocally()
            super.onBackPressed()
        } else {
            Toast.makeText(this, getString(R.string.common_exit_prompt), Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
    
    /**
     * Limpia la sesión de invitado del estado local (no elimina de Firebase)
     */
    private fun clearGuestSessionLocally() {
        val currentState = viewModel.uiState.value
        if (currentState is UiState.Guest) {
            // Solo limpiar el estado local, NO eliminar de Firebase
            viewModel.clearGuestSessionLocally()
        }
        // No hacer nada si está en GuestLimitReached para mantener el límite
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // También limpiar sesión local al destruir la actividad
        clearGuestSessionLocally()
    }
}

@Composable
fun YardSaleApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val guestSession by viewModel.guestSession.collectAsState()
    val yardSales by viewModel.yardSales.collectAsState()
    
    var showLogin by remember { mutableStateOf(false) }
    var showRegister by remember { mutableStateOf(false) }
    var showSearchRadius by remember { mutableStateOf(false) }
    var showSimpleMapTest by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGuestMenu by remember { mutableStateOf(false) }
    var showUserMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Obtener el contexto de la actividad
    val context = LocalContext.current
    
    // Mostrar Snackbar de éxito y cerrar pantallas modales cuando sea apropiado
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                val key = (uiState as UiState.Success).message
                val msg = when (key) {
                    "success_registration" -> context.getString(R.string.success_registration)
                    "success_radius_updated" -> context.getString(R.string.success_radius_updated)
                    "success_sign_out" -> context.getString(R.string.success_sign_out)
                    else -> key
                }
                snackbarHostState.showSnackbar(msg)
                // Cerrar pantallas modales según el tipo de éxito
                when (key) {
                    "success_registration" -> showRegister = false
                    "success_radius_updated" -> showSearchRadius = false
                }
                // Limpiar el estado de éxito para que no se repita
                viewModel.clearError()
            }
            is UiState.Authenticated -> {
                // Cerrar pantallas modales cuando el usuario se autentica exitosamente
                showLogin = false
                showRegister = false
            }
            else -> { /* No hacer nada para otros estados */ }
        }
    }
    
    // Controlar si el menú debe estar abierto automáticamente
    LaunchedEffect(guestSession) {
        if (guestSession?.limiteAlcanzado == true) {
            showGuestMenu = true
        }
    }
    
    // Ocultar menús cuando se abren pantallas modales
    LaunchedEffect(showLogin, showRegister, showSearchRadius) {
        if (showLogin || showRegister || showSearchRadius) {
            showGuestMenu = false
            showUserMenu = false
        }
    }
    
                // Manejar errores
            LaunchedEffect(uiState) {
                if (uiState is UiState.Error) {
                    val errorKey = (uiState as UiState.Error).message
                    errorMessage = when (errorKey) {
                        "error_email_already_exists" -> context.getString(R.string.error_email_already_exists)
                        "error_invalid_password" -> context.getString(R.string.error_invalid_password)
                        "error_network" -> context.getString(R.string.error_network)
                        "error_timeout" -> context.getString(R.string.error_timeout)
                        "error_user_not_found" -> context.getString(R.string.error_user_not_found)
                        "error_wrong_password" -> context.getString(R.string.error_wrong_password)
                        "error_registration_failed" -> context.getString(R.string.error_registration_failed)
                        "error_login_failed" -> context.getString(R.string.error_login_failed)
                        "error_unexpected" -> context.getString(R.string.error_unexpected)
                        "error_recaptcha_config" -> context.getString(R.string.error_recaptcha_config)
                        "error_initialization" -> context.getString(R.string.error_initialization)
                        "error_guest_session" -> context.getString(R.string.error_guest_session)
                        "error_device_id" -> context.getString(R.string.error_device_id)
                        "error_radius_update_failed" -> context.getString(R.string.error_radius_update_failed)
                        else -> errorKey // Si no es una clave conocida, mostrar el mensaje tal como viene
                    }
                }
            }
    
    Scaffold(modifier = Modifier.fillMaxSize(), snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator()
                }
                
                is UiState.Success -> {
                    // Estado temporal para mostrar Snackbar de éxito
                    // Mostrar el mapa como si estuviera autenticado (el usuario ya está autenticado)
                    MapScreen(
                        yardSales = yardSales,
                        currentUser = currentUser,
                        guestSession = null,
                        onSignOut = { viewModel.signOut() },
                        onGoToRegister = { showRegister = true },
                        onGoToLogin = { showLogin = true },
                        onTestSimpleMap = { showSimpleMapTest = true },
                        mainViewModel = viewModel
                    )
                    
                    // Menú para usuarios registrados
                    currentUser?.let { user ->
                        if (showUserMenu) {
                            UserMenu(
                                user = user,
                                onSignOut = { 
                                    viewModel.signOut()
                                    showUserMenu = false
                                },
                                onMenuToggle = { showUserMenu = false },
                                onConfigureSearchRadius = { showSearchRadius = true }
                            )
                        }
                    }
                    
                    // Botón flotante para usuarios registrados
                    if (!showLogin && !showRegister) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10f),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            FloatingActionButton(
                                onClick = { showUserMenu = !showUserMenu },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    imageVector = if (showUserMenu) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = if (showUserMenu) "Cerrar menú" else "Abrir menú"
                                )
                            }
                        }
                    }
                }
                
                is UiState.Authenticated -> {
                    // Usuario autenticado - mostrar mapa
                    MapScreen(
                        yardSales = yardSales,
                        currentUser = currentUser,
                        guestSession = null,
                        onSignOut = { viewModel.signOut() },
                        onGoToRegister = { showRegister = true },
                        onGoToLogin = { showLogin = true },
                        onTestSimpleMap = { showSimpleMapTest = true },
                        mainViewModel = viewModel
                    )
                    
                    // Menú para usuarios registrados
                    currentUser?.let { user ->
                        if (showUserMenu) {
                            UserMenu(
                                user = user,
                                onSignOut = { 
                                    viewModel.signOut()
                                    showUserMenu = false
                                },
                                onMenuToggle = { showUserMenu = false },
                                onConfigureSearchRadius = { showSearchRadius = true }
                            )
                        }
                    }
                    
                    // Botón flotante para usuarios registrados
                    if (!showLogin && !showRegister) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10f),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            FloatingActionButton(
                                onClick = { showUserMenu = !showUserMenu },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    imageVector = if (showUserMenu) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = if (showUserMenu) "Cerrar menú" else "Abrir menú"
                                )
                            }
                        }
                    }
                }
                
                is UiState.Guest -> {
                    // Usuario invitado - mostrar mapa
                    MapScreen(
                        yardSales = yardSales,
                        currentUser = null,
                        guestSession = guestSession,
                        onSignOut = { /* No aplica para invitados */ },
                        onGoToRegister = { showRegister = true },
                        onGoToLogin = { showLogin = true },
                        onTestSimpleMap = { showSimpleMapTest = true },
                        mainViewModel = viewModel
                    )
                    
                    // Solo mostrar botón y menú si no hay pantallas modales abiertas
                    if (!showLogin && !showRegister) {
                        // Menú flotante para invitados
                        if (showGuestMenu) {
                            FloatingGuestMenu(
                                guestSession = guestSession,
                                onMenuToggle = { showGuestMenu = !showGuestMenu },
                                onLoginClick = { showLogin = true },
                                onRegisterClick = { showRegister = true }
                            )
                        }
                        
                        // Botón flotante fijo para controlar el menú (por encima de todo)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10f), // zIndex muy alto para estar por encima de todo
                            contentAlignment = Alignment.TopEnd
                        ) {
                            FloatingActionButton(
                                onClick = { showGuestMenu = !showGuestMenu },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    imageVector = if (showGuestMenu) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (showGuestMenu) "Ocultar" else "Mostrar"
                                )
                            }
                        }
                    }
                }
                
                is UiState.GuestLimitReached -> {
                    // Límite alcanzado - mostrar mapa con menú automáticamente abierto
                    MapScreen(
                        yardSales = yardSales,
                        currentUser = null,
                        guestSession = state.session,
                        onSignOut = { /* No aplica para invitados */ },
                        onGoToRegister = { showRegister = true },
                        onGoToLogin = { showLogin = true },
                        onTestSimpleMap = { showSimpleMapTest = true },
                        mainViewModel = viewModel
                    )
                    
                    // Solo mostrar botón y menú si no hay pantallas modales abiertas
                    if (!showLogin && !showRegister) {
                        // Menú flotante para invitados (automáticamente abierto si límite alcanzado)
                        if (showGuestMenu || state.session.limiteAlcanzado) {
                            FloatingGuestMenu(
                                guestSession = state.session,
                                onMenuToggle = { showGuestMenu = !showGuestMenu },
                                onLoginClick = { showLogin = true },
                                onRegisterClick = { showRegister = true }
                            )
                        }
                        
                        // Botón flotante fijo para controlar el menú (por encima de todo)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(10f), // zIndex muy alto para estar por encima de todo
                            contentAlignment = Alignment.TopEnd
                        ) {
                            FloatingActionButton(
                                onClick = { showGuestMenu = !showGuestMenu },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(48.dp),
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ) {
                                Icon(
                                    imageVector = if (showGuestMenu) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (showGuestMenu) "Ocultar" else "Mostrar"
                                )
                            }
                        }
                    }
                }
                
                is UiState.SessionChoice -> {
                    SessionChoiceScreen(
                        onLogin = { showLogin = true },
                        onRegister = { showRegister = true },
                        onContinueAsGuest = { viewModel.continueAsGuest(context) }
                    )
                }
                
                is UiState.Error -> {
                    // Solo mostrar ErrorContent si no hay pantallas modales abiertas
                    if (!showLogin && !showRegister) {
                        ErrorContent(
                            message = state.message,
                            onDismiss = { 
                                viewModel.clearError()
                                errorMessage = null
                            }
                        )
                    }
                }
            }
            
            // Pantallas modales
            if (showLogin) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    LoginScreen(
                        onLogin = { email, password ->
                            viewModel.signInUser(email, password)
                            // No cerrar la pantalla de login si hay error
                            // Solo se cerrará si el login es exitoso
                        },
                        onGoToRegister = {
                            showLogin = false
                            showRegister = true
                            viewModel.clearError()
                            errorMessage = null
                        },
                        onCancel = {
                            showLogin = false
                            viewModel.clearError()
                            errorMessage = null
                        },
                        isLoading = uiState is UiState.Loading,
                        errorMessage = errorMessage
                    )
                }
            }
            
            if (showRegister) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    RegisterScreen(
                        onRegister = { email, password, nombre, tipoUsuario ->
                            viewModel.registerUser(email, password, nombre, tipoUsuario)
                            // No cerrar la pantalla de registro si hay error
                            // Solo se cerrará si el registro es exitoso
                        },
                        onBackToLogin = {
                            showRegister = false
                            showLogin = true
                            viewModel.clearError()
                            errorMessage = null
                        },
                        onCancel = {
                            showRegister = false
                            viewModel.clearError()
                            errorMessage = null
                        },
                        isLoading = uiState is UiState.Loading,
                        errorMessage = errorMessage
                    )
                }
            }
            
            if (showSearchRadius) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    currentUser?.let { user ->
                        SearchRadiusScreen(
                            user = user,
                            onSave = { radius: Float, unit: DistanceUnit ->
                                viewModel.updateUserSearchRadius(radius, unit)
                            },
                            onCancel = {
                                showSearchRadius = false
                                viewModel.clearError()
                                errorMessage = null
                            }
                        )
                    }
                }
            }
            
            // Pantalla de prueba de mapa simple
            if (showSimpleMapTest) {
                SimpleMapTestScreen(
                    onBack = {
                        showSimpleMapTest = false
                    }
                )
            }
        }
    }
}

@Composable
fun GuestLimitReachedContent(
    session: com.internetfacilito.yardsale.data.model.GuestSession,
    onRegister: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        com.internetfacilito.yardsale.ui.screens.MapScreen(
            yardSales = emptyList(),
            currentUser = null,
            guestSession = session,
            onSignOut = { },
            onGoToRegister = onRegister,
            onGoToLogin = { },
            onTestSimpleMap = { }
        )
    }
}

@Composable
fun ErrorContent(message: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    
    // Traducir el mensaje de error
    val translatedMessage = when (message) {
        "error_email_already_exists" -> context.getString(R.string.error_email_already_exists)
        "error_invalid_password" -> context.getString(R.string.error_invalid_password)
        "error_network" -> context.getString(R.string.error_network)
        "error_timeout" -> context.getString(R.string.error_timeout)
        "error_user_not_found" -> context.getString(R.string.error_user_not_found)
        "error_wrong_password" -> context.getString(R.string.error_wrong_password)
        "error_registration_failed" -> context.getString(R.string.error_registration_failed)
        "error_login_failed" -> context.getString(R.string.error_login_failed)
        "error_unexpected" -> context.getString(R.string.error_unexpected)
        "error_recaptcha_config" -> context.getString(R.string.error_recaptcha_config)
        "error_initialization" -> context.getString(R.string.error_initialization)
        "error_guest_session" -> context.getString(R.string.error_guest_session)
        "error_device_id" -> context.getString(R.string.error_device_id)
        "error_no_internet" -> context.getString(R.string.error_no_internet)
        else -> message // Si no es una clave conocida, mostrar el mensaje tal como viene
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.common_error),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = translatedMessage,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.common_dismiss))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun YardSaleAppPreview() {
    YardSaleTheme {
        Text("YardSale App")
    }
}