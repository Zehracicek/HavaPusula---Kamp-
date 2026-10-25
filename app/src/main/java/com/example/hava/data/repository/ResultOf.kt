package com.example.hava.data.repository

import kotlinx.coroutines.CancellationException

/** İstisnaları [Result]'a çevirir; iptal edilen coroutine'in iptali yutulmaz. */
internal inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
