package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.JsonApiAttribute
import com.tanasi.jsonapi.JsonApiId
import com.tanasi.jsonapi.JsonApiType

@JsonApiType("people")
data class People(
    @JsonApiId var id: String,
    @JsonApiAttribute("first-name") val firstName: String = "",
    @JsonApiAttribute("last-name") val lastName: String = "",
    @JsonApiAttribute("twitter") val twitter: String = "",
)