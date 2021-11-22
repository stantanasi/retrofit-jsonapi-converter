package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.JsonApiResource
import com.tanasi.jsonapi.JsonApiType
import kotlin.reflect.KProperty

@JsonApiType("articles")
data class Article(
    var id: String? = null,
    var title: String = "",
    var author: People? = null,
    var comments: List<Comment> = listOf(),
) : JsonApiResource {
    override val dirtyProperties: MutableList<KProperty<*>> = mutableListOf()
}