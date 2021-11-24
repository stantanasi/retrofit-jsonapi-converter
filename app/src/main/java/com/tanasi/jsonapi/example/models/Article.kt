package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.JsonApiType

@JsonApiType("articles")
data class Article(
    var id: String? = null,
    var title: String = "",
    var author: People? = null,
    var comments: List<Comment> = listOf(),
)