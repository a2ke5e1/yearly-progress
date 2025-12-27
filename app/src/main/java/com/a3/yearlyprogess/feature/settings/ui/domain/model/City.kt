package com.a3.yearlyprogess.domain.model

data class City(
    val name: String,
    val nameAscii: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val iso2: String,
    val iso3: String,
    val adminName: String,
    val population: Long?
) {
    val displayName: String
        get() = "$nameAscii, $country"

    val searchableText: String
        get() = "$nameAscii $name $country $adminName".lowercase()
}