package com.example.hava.data.remote.dto

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String? = null,
    @SerializedName("country_code") val countryCode: String? = null,
    @SerializedName("admin1") val region: String? = null,
    @SerializedName("population") val population: Long? = null,
)
