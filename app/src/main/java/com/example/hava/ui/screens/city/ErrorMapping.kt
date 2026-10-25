package com.example.hava.ui.screens.city

import androidx.annotation.StringRes
import com.example.hava.R
import com.example.hava.data.repository.LocationUnavailableException
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException

// Sıra önemli: MalformedJsonException ve zaman aşımı istisnaları da IOException'dır
internal fun Throwable.toErrorType(): ErrorType = when (this) {
    is MalformedJsonException, is JsonParseException -> ErrorType.SERVER
    is InterruptedIOException -> ErrorType.TIMEOUT
    is IOException -> ErrorType.NETWORK
    is HttpException -> ErrorType.SERVER
    is LocationUnavailableException, is SecurityException -> ErrorType.LOCATION
    else -> ErrorType.UNKNOWN
}

@StringRes
internal fun ErrorType.messageRes(): Int = when (this) {
    ErrorType.NETWORK -> R.string.error_network
    ErrorType.TIMEOUT -> R.string.error_timeout
    ErrorType.SERVER -> R.string.error_server
    ErrorType.LOCATION -> R.string.error_location
    ErrorType.UNKNOWN -> R.string.error_unknown
}
