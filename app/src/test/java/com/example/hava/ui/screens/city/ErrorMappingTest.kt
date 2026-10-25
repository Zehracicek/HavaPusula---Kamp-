package com.example.hava.ui.screens.city

import com.example.hava.data.repository.LocationUnavailableException
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorMappingTest {

    @Test
    fun `bağlantı yoksa NETWORK`() {
        assertEquals(ErrorType.NETWORK, UnknownHostException().toErrorType())
    }

    @Test
    fun `zaman aşımı TIMEOUT`() {
        assertEquals(ErrorType.TIMEOUT, SocketTimeoutException().toErrorType())
        // OkHttp callTimeout aşıldığında bu istisnayı fırlatır
        assertEquals(ErrorType.TIMEOUT, InterruptedIOException("timeout").toErrorType())
    }

    @Test
    fun `HTTP hatası SERVER`() {
        val error = HttpException(Response.error<Any>(503, "".toResponseBody()))
        assertEquals(ErrorType.SERVER, error.toErrorType())
    }

    @Test
    fun `bozuk JSON ağ hatası sayılmaz`() {
        assertEquals(ErrorType.SERVER, MalformedJsonException("bad").toErrorType())
        assertEquals(ErrorType.SERVER, JsonSyntaxException("bad").toErrorType())
    }

    @Test
    fun `konum hataları LOCATION`() {
        assertEquals(ErrorType.LOCATION, LocationUnavailableException().toErrorType())
        assertEquals(ErrorType.LOCATION, SecurityException().toErrorType())
    }

    @Test
    fun `diğer hatalar UNKNOWN`() {
        assertEquals(ErrorType.UNKNOWN, IllegalStateException().toErrorType())
    }
}
