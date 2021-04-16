package com.tanasi.jsonapi.converter

import com.tanasi.jsonapi.JsonApiResource
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter

class JsonApiRequestConverter : Converter<Any, RequestBody> {

    override fun convert(value: Any): RequestBody? {
        val json = when (value) {
            is JsonApiResource -> value.toJson()
            else -> value.toString()
        }

        return RequestBody.create(MediaType.get("application/json; charset=UTF-8"), json)
    }

}