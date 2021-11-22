package com.tanasi.jsonapi.example.services

import com.tanasi.jsonapi.JsonApiParams
import com.tanasi.jsonapi.JsonApiResponse
import com.tanasi.jsonapi.callAdapter.JsonApiCallAdapterFactory
import com.tanasi.jsonapi.converter.JsonApiConverterFactory
import com.tanasi.jsonapi.example.models.Article
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.*

interface MainService {

    companion object {
        fun build(): MainService {
            val client = OkHttpClient.Builder().build()

            val retrofit = Retrofit.Builder()
                    .baseUrl("http://192.168.31.252:5000/")
                    .client(client)
                    .addCallAdapterFactory(JsonApiCallAdapterFactory.create())
                    .addConverterFactory(JsonApiConverterFactory.create())
                    .build()

            return retrofit.create(MainService::class.java)
        }
    }

    @GET("articles")
    suspend fun getArticles(@QueryMap params: JsonApiParams = JsonApiParams()): JsonApiResponse<List<Article>>

    @GET("articles/{id}")
    suspend fun getArticle(@Path("id") id: String, @QueryMap params: JsonApiParams = JsonApiParams()): JsonApiResponse<Article>

    @POST("articles")
    suspend fun createArticle(@Body article: Article): JsonApiResponse<Article>
}