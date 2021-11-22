package com.tanasi.jsonapi.example.fragments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tanasi.jsonapi.JsonApiParams
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.jsonapi.example.models.Article
import com.tanasi.jsonapi.example.services.MainService
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

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
}