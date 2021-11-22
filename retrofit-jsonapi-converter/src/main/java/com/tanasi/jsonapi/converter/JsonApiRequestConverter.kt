package com.tanasi.jsonapi.converter

import com.tanasi.jsonapi.JsonApiAttribute
import com.tanasi.jsonapi.JsonApiRelationship
import com.tanasi.jsonapi.JsonApiResource
import com.tanasi.jsonapi.JsonApiType
import com.tanasi.jsonapi.extensions.jsonApiId
import com.tanasi.jsonapi.extensions.jsonApiType
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Converter
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class JsonApiRequestConverter : Converter<Any, RequestBody> {

    override fun convert(value: Any): RequestBody = RequestBody.create(
        MediaType.get("application/json; charset=UTF-8"),
        toJson(value, value::class).toString()
    )


    private fun <T : Any> toJson(value: T, c: KClass<*>): JSONObject {
        val attributes = JSONObject()
        val relationships = JSONObject()

        val data = JSONObject()
            .put("type", c.jsonApiType)
            .put("id", value.jsonApiId)
            .put("attributes", attributes)
            .put("relationships", relationships)

        c.primaryConstructor?.parameters
            ?.map {
                data class Result(val param: KParameter, val prop: KProperty1<out Any, *>)
                Result(
                    param = it,
                    prop = c.declaredMemberProperties
                        .firstOrNull { property -> property.name == it.name }!!
                        .also { property -> property.isAccessible = true }
                )
            }
            ?.filter {
                when (value) {
                    is JsonApiResource -> value.dirtyProperties.contains(it.prop)
                    else -> true
                }
            }
            ?.filter {
                when {
                    it.param.hasAnnotation<JsonApiAttribute>() -> {
                        val annotation = it.param.findAnnotation<JsonApiAttribute>()!!
                        !annotation.ignore
                    }
                    it.param.hasAnnotation<JsonApiRelationship>() -> {
                        val annotation = it.param.findAnnotation<JsonApiRelationship>()!!
                        !annotation.ignore
                    }
                    else -> true
                }
            }
            ?.forEach {
                val propValue = it.prop.call(value)

                when {
                    it.param.hasAnnotation<JsonApiAttribute>() -> {
                        val annotation = it.param.findAnnotation<JsonApiAttribute>()!!
                        attributes.put(annotation.name, propValue)
                    }
                    it.param.hasAnnotation<JsonApiRelationship>() -> {
                        val annotation = it.param.findAnnotation<JsonApiRelationship>()!!
                        relationships.put(annotation.name,
                            propValue?.toResourceLinkage() ?: JSONObject.NULL)
                    }
                    else -> when {
                        it.prop.isJsonApiAttribute() -> {
                            attributes.put(it.prop.name, it.prop.call(value))
                        }
                        it.prop.isJsonApiRelationship() -> {
                            relationships.put(it.prop.name,
                                propValue?.toResourceLinkage() ?: JSONObject.NULL)
                        }
                    }
                }
            }

        return JSONObject().put("data", data)
    }

    private fun Any.toResourceLinkage(): JSONObject = when (this) {
        is List<*> -> {
            JSONObject()
                .put("data", JSONArray().also { data ->
                    this
                        .filterNotNull()
                        .forEach { item ->
                            data
                                .put(JSONObject()
                                    .put("type", item::class.jsonApiType)
                                    .put("id", item.jsonApiId))
                        }
                })
        }
        else -> {
            JSONObject()
                .put("data", JSONObject()
                    .put("type", this::class.jsonApiType)
                    .put("id", this.jsonApiId))
        }
    }


    private fun KProperty<*>.isJsonApiId(): Boolean {
        return this.name == "id"
    }

    private fun KProperty<*>.isJsonApiAttribute(): Boolean {
        return !this.isJsonApiId() && !this.isJsonApiRelationship()
    }

    private fun KProperty<*>.isJsonApiRelationship(): Boolean {
        val c = when (this.returnType.classifier) {
            List::class -> this.returnType.arguments.first().type?.classifier as KClass<*>
            else -> this.returnType.classifier as KClass<*>
        }

        return c.hasAnnotation<JsonApiType>()
    }
}