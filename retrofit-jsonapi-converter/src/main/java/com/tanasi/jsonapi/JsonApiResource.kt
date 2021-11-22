package com.tanasi.jsonapi

import kotlin.reflect.KProperty

interface JsonApiResource {
    val dirtyProperties: MutableList<KProperty<*>>
}
