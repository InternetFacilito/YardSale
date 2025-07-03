package com.internetfacilito.yardsale.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.internetfacilito.yardsale.data.model.GuestSession
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.res.stringResource
import com.internetfacilito.yardsale.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingGuestMenu(
    guestSession: GuestSession?,
    onMenuToggle: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    // Función para manejar el login y ocultar el menú
    val handleLogin = {
        onMenuToggle() // Ocultar el menú
        onLoginClick() // Llamar al callback original
    }
    
    // Función para manejar el registro y ocultar el menú
    val handleRegister = {
        onMenuToggle() // Ocultar el menú
        onRegisterClick() // Llamar al callback original
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f), // Asegurar que esté por encima del contenido
        contentAlignment = Alignment.TopCenter
    ) {
        // Contenido expandible (centrado, más abajo para no tapar el botón)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp), // Margen superior más grande para evitar tapar el botón
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Título principal (centrado)
                Text(
                    text = stringResource(R.string.guest_menu_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Información del dispositivo (tarjeta centrada, texto a la izquierda)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start // Texto a la izquierda
                    ) {
                        Text(
                            text = stringResource(R.string.guest_menu_device_info),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Text(
                            text = "ID: ${guestSession?.deviceId?.take(8)}...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = stringResource(R.string.guest_menu_sessions_used, guestSession?.contadorSesiones ?: 0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        val sesionesRestantes = 10 - (guestSession?.contadorSesiones ?: 0)
                        Text(
                            text = stringResource(R.string.guest_menu_sessions_remaining, sesionesRestantes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (sesionesRestantes <= 1) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                // Advertencia si quedan pocas sesiones (tarjeta centrada, texto a la izquierda)
                if (guestSession?.contadorSesiones?.let { it >= 9 } ?: false) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (guestSession.limiteAlcanzado) {
                                MaterialTheme.colorScheme.errorContainer
                            } else {
                                MaterialTheme.colorScheme.tertiaryContainer
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start // Texto a la izquierda
                        ) {
                            Text(
                                text = if (guestSession.limiteAlcanzado) {
                                    stringResource(R.string.guest_menu_limit_reached)
                                } else {
                                    stringResource(R.string.guest_menu_last_session)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = if (guestSession.limiteAlcanzado) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (guestSession.limiteAlcanzado) {
                                    stringResource(R.string.guest_menu_limit_reached_message)
                                } else {
                                    stringResource(R.string.guest_menu_last_session_message)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (guestSession.limiteAlcanzado) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                }
                            )
                        }
                    }
                }
                
                // Botones de acción (centrados) - usando los nuevos handlers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = handleLogin,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(stringResource(R.string.guest_menu_login))
                    }
                    
                    Button(
                        onClick = handleRegister,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.guest_menu_register))
                    }
                }
                
                // Información adicional (tarjeta centrada, texto a la izquierda)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start // Texto a la izquierda
                    ) {
                        Text(
                            text = stringResource(R.string.guest_menu_why_register),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.guest_menu_benefit_1),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.guest_menu_benefit_2),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.guest_menu_benefit_3),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = stringResource(R.string.guest_menu_benefit_4),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                // Botón para ocultar el menú
                Button(
                    onClick = { onMenuToggle() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = stringResource(R.string.guest_menu_hide)
                        )
                        Text(stringResource(R.string.guest_menu_hide))
                    }
                }
            }
        }
    }
} 