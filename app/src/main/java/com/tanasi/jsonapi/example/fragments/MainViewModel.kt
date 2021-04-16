package com.tanasi.jsonapi.example.fragments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanasi.jsonapi.JsonApiParams
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.jsonapi.example.models.Article
import com.tanasi.jsonapi.example.services.TestApiService
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    fun getArticles() = viewModelScope.launch {
        val response = TestApiService.build().getArticles(
            params = JsonApiParams(
                include = listOf("author")
            )
        )
        when (response) {
            is JsonApiResponse.Success -> {
                response.body.data!! // List<Article>
                response.body.data!!.first().title // JSON:API paints my bikeshed!
            }
            is JsonApiResponse.Error.ServerError -> TODO()
            is JsonApiResponse.Error.NetworkError -> TODO()
            is JsonApiResponse.Error.UnknownError -> TODO()
        }
    }

    fun getArticle(id: String) = viewModelScope.launch {
        val response = TestApiService.build().getArticle(
            id = id,
            params = JsonApiParams(
                include = listOf("author")
            )
        )
        when (response) {
            is JsonApiResponse.Success -> {
                response.body.data!! // Article object
                response.body.data!!.title // JSON:API paints my bikeshed!
                response.body.data!!.writer // Author object or null
            }
            is JsonApiResponse.Error.ServerError -> {
                response.body.errors.first().title // Value is too short
                response.body.errors.last().source!!.pointer // /data/attributes/password
            }
            is JsonApiResponse.Error.NetworkError -> {
                response.error // IOException
                Log.e("TAG", "getArticle: ", response.error)
            }
            is JsonApiResponse.Error.UnknownError -> {
                response.error // Throwable
                Log.e("TAG", "getArticle: ", response.error)
            }
        }
    }

    fun createArticle(article: Article) = viewModelScope.launch {
        article.putTitle(article.title)
        article.putWriter(article.writer!!)

        val response = TestApiService.build().createArticle(article)
        when (response) {
            is JsonApiResponse.Success -> {
                response.body.data!! // Article created
            }
            is JsonApiResponse.Error.ServerError -> TODO()
            is JsonApiResponse.Error.NetworkError -> TODO()
            is JsonApiResponse.Error.UnknownError -> TODO()
        }
    }
}