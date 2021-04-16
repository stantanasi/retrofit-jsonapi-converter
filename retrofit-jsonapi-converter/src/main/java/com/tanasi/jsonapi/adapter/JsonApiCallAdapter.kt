package com.tanasi.jsonapi.adapter

import com.tanasi.jsonapi.JsonApiBody
import com.tanasi.jsonapi.JsonApiResponse
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class JsonApiCallAdapter<T : Any>(
        private val responseType: Type
) : CallAdapter<JsonApiBody<T>, Call<JsonApiResponse<T>>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<JsonApiBody<T>>): Call<JsonApiResponse<T>> = JsonApiCall(call, responseType)
}