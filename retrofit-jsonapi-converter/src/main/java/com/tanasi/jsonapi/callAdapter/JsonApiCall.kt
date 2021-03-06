package com.tanasi.jsonapi.callAdapter

import com.tanasi.jsonapi.JsonApiError
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.jsonapi.bodies.JsonApiBody
import com.tanasi.jsonapi.bodies.JsonApiErrorBody
import com.tanasi.jsonapi.converter.JsonApiResponseConverter
import okhttp3.Request
import okio.Timeout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.lang.reflect.Type

class JsonApiCall<T : Any>(
    private val call: Call<JsonApiBody<T>>,
    private val returnType: Type,
) : Call<JsonApiResponse<T>> {

    override fun clone(): Call<JsonApiResponse<T>> = JsonApiCall(
        call.clone(),
        returnType
    )

    override fun execute(): Response<JsonApiResponse<T>> {
        throw UnsupportedOperationException("Network Response call does not support synchronous execution")
    }

    override fun enqueue(callback: Callback<JsonApiResponse<T>>) = synchronized(this) {
        call.enqueue(object : Callback<JsonApiBody<T>> {
            override fun onResponse(
                call: Call<JsonApiBody<T>>,
                response: Response<JsonApiBody<T>>,
            ) {
                val jsonApiResponse = ResponseHandler.handleResponse(response, returnType)
                callback.onResponse(this@JsonApiCall, Response.success(jsonApiResponse))
            }

            override fun onFailure(call: Call<JsonApiBody<T>>, t: Throwable) {
                val jsonApiResponse = ResponseHandler.handleFailure<T>(t)
                callback.onResponse(this@JsonApiCall, Response.success(jsonApiResponse))
            }

        })
    }

    override fun isExecuted(): Boolean = call.isExecuted

    override fun cancel() = call.cancel()

    override fun isCanceled(): Boolean = call.isCanceled

    override fun request(): Request = call.request()

    override fun timeout(): Timeout = call.timeout()


    private object ResponseHandler {

        fun <T : Any> handleResponse(
            response: Response<JsonApiBody<T>>,
            returnType: Type,
        ): JsonApiResponse<T> {
            val code = response.code()
            val headers = response.headers()

            return if (response.isSuccessful) {
                if (returnType == Unit::class.java) {
                    @Suppress("UNCHECKED_CAST")
                    JsonApiResponse.Success(code,
                        JsonApiBody("", Unit),
                        headers) as JsonApiResponse<T>
                } else {
                    val body = response.body()
                    if (body != null) {
                        JsonApiResponse.Success(code, body, headers)
                    } else {
                        JsonApiResponse.Error.UnknownError(Exception("TODO: Code HTTP bon / body = null"))
                    }
                }
            } else {
                val rawBody = response.errorBody()?.string() ?: ""
                try {
                    val body = JsonApiResponseConverter.convertError(rawBody)
                    JsonApiResponse.Error.ServerError(code, body, headers)
                } catch (e: Exception) {
                    JsonApiResponse.Error.ServerError(
                        code,
                        JsonApiErrorBody(
                            rawBody,
                            listOf(
                                JsonApiError(
                                    status = code.toString(),
                                    title = response.message(),
                                    detail = e.message,
                                    meta = JSONObject()
                                        .put("stackTrace", e.stackTrace.toString()),
                                ),
                            ),
                        ),
                        headers
                    )
                }
            }
        }

        fun <T : Any> handleFailure(
            throwable: Throwable,
        ): JsonApiResponse<T> {
            return when (throwable) {
                is IOException -> JsonApiResponse.Error.NetworkError(throwable)
                is HttpException -> {
                    val responseCode = throwable.response()?.code() ?: 520
                    val body =
                        JsonApiResponseConverter.convertError(throwable.response()?.errorBody()
                            ?.string() ?: "")
                    val headers = throwable.response()?.headers()

                    JsonApiResponse.Error.ServerError(responseCode, body, headers)
                }
                else -> JsonApiResponse.Error.UnknownError(throwable)
            }
        }
    }
}