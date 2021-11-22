package com.tanasi.jsonapi.extensions

import com.tanasi.jsonapi.JsonApiAttribute
import com.tanasi.jsonapi.JsonApiRelationship
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

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