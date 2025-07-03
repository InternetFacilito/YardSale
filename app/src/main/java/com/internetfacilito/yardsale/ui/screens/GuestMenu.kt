package com.internetfacilito.yardsale.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.internetfacilito.yardsale.data.model.GuestSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestMenu(
    guestSession: GuestSession,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            Text(
                text = "Usuario No Registrado",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Información del dispositivo
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Información del Dispositivo:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "ID: ${guestSession.deviceId.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Sesiones usadas: ${guestSession.contadorSesiones}/9",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val sesionesRestantes = 9 - guestSession.contadorSesiones
                    Text(
                        text = "Sesiones restantes: $sesionesRestantes",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (sesionesRestantes <= 1) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            // Advertencia si quedan pocas sesiones
            if (guestSession.contadorSesiones >= 8) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = if (guestSession.limiteAlcanzado) {
                                "⚠️ Límite de sesiones alcanzado"
                            } else {
                                "⚠️ Última sesión disponible"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Regístrate para continuar usando la aplicación",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLogin,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Iniciar Sesión")
                }
                
                Button(
                    onClick = onRegister,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Registrarse")
                }
            }
            
            // Información adicional
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "¿Por qué registrarse?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Acceso ilimitado a todas las funcionalidades",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Crear y gestionar yard sales (vendedores)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Calificar y reportar yard sales (compradores)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Guardar favoritos y recibir notificaciones",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
} 