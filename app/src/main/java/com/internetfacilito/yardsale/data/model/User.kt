package com.internetfacilito.yardsale.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class User(
    val id: String = "",
    val tipoUsuario: UserType = UserType.INVITADO,
    val nombre: String = "",
    val email: String = "",
    val ubicacion: GeoPoint? = null,
    val telefono: String = "",
    val radioBusquedaKm: Float = 1.0f, // Radio de búsqueda en kilómetros
    val fechaRegistro: Timestamp? = null,
    val estado: UserStatus = UserStatus.ACTIVO,
    val fechaSancion: Timestamp? = null,
    val motivoSancion: String = "",
    val creadoPor: String = "",
    val fechaCreacion: Timestamp? = null,
    val fechaActualizacion: Timestamp? = null
)

enum class UserType {
    VENDEDOR,
    COMPRADOR,
    INVITADO,
    ADMIN,
    SUPERADMIN
}

enum class UserStatus {
    ACTIVO,
    SANCIONADO,
    SUSPENDIDO
} 