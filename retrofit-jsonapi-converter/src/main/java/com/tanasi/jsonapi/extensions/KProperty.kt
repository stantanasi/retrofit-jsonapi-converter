package com.tanasi.jsonapi.extensions

import com.tanasi.jsonapi.JsonApiAttribute
import com.tanasi.jsonapi.JsonApiRelationship
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

fun KProperty<*>.jsonApiName(c: KClass<*>): String =
    c.primaryConstructor?.parameters
        ?.firstOrNull { it.name == this.name }
        ?.let {
            it.findAnnotation<JsonApiAttribute>()
                ?: it.findAnnotation<JsonApiRelationship>()
        }
        ?.let {
            when (it) {
                is JsonApiAttribute -> it.name
                is JsonApiRelationship -> it.name
                else -> null
            }
        }
        ?: this.name