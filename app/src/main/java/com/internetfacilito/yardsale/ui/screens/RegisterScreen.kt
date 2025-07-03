package com.internetfacilito.yardsale.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.internetfacilito.yardsale.data.model.UserType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.res.stringResource
import com.internetfacilito.yardsale.R
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegister: (email: String, password: String, nombre: String, tipoUsuario: UserType) -> Unit,
    onBackToLogin: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var selectedUserType by remember { mutableStateOf(UserType.COMPRADOR) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    // Estados de validación
    var nombreError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var isNombreTouched by remember { mutableStateOf(false) }
    var isEmailTouched by remember { mutableStateOf(false) }
    var isPasswordTouched by remember { mutableStateOf(false) }
    var isConfirmPasswordTouched by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
    // Función de validación de nombre
    fun validateNombre(nombre: String): String? {
        return when {
            nombre.isEmpty() -> context.getString(R.string.error_field_required)
            nombre.length < 2 -> context.getString(R.string.error_name_too_short)
            else -> null
        }
    }
    
    // Función de validación de email
    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> context.getString(R.string.error_field_required)
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> 
                context.getString(R.string.error_invalid_email)
            else -> null
        }
    }
    
    // Función de validación de contraseña
    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> context.getString(R.string.error_field_required)
            password.length < 6 -> context.getString(R.string.error_weak_password)
            else -> null
        }
    }
    
    // Función de validación de confirmación de contraseña
    fun validateConfirmPassword(confirmPassword: String, password: String): String? {
        return when {
            confirmPassword.isEmpty() -> context.getString(R.string.error_field_required)
            confirmPassword != password -> context.getString(R.string.error_passwords_not_match)
            else -> null
        }
    }
    
    // Validación en tiempo real
    LaunchedEffect(nombre) {
        if (isNombreTouched) {
            nombreError = validateNombre(nombre)
        }
    }
    
    LaunchedEffect(email) {
        if (isEmailTouched) {
            emailError = validateEmail(email)
        }
    }
    
    LaunchedEffect(password) {
        if (isPasswordTouched) {
            passwordError = validatePassword(password)
        }
    }
    
    LaunchedEffect(confirmPassword, password) {
        if (isConfirmPasswordTouched) {
            confirmPasswordError = validateConfirmPassword(confirmPassword, password)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.register_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        // Campo de nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { 
                nombre = it
                if (!isNombreTouched) isNombreTouched = true
            },
            label = { Text(stringResource(R.string.register_full_name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = nombreError != null,
            supportingText = nombreError?.let { { Text(it) } },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(R.string.register_full_name)
                )
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        
        // Campo de email
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                if (!isEmailTouched) isEmailTouched = true
            },
            label = { Text(stringResource(R.string.register_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = stringResource(R.string.register_email)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        
        // Campo de contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                if (!isPasswordTouched) isPasswordTouched = true
            },
            label = { Text(stringResource(R.string.register_password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.register_password)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showPassword) stringResource(R.string.register_hide_password) else stringResource(R.string.register_show_password)
                    )
                }
            }
        )
        
        // Campo de confirmar contraseña
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                if (!isConfirmPasswordTouched) isConfirmPasswordTouched = true
            },
            label = { Text(stringResource(R.string.register_confirm_password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = confirmPasswordError != null,
            supportingText = confirmPasswordError?.let { { Text(it) } },
            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.register_confirm_password)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (showConfirmPassword) stringResource(R.string.register_hide_confirm_password) else stringResource(R.string.register_show_confirm_password)
                    )
                }
            }
        )
        
        // Selector de tipo de usuario
        Text(
            text = stringResource(R.string.register_user_type),
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserType.values().filter { it != UserType.INVITADO && it != UserType.ADMIN && it != UserType.SUPERADMIN }.forEach { userType ->
                FilterChip(
                    selected = selectedUserType == userType,
                    onClick = { selectedUserType = userType },
                    label = {
                        Text(
                            when (userType) {
                                UserType.VENDEDOR -> stringResource(R.string.register_seller)
                                UserType.COMPRADOR -> stringResource(R.string.register_buyer)
                                else -> userType.name
                            }
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Mensaje de error
        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Botón de registro
        Button(
            onClick = {
                // Validar antes de enviar
                isNombreTouched = true
                isEmailTouched = true
                isPasswordTouched = true
                isConfirmPasswordTouched = true
                nombreError = validateNombre(nombre)
                emailError = validateEmail(email)
                passwordError = validatePassword(password)
                confirmPasswordError = validateConfirmPassword(confirmPassword, password)
                
                // Solo enviar si no hay errores
                if (nombreError == null && emailError == null && 
                    passwordError == null && confirmPasswordError == null) {
                    onRegister(email, password, nombre, selectedUserType)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && 
                     confirmPassword.isNotEmpty() && nombre.isNotEmpty() && 
                     nombreError == null && emailError == null && 
                     passwordError == null && confirmPasswordError == null
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.register_button))
            }
        }
        
        // Botón de cancelar
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.register_cancel))
        }
        
        // Botón para ir al login
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.register_login_link))
        }
        
        // Información adicional
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.register_info_title),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.register_info_seller),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.register_info_buyer),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.register_info_password),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 