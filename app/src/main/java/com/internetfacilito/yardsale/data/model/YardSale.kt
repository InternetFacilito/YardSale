package com.internetfacilito.yardsale.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class YardSale(
    val id: String = "",
    val vendedorId: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val ubicacion: GeoPoint? = null,
    val direccion: String = "",
    val tipo: YardSaleType = YardSaleType.TEMPORAL,
    val estado: YardSaleStatus = YardSaleStatus.INACTIVA,
    val diasActiva: List<String> = emptyList(),
    val horarios: String = "",
    val imagenes: List<String> = emptyList(),
    val categorias: List<String> = emptyList(),
    val fechaCreacion: Timestamp? = null,
    val fechaActualizacion: Timestamp? = null,
    val fechaInicio: Timestamp? = null,
    val fechaFin: Timestamp? = null,
    val ratingPromedio: Float = 0f,
    val totalRatings: Int = 0,
    val totalReportes: Int = 0
)

enum class YardSaleType {
    TEMPORAL,
    PERMANENTE
}

enum class YardSaleStatus {
    ACTIVA,
    INACTIVA,
    FINALIZADA,
    PAUSADA
} 