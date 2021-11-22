package com.tanasi.jsonapi.extensions

import com.tanasi.jsonapi.JsonApiId
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

val Any.jsonApiId: String?
    get() {
        val kParameter = this::class.primaryConstructor?.parameters
            ?.run {
                firstOrNull { it.hasAnnotation<JsonApiId>() }
                    ?: firstOrNull { it.name == "id" }
            }
            ?: throw Exception("${this::class.qualifiedName} class doesn't have an id property")

        return this::class.declaredMemberProperties
            .firstOrNull { it.name == kParameter.name }
            ?.call(this) as String?
    }