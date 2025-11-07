package com.eam.tarea_retrofitenandroid.remote

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class PokeApiServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: PokeApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getPokemonList debe retornar lista exitosamente`() { runBlocking {
        val mockResponse = """
            {
                "count": 1302,
                "next": "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                "previous": null,
                "results": [
                    {
                        "name": "bulbasaur",
                        "url": "https://pokeapi.co/api/v2/pokemon/1/"
                    },
                    {
                        "name": "ivysaur",
                        "url": "https://pokeapi.co/api/v2/pokemon/2/"
                    },
                    {
                        "name": "venusaur",
                        "url": "https://pokeapi.co/api/v2/pokemon/3/"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        val response = apiService.getPokemonList(limit = 20)

        expectThat(response.isSuccessful).isTrue()
        expectThat(response.body()).isNotNull()

        response.body()?.let { body ->
            expectThat(body.results.size).isEqualTo(3)
            expectThat(body.results[0].name).isEqualTo("bulbasaur")
            expectThat(body.results[1].name).isEqualTo("ivysaur")
            expectThat(body.results[2].name).isEqualTo("venusaur")
            expectThat(body.count).isEqualTo(1302)
        }
    } }

    @Test
    fun `getPokemon debe retornar pokemon específico exitosamente`() { runBlocking {
        val mockResponse = """
            {
                "id": 25,
                "name": "pikachu",
                "height": 4,
                "weight": 60,
                "sprites": {
                    "front_default": "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"
                },
                "types": [
                    {
                        "slot": 1,
                        "type": {
                            "name": "electric",
                            "url": "https://pokeapi.co/api/v2/type/13/"
                        }
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )

        val response = apiService.getPokemon("pikachu")

        expectThat(response.isSuccessful).isTrue()
        expectThat(response.body()).isNotNull()

        response.body()?.let { pokemon ->
            expectThat(pokemon.id).isEqualTo(25)
            expectThat(pokemon.name).isEqualTo("pikachu")
            expectThat(pokemon.height).isEqualTo(4)
            expectThat(pokemon.weight).isEqualTo(60)
            expectThat(pokemon.types.size).isEqualTo(1)
            expectThat(pokemon.types[0].type.name).isEqualTo("electric")
            expectThat(pokemon.sprites.front_default).isEqualTo("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png")
        }
    } }

    @Test
    fun `getPokemonList debe manejar error 404 correctamente`() { runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("""{"error": "Not found"}""")
        )

        val response = apiService.getPokemonList()

        expectThat(response.isSuccessful).isEqualTo(false)
        expectThat(response.code()).isEqualTo(404)
    } }
}