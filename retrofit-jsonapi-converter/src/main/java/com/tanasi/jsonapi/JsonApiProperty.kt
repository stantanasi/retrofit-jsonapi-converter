package com.tanasi.jsonapi

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class JsonApiProperty<T>(private val default: T?) {

    operator fun provideDelegate(thisRef: Any, property: KProperty<*>): ReadWriteProperty<Any, T> {
        return when {
            property.returnType.isMarkedNullable -> JsonApiPropertyNullable(default)
            else -> JsonApiPropertyNotNull(default!!)
        } as ReadWriteProperty<Any, T>
    }
}

private class JsonApiPropertyNullable<T>(default: T?) : ReadWriteProperty<Any, T?> {

    var field: T? = default

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return field
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        field = value
        when (thisRef) {
            is JsonApiResource -> thisRef.dirtyProperties.add(property)
        }
    }
}

private class JsonApiPropertyNotNull<T>(default: T) : ReadWriteProperty<Any, T> {

    var field: T = default

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        return field
    }

    override operator fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        field = value
        when (thisRef) {
            is JsonApiResource -> thisRef.dirtyProperties.add(property)
        }
    }
}