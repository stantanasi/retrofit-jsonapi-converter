package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.*

@JsonApiType("articles")
data class Article(
    @JsonApiId override var id: String = "",
    @JsonApiAttribute("title") var title: String = "",
    @JsonApiRelationship("author") var writer: Author? = null
) : JsonApiResource() {

    fun putTitle(title: String) = putAttribute("title", title)

    fun putWriter(writer: Author) = putRelationship("author", writer)
}