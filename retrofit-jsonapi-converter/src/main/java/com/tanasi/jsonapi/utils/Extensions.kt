package com.tanasi.jsonapi.utils

import com.tanasi.jsonapi.JsonApiAttribute
import com.tanasi.jsonapi.JsonApiRelationship
import com.tanasi.jsonapi.JsonApiType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

fun KClass<*>.jsonApiType(): String =
    this.findAnnotation<JsonApiType>()?.name ?: this.qualifiedName ?: ""

fun KClass<*>.jsonApiAttribute(property: KProperty<*>): String =
    when (val kParameter = this.primaryConstructor?.parameters?.find { it.name == property.name }) {
        null -> property.findAnnotation<JsonApiAttribute>()?.name ?: property.name
        else -> kParameter.findAnnotation<JsonApiAttribute>()?.name ?: property.name
    }

fun KClass<*>.jsonApiRelationship(property: KProperty<*>): String =
    when (val kParameter = this.primaryConstructor?.parameters?.find { it.name == property.name }) {
        null -> property.findAnnotation<JsonApiAttribute>()?.name ?: property.name
        else -> kParameter.findAnnotation<JsonApiAttribute>()?.name ?: property.name
    }
