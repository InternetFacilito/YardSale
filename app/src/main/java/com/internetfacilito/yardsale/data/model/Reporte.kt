package com.internetfacilito.yardsale.data.model

import com.google.firebase.Timestamp

data class Reporte(
    val id: String = "",
    val yardSaleId: String = "",
    val compradorId: String = "",
    val motivo: String = "",
    val descripcion: String = "",
    val fecha: Timestamp? = null,
    val estado: ReporteStatus = ReporteStatus.PENDIENTE,
    val administradorAsignado: String = "",
    val accionTomada: String = "",
    val fechaResolucion: Timestamp? = null,
    val resueltoPor: String = ""
)

enum class ReporteStatus {
    PENDIENTE,
    EN_REVISION,
    RESUELTO,
    DESCARTADO
} 