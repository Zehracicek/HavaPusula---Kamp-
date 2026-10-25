package com.example.hava.data.repository

import com.example.hava.data.remote.dto.City

interface LocationRepository {

    /** Cihaz konumunu hava durumu istenebilecek bir şehre çevirir. */
    suspend fun getCurrentCity(): Result<City>
}

/** Konum servisleri kapalı veya makul sürede konum alınamadı. */
class LocationUnavailableException : Exception()
