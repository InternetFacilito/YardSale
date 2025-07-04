package com.internetfacilito.yardsale.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.internetfacilito.yardsale.R
import com.internetfacilito.yardsale.data.model.User
import com.internetfacilito.yardsale.data.model.DistanceUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchRadiusScreen(
    user: User,
    onSave: (Float, DistanceUnit) -> Unit,
    onCancel: () -> Unit
) {
    // Convertir el radio almacenado en km a la unidad preferida del usuario
    val unit = try { user.unidadDistancia } catch (e: Exception) { DistanceUnit.KILOMETERS }
    val initialRadiusInPreferredUnit = user.radioBusquedaKm / unit.conversionToKm
    var currentRadius by remember { mutableStateOf(initialRadiusInPreferredUnit) }
    var currentUnit by remember { mutableStateOf(unit) }
    var isSaving by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp,
                top = 80.dp // Padding extra en la parte superior para el botón de menú
            )
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título
        Text(
            text = stringResource(R.string.search_radius_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Descripción
        Text(
            text = stringResource(R.string.search_radius_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Radio actual
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.search_radius_current, "${String.format("%.1f", currentRadius)} ${currentUnit.symbol}"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Slider
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Etiquetas de rango
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (currentUnit == DistanceUnit.KILOMETERS) "0.5 km" else "0.3 mi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (currentUnit == DistanceUnit.KILOMETERS) "10 km" else "6.2 mi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Slider
            Slider(
                value = currentRadius,
                onValueChange = { currentRadius = it },
                valueRange = if (currentUnit == DistanceUnit.KILOMETERS) 0.5f..10f else 0.3f..6.2f,
                steps = 19, // 19 pasos
                modifier = Modifier.fillMaxWidth()
            )
            
            // Valor actual del slider
            Text(
                text = "${currentRadius} ${currentUnit.symbol}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        
        // Selector de unidad de distancia
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.search_radius_unit_selector),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón para Kilómetros
                    OutlinedButton(
                        onClick = { 
                            // Convertir el valor actual a la nueva unidad
                            val radiusInKm = currentRadius * currentUnit.conversionToKm
                            currentRadius = radiusInKm / DistanceUnit.KILOMETERS.conversionToKm
                            currentUnit = DistanceUnit.KILOMETERS 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (currentUnit == DistanceUnit.KILOMETERS) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(stringResource(R.string.search_radius_unit_km))
                    }
                    
                    // Botón para Millas
                    OutlinedButton(
                        onClick = { 
                            // Convertir el valor actual a la nueva unidad
                            val radiusInKm = currentRadius * currentUnit.conversionToKm
                            currentRadius = radiusInKm / DistanceUnit.MILES.conversionToKm
                            currentUnit = DistanceUnit.MILES 
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (currentUnit == DistanceUnit.MILES) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(stringResource(R.string.search_radius_unit_miles))
                    }
                }
            }
        }
        
        // Información adicional
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.search_radius_info_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.search_radius_info_smaller),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.search_radius_info_larger),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.search_radius_info_changeable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        
        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón Cancelar
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Text(stringResource(R.string.search_radius_cancel))
            }
            
            // Botón Guardar
                    Button(
            onClick = {
                isSaving = true
                onSave(currentRadius, currentUnit)
            },
                modifier = Modifier.weight(1f),
                enabled = !isSaving && (currentRadius * currentUnit.conversionToKm != user.radioBusquedaKm || currentUnit != (try { user.unidadDistancia } catch (e: Exception) { DistanceUnit.KILOMETERS }))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.search_radius_save))
            }
        }
    }
} 