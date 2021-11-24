package com.tanasi.jsonapi.example.models

import com.tanasi.jsonapi.JsonApiAttribute
import com.tanasi.jsonapi.JsonApiId
import com.tanasi.jsonapi.JsonApiResource
import com.tanasi.jsonapi.JsonApiType
import kotlin.reflect.KProperty

@JsonApiType("people")
data class People(
    @JsonApiId var id: String,
    @JsonApiAttribute("first-name") private var _firstName: String = "",
    @JsonApiAttribute("last-name") private var _lastName: String = "",
    @JsonApiAttribute("twitter") private var _twitter: String = "",
) : JsonApiResource {

    var firstName: String = _firstName
        set(value) {
            _firstName = value
            field = value
            dirtyProperties.add(this::_firstName)
        }
    var lastName: String = _lastName
        set(value) {
            _lastName = value
            field = value
            dirtyProperties.add(this::_lastName)
        }
    var twitter: String = _twitter
        set(value) {
            _twitter = value
            field = value
            dirtyProperties.add(this::_twitter)
        }

    override val dirtyProperties: MutableList<KProperty<*>> = mutableListOf()
}