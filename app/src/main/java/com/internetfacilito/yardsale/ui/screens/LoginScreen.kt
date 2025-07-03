package com.internetfacilito.yardsale.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.res.stringResource
import com.internetfacilito.yardsale.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLogin: (email: String, password: String) -> Unit,
    onGoToRegister: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    // Estados de validación
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isEmailTouched by remember { mutableStateOf(false) }
    var isPasswordTouched by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    
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
    
    // Validación en tiempo real
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
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = stringResource(R.string.login_welcome),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Campo de email
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                if (!isEmailTouched) isEmailTouched = true
            },
            label = { Text(stringResource(R.string.login_email)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = stringResource(R.string.login_email)
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
            label = { Text(stringResource(R.string.login_password)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.login_password)
                )
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) stringResource(R.string.login_hide_password) else stringResource(R.string.login_show_password)
                    )
                }
            }
        )
        
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
        
        // Botón de inicio de sesión
        Button(
            onClick = { 
                // Validar antes de enviar
                isEmailTouched = true
                isPasswordTouched = true
                emailError = validateEmail(email)
                passwordError = validatePassword(password)
                
                // Solo enviar si no hay errores
                if (emailError == null && passwordError == null) {
                    onLogin(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && 
                     emailError == null && passwordError == null
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.login_button))
            }
        }
        
        // Botón de cancelar
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(stringResource(R.string.login_cancel))
        }
        
        // Botón para ir al registro
        TextButton(
            onClick = onGoToRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_register_link))
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Información adicional
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_forgot_password_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 