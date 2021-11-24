<h1 align="center">Retrofit JSON:API Converter</h1>

<p align="center">
  <img src="https://jsonapi.org/images/jsonapi.png" height="100px" />
  <br />
  A Retrofit converter for JSON:API specification.
  <br />
  <a href="https://jitpack.io/#stantanasi/retrofit-jsonapi-converter">
    <strong>Implement library »</strong>
  </a>
  <br />
  <br />
  <a href="https://github.com/stantanasi/retrofit-jsonapi-converter/issues">Report Bug</a>
  ·
  <a href="https://github.com/stantanasi/retrofit-jsonapi-converter/issues">Request Feature</a>
</p>

<details>
  <summary>Table of Contents</summary>

  * [About the project](#about-the-project)
    * [Built with](#built-with)
  * [Getting started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Setup](#setup)
  * [Usage](#usage)
    * [JSON:API response object](#jsonapi-response-object)
    * [Setting the models](#setting-the-models)
      * [Request body](#request-body)
      * [Multi-type relationship](#multi-type-relationship)
    * [Define the endpoints](#define-the-endpoints)
      * [JsonApiParams](#jsonapiparams)
      * [JsonApiResponse](#jsonapiresponse)
    * [Make the request](#make-the-request)
      * [Fetch a collection](#fetch-a-collection)
      * [Fetch a resource](#fetch-a-resource)
      * [Create a resource](#create-a-resource)
  * [Contributing](#contributing)
  * [Author](#author)
  * [License](#license)
</details>


## About the project

Retrofit JSON:API Converter is a Retrofit converter for JSON:API specification

JSON:API is a specification for how a client should request that resources be fetched or modified, and how a server should respond to those requests.

JSON:API is designed to minimize both the number of requests and the amount of data transmitted between clients and servers. This efficiency is achieved without compromising readability, flexibility, or discoverability.

This is not an official [Square product](https://square.github.io).

### Built with

- [Kotlin](https://kotlinlang.org)
- [Retrofit](https://square.github.io/retrofit)
- [JSON:API specification](https://jsonapi.org)


## Getting started

### Prerequisites

Inside your root `build.gradle`, add the JitPack maven repository to the list of repositories:

```gradle
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```

Inside your module `build.gradle`, implement library [latest version](https://jitpack.io/#stantanasi/retrofit-jsonapi-converter):

```gradle
dependencies {
  ...
  implementation 'com.github.stantanasi:retrofit-jsonapi-converter:LAST_VERSION'
}
```

### Setup

Add the following lines when creating the retrofit instance:
- **addCallAdapterFactory(JsonApiCallAdapterFactory.create())**
- **addConverterFactory(JsonApiConverterFactory.create())**

```kotlin
val retrofit = Retrofit.Builder()
  .baseUrl("http://example.com/")
  .addCallAdapterFactory(JsonApiCallAdapterFactory.create())
  .addConverterFactory(JsonApiConverterFactory.create())
  .build()
```


## Usage

### JSON:API response object

Let's suppose you have an API that returns the following response:

```json
{
  "data": {
    "type": "articles",
    "id": "1",
    "attributes": {
      "title": "JSON:API paints my bikeshed!"
    },
    "links": {
      "self": "http://example.com/articles/1"
    },
    "relationships": {
      "author": {
        "links": {
          "self": "http://example.com/articles/1/relationships/author",
          "related": "http://example.com/articles/1/author"
        },
        "data": {
          "type": "people",
          "id": "9"
        }
      },
      "comments": {
        "links": {
          "self": "http://example.com/articles/1/relationships/comments",
          "related": "http://example.com/articles/1/comments"
        },
        "data": [
          {
            "type": "comments",
            "id": "5"
          },
          {
            "type": "comments",
            "id": "12"
          }
        ]
      }
    }
  },
  "included": [
    {
      "type": "people",
      "id": "9",
      "attributes": {
        "first-name": "Dan",
        "last-name": "Gebhardt",
        "twitter": "dgeb"
      },
      "links": {
        "self": "http://example.com/people/9"
      }
    },
    {
      "type": "comments",
      "id": "5",
      "attributes": {
        "body": "First!"
      },
      "relationships": {
        "author": {
          "data": {
            "type": "people",
            "id": "2"
          }
        }
      },
      "links": {
        "self": "http://example.com/comments/5"
      }
    },
    {
      "type": "comments",
      "id": "12",
      "attributes": {
        "body": "I like XML better"
      },
      "relationships": {
        "author": {
          "data": {
            "type": "people",
            "id": "9"
          }
        }
      },
      "links": {
        "self": "http://example.com/comments/12"
      }
    }
  ]
}
```

### Setting the models

You could create the models like this:

```kotlin
@JsonApiType("articles")
data class Article(
    var id: String? = null,
    var title: String = "",
    var author: People? = null,
    var comments: List<Comment> = listOf(),
)

@JsonApiType("people")
data class People(
    @JsonApiId var id: String,
    @JsonApiAttribute("first-name") val firstName: String,
    @JsonApiAttribute("last-name") val lastName: String,
    @JsonApiAttribute("twitter") val twitter: String = "",
)

@JsonApiType("comments")
data class Comment(
    @JsonApiId val id: String? = null,
    var body: String = "",
    var author: People? = null,
)
```

- Use class or data class, whichever you prefer.
- Use val or var, whichever you prefer.

To have custom property name, you must add @JsonApiAttribute and/or @JsonApiRelationship annotations.

Property with default value is recommended, in case attribute is not present inside json response.

Annotations @JsonApiAttribute and @JsonApiRelationship contains an "ignore" property wich ignore fields in request body

#### Request body

If you send your model inside a request, your model will be converte to JSON:API specification with **ALL** attributes and relationships.

If you only need to send specific attributes/relationships inside your request body, you have to:
- `implements JsonApiResource` to your model
- Add updated properties inside `dirtyProperties`

```kotlin
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
```

#### Multi-type relationship

```kotlin
@JsonApiType("people")
data class People(
    ...
    val books: List<Book> = listOf()
)

sealed class Book {
    @JsonApiType("dictionaries")
    data class Dictionaries(val id: String, val title: String) : Book()

    @JsonApiType("graphic-novels")
    data class GraphicNovel(val id: String, val name: String) : Book()
}
```

### Define the endpoints

With Retrofit 2, endpoints are defined inside of an interface using special retrofit annotations to encode details about the parameters and request method.

```kotlin
@GET("articles")
suspend fun getArticles(@QueryMap params: JsonApiParams = JsonApiParams()): JsonApiResponse<List<Article>>

@GET("articles/{id}")
suspend fun getArticle(@Path("id") id: String, @QueryMap params: JsonApiParams = JsonApiParams()): JsonApiResponse<Article>

@POST("articles")
suspend fun createArticle(@Body article: Article): JsonApiResponse<Article>

@DELETE("articles/{id}")
suspend fun deleteArticle(@Path("id") id: String): JsonApiResponse<Unit>
```

#### JsonApiParams

```kotlin
JsonApiParams(
    include = listOf<String>(),
    fields = mapOf<String, List<String>>(),
    sort = listOf<String>(),
    limit = 10,
    offset = 0,
    filter = mapOf<String, List<String>>()
)
```

#### JsonApiResponse

```kotlin
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
```

### Make the request

#### Fetch a collection

```kotlin
val response = MainService.build().getArticles(
    params = JsonApiParams(
        include = listOf("author")
    )
)
when (response) {
    is JsonApiResponse.Success -> {
        response.body.data?.forEach {
            it.title // String (e.g., JSON:API paints my bikeshed!)
        }
    }
    else -> TODO()
}
```

#### Fetch a resource

```kotlin
val response = MainService.build().getArticle(
    id = id,
    params = JsonApiParams(
        include = listOf("author")
    )
)
when (response) {
    is JsonApiResponse.Success -> {
        response.body.data // Article
        response.body.data?.title // String (e.g., JSON:API paints my bikeshed!)
        response.body.data?.author // People
    }
    else -> TODO()
}
```

#### Create a resource

```kotlin
val response = TestApiService.build().createArticle(
    article = Article(
        title = "test",
        author = People(
            id = "2"
        ),
        comments = listOf(
            Comment(
                "7"
            )
        )
    )
)
when (response) {
    is JsonApiResponse.Success -> {
        response.body.data // Article created
    }
    else -> TODO()
}
```


## Contributing

Contributions are what make the open source community such an amazing place to be learn, inspire, and create. Any contributions you make are **greatly appreciated**.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a pull request


## Author

- [Lory-Stan TANASI](https://github.com/stantanasi)


## License

This project is licensed under the `Apache-2.0` License - see the [LICENSE](LICENSE) file for details

<p align="center">
  <br />
  © 2021 Lory-Stan TANASI. All rights reserved
</p>