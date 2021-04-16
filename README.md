# Retrofit JsonApi Converter: Android library for Android

Retrofit JsonApi Converter is a Android library for convert JSON:API response to model and model to JSON:API format

# Introduction

### Retrofit
[Retrofit](https://square.github.io/retrofit/) is a REST Client for Java and Android. It makes it relatively easy to retrieve and upload JSON (or other structured data) via a REST based webservice. In Retrofit you configure which converter is used for the data serialization. Typically for JSON you use GSon, but you can add custom converters to process XML or other protocols. Retrofit uses the OkHttp library for HTTP requests.

### JSON:API
[JSON:API](https://jsonapi.org/) is a specification for how a client should request that resources be fetched or modified, and how a server should respond to those requests.

JSON:API is designed to minimize both the number of requests and the amount of data transmitted between clients and servers. This efficiency is achieved without compromising readability, flexibility, or discoverability.

# Getting started

### Implement dependency
```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}

dependencies {
  implementation 'com.github.StanTanasi:retrofit-jsonapi-converter:LAST_VERSION'
}
```

### Setup
Add the following lines when creating the retrofit instance:
+ .addCallAdapterFactory(JsonApiCallAdapterFactory.create())
+ .addConverterFactory(JsonApiConverterFactory.create())
```kotlin
val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addCallAdapterFactory(JsonApiCallAdapterFactory.create())
                    .addConverterFactory(JsonApiConverterFactory.create())
                    .build()
```

# Usage

## Create model
Example json:
```json
{
  "links": {
    "self": "http://example.com/articles/1"
  },
  "data": {
    "type": "articles",
    "id": "1",
    "attributes": {
      "title": "JSON:API paints my bikeshed!"
    },
    "relationships": {
      "author": {
        "links": {
          "related": "http://example.com/articles/1/author"
        }
      }
    }
  }
}
```
Corresponding model:
```kotlin
@JsonApiType("articles")
data class Article(
    val id: String = "",
    var title: String = "",
    val author: Author? = null
) {
}
```
- Use class or data class, whichever you prefer.
- Use val or var, whichever you prefer.

Default value for property is recommended, in case attribute is not present inside json.

To have custom property name, you must add @JsonApiAttribute and/or @JsonApiRelationship annotations
```kotlin
@JsonApiType("articles")
data class Article(
    @JsonApiId val id: String = "",
    var title: String = "",
    @JsonApiRelationship("author") var writer: Author? = null
) {
}
```

If you need to send your model in request body, you will have to add: 
+ model extends JsonApiResource
+ use function putAttribute and putRelationship, to update resource
```kotlin
@JsonApiType("articles")
data class Article(
    @JsonApiId override var id: String = "",
    @JsonApiAttribute("title") var title: String = "",
    @JsonApiRelationship("author") var writer: Author? = null
) : JsonApiResource() {

    fun putTitle(title: String) = putAttribute("title", title)

    fun putWriter(writer: Author) = putRelationship("author", writer)
}
```

## Make a request

### API Service
```kotlin
@GET("articles")
suspend fun getArticles(@QueryMap params: JsonApiParams = JsonApiParams()): JsonApiResponse<List<Article>>

@GET("articles/{id}")
suspend fun getArticle(@Path("id") id: String, @QueryMap params: JsonApiParams = JsonApiParams()): JsonApiResponse<Article>

@POST("articles")
suspend fun createArticle(@Body article: Article): JsonApiResponse<Article>
```

#### JsonApiParams

Example
```kotlin
JsonApiParams(
    include = listOf("author"),
    fields = mapOf(
        "articles" to listOf("title", "author"),
        "people" to listOf("name")
    ),
    sort = listOf("title"),
    limit = 10,
    offset = 0,
    filter = mapOf("title" to listOf("JSON:API paints my bikeshed!", "title2"))
)
```

### Fetch a collection of articles
```kotlin
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
```

### Fetch an article
```kotlin
val response = TestApiService.build().getArticle(
    id = "1",
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
    is JsonApiResponse.Error.ServerError -> TODO()
    is JsonApiResponse.Error.NetworkError -> TODO()
    is JsonApiResponse.Error.UnknownError -> TODO()
}
```

### Create an article
```kotlin
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
```

## Response body
```kotlin
val response = TestApiService.build().getArticle(id)
when (response) {
    is JsonApiResponse.Success -> {
        response.headers // okhttp3.Headers
        response.code // Int = 200

        response.body.jsonApi?.version // String
        response.body.included // JSONArray
        response.body.links?.first // String
        response.body.meta // JSONObject

        response.body.raw // String
    }
    is JsonApiResponse.Error -> TODO()
}
```

# Error response

### Example
```json
{
  "jsonapi": { "version": "1.0" },
  "errors": [
    {
      "code":   "400",
      "source": { "pointer": "/data/attributes/firstName" },
      "title":  "Value is too short",
      "detail": "First name must contain at least three characters."
    },
    {
      "code":   "400",
      "source": { "pointer": "/data/attributes/password" },
      "title": "Passwords must contain a letter, number, and punctuation character.",
      "detail": "The password provided is missing a punctuation character."
    },
    {
      "code":   "400",
      "source": { "pointer": "/data/attributes/password" },
      "title": "Password and password confirmation do not match."
    }
  ]
}
```

#### Kotlin
```kotlin
val response = TestApiService.build().getArticle(id)
when (response) {
    is JsonApiResponse.Success -> TODO()
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
```

# Author
Lory-Stan TANASI - [GitHub](https://github.com/stantanasi)

# Disclaimer
This is not an official [Square product](https://square.github.io/).
