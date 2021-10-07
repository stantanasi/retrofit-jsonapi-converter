package com.tanasi.jsonapi

import okhttp3.Headers
import java.io.IOException
import java.lang.Exception

sealed class JsonApiResponse<out T : Any> {

    data class Success<T : Any>(
            val code: Int,
            val body: JsonApiBody<T>,
            val headers: Headers,
    ) : JsonApiResponse<T>()

    sealed class Error: JsonApiResponse<Nothing>() {

        data class ServerError(
                val code: Int,
                val body: JsonApiErrorBody,
                val headers: Headers?,
        ) : Error()

        data class NetworkError(val error: IOException) : Error()

        data class UnknownError(val error: Throwable) : Error()
    }
}