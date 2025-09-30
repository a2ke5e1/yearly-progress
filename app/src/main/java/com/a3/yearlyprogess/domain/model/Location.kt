package com.a3.yearlyprogess.domain.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val isManual: Boolean = false
)