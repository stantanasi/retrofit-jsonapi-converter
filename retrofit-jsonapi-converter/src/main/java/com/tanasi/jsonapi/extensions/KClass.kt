package com.tanasi.jsonapi.extensions

import com.tanasi.jsonapi.JsonApiType
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

val KClass<*>.jsonApiType: String
    get() {
        return this.findAnnotation<JsonApiType>()?.name
            ?: throw Exception("${this.qualifiedName} class doesn't have @JsonApiType annotation")
    }