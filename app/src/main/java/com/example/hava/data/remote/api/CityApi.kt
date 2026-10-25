package com.example.hava.data.remote.api

import com.example.hava.data.remote.dto.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApi {

    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        /** ISO-3166-1 alpha-2; null ise tüm dünyada aranır */
        @Query("countryCode") countryCode: String? = null,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "tr",
        @Query("format") format: String = "json",
    ): CitySearchResponse
}
