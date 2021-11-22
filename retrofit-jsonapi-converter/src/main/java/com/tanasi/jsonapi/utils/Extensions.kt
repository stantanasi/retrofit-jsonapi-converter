package com.tanasi.jsonapi.utils

import com.tanasi.jsonapi.JsonApiAttribute
import com.tanasi.jsonapi.JsonApiId
import com.tanasi.jsonapi.JsonApiRelationship
import com.tanasi.jsonapi.JsonApiType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

val KClass<*>.jsonApiType: String
    get() {
        return this.findAnnotation<JsonApiType>()?.name
            ?: throw Exception("${this.qualifiedName} class doesn't have @JsonApiType annotation")
    }

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

fun KProperty<*>.jsonApiAttributeName(c: KClass<*>): String =
    this::class.primaryConstructor?.parameters
        ?.firstOrNull { it.name == this.name }
        ?.findAnnotation<JsonApiAttribute>()
        ?.name
        ?: this.name

fun KProperty<*>.jsonApiRelationshipName(c: KClass<*>): String =
    this::class.primaryConstructor?.parameters
        ?.firstOrNull { it.name == this.name }
        ?.findAnnotation<JsonApiRelationship>()
        ?.name
        ?: this.name
