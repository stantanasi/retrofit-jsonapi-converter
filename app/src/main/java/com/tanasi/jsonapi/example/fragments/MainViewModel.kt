package com.tanasi.jsonapi.example.fragments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanasi.jsonapi.JsonApiParams
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.jsonapi.example.models.Article
import com.tanasi.jsonapi.example.services.MainService
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    companion object {
        const val TAG = "MainViewModel"
    }

    fun getArticles() = viewModelScope.launch {
        val response = MainService.build().getArticles(
            params = JsonApiParams(
                include = listOf("author")
            )
        )
        when (response) {
            is JsonApiResponse.Success -> {
                response.body.data!! // List<Article>
                response.body.data!!.first().title // JSON:API paints my bikeshed!
            }
            else -> TODO()
        }
    }

    fun getArticle(id: String) = viewModelScope.launch {
        val response = MainService.build().getArticle(
            id = id,
            params = JsonApiParams(
                include = listOf("author")
            )
        )
        when (response) {
            is JsonApiResponse.Success -> {
                response.body.data!! // Article object
                response.body.data!!.title // JSON:API paints my bikeshed!
                response.body.data!!.author // Author object or null
            }
            else -> TODO()
        }
    }

    fun createArticle(article: Article) = viewModelScope.launch {
        val response = MainService.build().createArticle(
            article = article
        )
        when (response) {
            is JsonApiResponse.Success -> {
                response.body.data!! // Article created
            }
            else -> TODO()
        }
    }

    fun test() = viewModelScope.launch {
        val response = MainService.build().getArticle(
            id = "1"
        )
        when (response) {
            is JsonApiResponse.Success -> {
                response.headers // okhttp3.Headers
                response.code // Int (e.g., 2xx)

                response.body.jsonApi?.version // String (e.g., "1.0")
                response.body.included // JSONArray
                response.body.links?.first // String (e.g., "http://example.com/...")
                response.body.meta // JSONObject

                response.body.raw // String (e.g., " {"data":{"type":"articles", ... ")
            }
            is JsonApiResponse.Error.ServerError -> {
                response.body.errors.forEach {
                    it.id // String
                    it.links?.about // String
                    it.status // String
                    it.code // String
                    it.title // String
                    it.detail // String
                    it.source?.pointer // String
                    it.source?.parameter // String
                    it.meta // String
                }
            }
            is JsonApiResponse.Error.NetworkError -> {
                Log.e(
                    TAG,
                    "Network error: ",
                    response.error // IOException
                )
            }
            is JsonApiResponse.Error.UnknownError -> {
                Log.e(
                    TAG,
                    "Unknown error: ",
                    response.error // Throwable
                )
            }
        }
    }
}