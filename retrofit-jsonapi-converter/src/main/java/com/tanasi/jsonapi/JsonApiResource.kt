package com.tanasi.jsonapi

import android.util.Log
import org.json.JSONException
import org.json.JSONObject

abstract class JsonApiResource {

    open lateinit var id: String

    private val attributes = JSONObject()
    private val relationships = JSONObject()


    protected fun putAttribute(name: String, value: Any?) {
        try {
            attributes.put(name, value ?: JSONObject.NULL)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    protected fun putRelationship(name: String, resource: JsonApiResource) {
        try {
            relationships
                    .put(name, JSONObject()
                            .put("data", JSONObject()
                                    .put("type", (resource.javaClass.kotlin.annotations.find { it is JsonApiType } as? JsonApiType)?.name)
                                    .put("id", resource.id)))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun toJson(): String {
        val result = JSONObject()
        try {
            val data = JSONObject()
            if (id != "") data.put("id", id)
            data.put("type", (this.javaClass.kotlin.annotations.find { it is JsonApiType } as? JsonApiType)?.name)
            if (attributes.length() != 0) data.put("attributes", attributes)
            if (relationships.length() != 0) data.put("relationships", relationships)
            result.put("data", data)
        } catch (e: JSONException) {
            Log.e("JsonApiResource", "update: ", e)
        }
        return result.toString()
    }
}
