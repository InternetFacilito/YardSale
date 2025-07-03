package com.internetfacilito.yardsale.data.model

import com.google.firebase.Timestamp

data class GuestSession(
    val deviceId: String = "",
    val contadorSesiones: Int = 0,
    val primeraSesion: Timestamp? = null,
    val ultimaSesion: Timestamp? = null,
    val limiteAlcanzado: Boolean = false
) 