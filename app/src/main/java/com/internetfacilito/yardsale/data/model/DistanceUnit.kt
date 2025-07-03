package com.internetfacilito.yardsale.data.model

enum class DistanceUnit(val symbol: String, val conversionToKm: Float) {
    KILOMETERS("km", 1.0f),
    MILES("mi", 1.60934f)
} 