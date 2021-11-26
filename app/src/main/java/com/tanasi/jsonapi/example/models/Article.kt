package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.JsonApiProperty
import com.tanasi.jsonapi.JsonApiResource
import com.tanasi.jsonapi.JsonApiType
import kotlin.reflect.KProperty

@JsonApiType("articles")
class Article(
    var id: String? = null,
    title: String = "",
    author: People? = null,
    comments: List<Comment> = listOf(),
) : JsonApiResource {

    var title: String by JsonApiProperty(title)
    var author: People? by JsonApiProperty(author)
    var comments: List<Comment> by JsonApiProperty(comments)

    override val dirtyProperties: MutableList<KProperty<*>> = mutableListOf()
}