package com.example.hava.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CitySearchResponse(
    // Sonuç olmadığında API bu alanı hiç göndermiyor
    @SerializedName("results") val results: List<City>? = null,
)
