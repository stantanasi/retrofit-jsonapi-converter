package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.JsonApiResource
import com.tanasi.jsonapi.JsonApiType

@JsonApiType("people")
class Author(
    override var id: String = "",
    val name: String = ""
) : JsonApiResource() {
}