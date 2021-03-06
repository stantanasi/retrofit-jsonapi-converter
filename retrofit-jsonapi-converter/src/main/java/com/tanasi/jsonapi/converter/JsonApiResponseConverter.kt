package com.tanasi.jsonapi.converter

import com.tanasi.jsonapi.*
import com.tanasi.jsonapi.bodies.JsonApiBody
import com.tanasi.jsonapi.bodies.JsonApiErrorBody
import com.tanasi.jsonapi.extensions.jsonApiType
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Converter
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor

class JsonApiResponseConverter(
    private val classReference: Class<*>,
    private val classReferenceList: Class<*>? = null,
) : Converter<ResponseBody, Any> {

    override fun convert(value: ResponseBody): Any {
        return if (classReferenceList == null)
            getResource(value, classReference.kotlin)
        else
            getCollection(value, classReference.kotlin)
    }

    private fun getResource(value: ResponseBody, classReference: KClass<*>): JsonApiBody<Any> {
        val json = value.string()
        val jsonObject = JSONObject(json)

        val data = jsonObject.optJSONObject("data") ?: return JsonApiBody(json, null)
        val included = jsonObject.optJSONArray("included")
        val meta = jsonObject.optJSONObject("meta")
        val links = jsonObject.optJSONObject("links")
        val jsonApi = jsonObject.optJSONObject("jsonapi")

        return JsonApiBody(
            json,
            fromJson(data, included, classReference),
            included,
            meta,
            links,
            jsonApi
        )
    }

    private fun getCollection(
        value: ResponseBody,
        classReference: KClass<*>,
    ): JsonApiBody<List<Any>> {
        val json = value.string()
        val jsonObject = JSONObject(json)

        val data = jsonObject.optJSONArray("data") ?: return JsonApiBody(json, null)
        val included = jsonObject.optJSONArray("included")
        val meta = jsonObject.optJSONObject("meta")
        val links = jsonObject.optJSONObject("links")
        val jsonApi = jsonObject.optJSONObject("jsonapi")

        val collection: MutableList<Any> = mutableListOf()
        for (i in 0 until data.length()) {
            collection.add(fromJson(data.optJSONObject(i), included, classReference))
        }

        return JsonApiBody(
            json,
            collection,
            included,
            meta,
            links,
            jsonApi
        )
    }

    companion object {
        fun convertError(json: String): JsonApiErrorBody {
            val jsonObject = JSONObject(json)

            val meta = jsonObject.optJSONObject("meta")
            val links = jsonObject.optJSONObject("links")
            val jsonApi = jsonObject.optJSONObject("jsonapi")

            val errors = jsonObject.optJSONArray("errors")!!
            val errorList: MutableList<JsonApiError> = mutableListOf()
            for (i in 0 until errors.length()) {
                errorList.add(JsonApiError(
                    errors.optJSONObject(i).optString("id"),
                    errors.optJSONObject(i).optJSONObject("links"),
                    errors.optJSONObject(i).optString("status"),
                    errors.optJSONObject(i).optString("code"),
                    errors.optJSONObject(i).optString("title"),
                    errors.optJSONObject(i).optString("detail"),
                    errors.optJSONObject(i).optJSONObject("source"),
                    errors.optJSONObject(i).optJSONObject("meta"),
                ))
            }

            return JsonApiErrorBody(
                json,
                errorList,
                meta,
                JsonApiBody.Links.fromJson(links),
                JsonApiBody.JsonApi.fromJson(jsonApi)
            )
        }
    }


    private fun <T : Any> fromJson(data: JSONObject, included: JSONArray?, c: KClass<T>): T {
        if (data.optString("type") != c.jsonApiType) {
            throw Exception(
                "${c.qualifiedName} doesn't have the same JSON:API type than data object : ${
                    c.jsonApiType
                } != ${data.optString("type")}"
            )
        }

        val attributes: JSONObject? = data.optJSONObject("attributes")
        val relationships: JSONObject? = data.optJSONObject("relationships")

        val params = c.primaryConstructor?.parameters
            ?.filter {
                when {
                    it.hasAnnotation<JsonApiId>() -> data.has("id")
                    it.hasAnnotation<JsonApiAttribute>() -> {
                        val annotation = it.findAnnotation<JsonApiAttribute>()!!
                        attributes?.has(annotation.name) ?: false
                    }
                    it.hasAnnotation<JsonApiRelationship>() -> {
                        val annotation = it.findAnnotation<JsonApiRelationship>()!!
                        if (relationships?.has(annotation.name) == true) {
                            when (val relationshipData =
                                relationships.optJSONObject(annotation.name)?.opt("data")) {
                                is JSONObject -> true
                                is JSONArray -> relationshipData.optJSONObject(0) != null
                                else -> false
                            }
                        } else false
                    }
                    else -> when {
                        data.has(it.name) -> true
                        attributes?.has(it.name) ?: false -> true
                        relationships?.has(it.name) ?: false -> {
                            when (relationships!!.optJSONObject(it.name)?.opt("data")) {
                                is JSONObject -> true
                                is JSONArray -> true
                                else -> false
                            }
                        }
                        else -> false
                    }
                }
            }
            ?.map {
                it to when {
                    it.hasAnnotation<JsonApiId>() -> data.optString("id")
                    it.hasAnnotation<JsonApiAttribute>() -> {
                        val annotation = it.findAnnotation<JsonApiAttribute>()!!
                        getAttribute(annotation.name, attributes!!, it.type)
                    }
                    it.hasAnnotation<JsonApiRelationship>() -> {
                        val annotation = it.findAnnotation<JsonApiRelationship>()!!
                        getRelation(annotation.name, relationships!!, included, it.type)
                    }
                    else -> {
                        when {
                            data.has(it.name) -> data.optString(it.name)
                            attributes?.has(it.name) ?: false -> getAttribute(it.name!!,
                                attributes!!,
                                it.type)
                            relationships?.has(it.name) ?: false -> getRelation(it.name!!,
                                relationships!!,
                                included,
                                it.type)
                            else -> null
                        }
                    }
                }
            }
            ?.toMap()
            ?: mapOf()

        return c.primaryConstructor!!.callBy(params)
    }


    private fun getAttribute(name: String, attributes: JSONObject, kType: KType): Any? {
        return if (attributes.isNull(name)) {
            null
        } else {
            when (kType.classifier) {
                Double::class -> attributes.optDouble(name)
                Long::class -> attributes.optLong(name)
                else -> attributes.opt(name)
            }
        }
    }

    private fun getRelation(
        name: String,
        relationships: JSONObject,
        included: JSONArray?,
        kType: KType,
    ): Any? {
        return when (val relationshipData = relationships.optJSONObject(name)?.opt("data")) {
            is JSONObject -> {
                val type = relationshipData.optString("type")
                val id = relationshipData.optString("id")

                val resourceData = included?.firstOrNull {
                    it.optString("type") == type && it.optString("id") == id
                } ?: return null

                val c = kType.classifier as KClass<*>
                fromJson(
                    resourceData,
                    included,
                    when {
                        c.isSealed -> c.sealedSubclasses
                            .firstOrNull { it.jsonApiType == type }
                            ?: throw Exception("Sealed class ${c.qualifiedName} doesn't contain a class with @JsonApiType equals $type")
                        else -> c
                    }
                )
            }
            is JSONArray -> {
                included?.let {
                    val list: MutableList<Any> = mutableListOf()
                    for (i in 0 until relationshipData.length()) {
                        val type = relationshipData.optJSONObject(i).optString("type")
                        val id = relationshipData.optJSONObject(i).optString("id")

                        val resourceData = included.firstOrNull {
                            it.optString("type") == type && it.optString("id") == id
                        } ?: continue

                        val c = kType.arguments.first().type?.classifier as KClass<*>
                        list.add(fromJson(
                            resourceData,
                            included,
                            when {
                                c.isSealed -> c.sealedSubclasses
                                    .firstOrNull { it.jsonApiType == type }
                                    ?: throw Exception("Sealed class ${c.qualifiedName} doesn't contain a class with @JsonApiType equals $type")
                                else -> c
                            }
                        ))
                    }
                    list
                } ?: mutableListOf<Any>()
            }
            else -> null
        }
    }


    private fun JSONArray.firstOrNull(predicate: (JSONObject) -> Boolean): JSONObject? {
        for (i in 0 until this.length()) {
            if (predicate(this.optJSONObject(i))) {
                return this.optJSONObject(i)
            }
        }
        return null
    }
}