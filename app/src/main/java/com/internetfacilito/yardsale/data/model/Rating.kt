package com.internetfacilito.yardsale.data.model

import com.google.firebase.Timestamp

data class Rating(
    val id: String = "",
    val yardSaleId: String = "",
    val compradorId: String = "",
    val rating: Int = 0,
    val comentario: String = "",
    val fecha: Timestamp? = null
) 