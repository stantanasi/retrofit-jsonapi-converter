package com.tanasi.jsonapi.converter

import com.tanasi.jsonapi.JsonApiType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.full.hasAnnotation

class JsonApiConverterFactory : Converter.Factory() {

    companion object {
        fun create(): JsonApiConverterFactory {
            return JsonApiConverterFactory()
        }
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<ResponseBody, *>? {
        if (!type.isJsonApiResource()) {
            return null
        }

        return if (type is ParameterizedType) {
            val parameterType = getParameterUpperBound(0, type)
            val packageClass = (parameterType as Class<*>).name
            val aClass = Class.forName(packageClass)
            JsonApiResponseConverter(aClass, getRawType(type))
        } else {
            val aClass = Class.forName((type as Class<*>).name)
            JsonApiResponseConverter(aClass)
        }
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>,
        methodAnnotations: Array<Annotation>,
        retrofit: Retrofit,
    ): Converter<*, RequestBody>? {
        if (!type.isJsonApiResource()) {
            return null
        }
        return JsonApiRequestConverter()
    }


    private fun Type.isJsonApiResource(): Boolean {
        val resourceClass = when (this) {
            is ParameterizedType -> {
                var newType = getParameterUpperBound(0, this)
                while (newType is ParameterizedType) {
                    newType = getParameterUpperBound(0, newType)
                }
                Class.forName(getRawType(newType).name)
            }
            else -> Class.forName(getRawType(this).name)
        }.kotlin

        return resourceClass.hasAnnotation<JsonApiType>()
    }
}