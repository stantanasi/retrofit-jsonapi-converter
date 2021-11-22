package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.JsonApiId
import com.tanasi.jsonapi.JsonApiType

@JsonApiType("comments")
data class Comment(
    @JsonApiId val id: String? = null,
    var body: String = "",
    var author: People? = null,
)