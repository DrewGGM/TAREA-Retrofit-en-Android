package com.eam.tarea_retrofitenandroid

import com.eam.tarea_retrofitenandroid.model.PokemonListItem
import com.eam.tarea_retrofitenandroid.model.PokemonListResponse
import com.eam.tarea_retrofitenandroid.remote.PokeApiService
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.util.concurrent.TimeUnit

class IntegrationTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: PokeApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .build()

        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `prueba completa - obtener lista de Pokemon con Retrofit`() { runBlocking {
        val pokemonJson = """
            {
                "count": 1302,
                "next": "https://pokeapi.co/api/v2/pokemon?offset=20&limit=20",
                "previous": null,
                "results": [
                    {
                        "name": "pikachu",
                        "url": "https://pokeapi.co/api/v2/pokemon/25/"
                    },
                    {
                        "name": "charizard",
                        "url": "https://pokeapi.co/api/v2/pokemon/6/"
                    },
                    {
                        "name": "mewtwo",
                        "url": "https://pokeapi.co/api/v2/pokemon/150/"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(pokemonJson)
        )

        val response = apiService.getPokemonList(limit = 20, offset = 0)

        expectThat(response) {
            get { isSuccessful }.isTrue()
            get { code() }.isEqualTo(200)
            get { body() }.isNotNull()
        }

        val pokemonList: PokemonListResponse = response.body()!!
        expectThat(pokemonList) {
            get { count }.isEqualTo(1302)
            get { results }.isNotEmpty()
            get { results.size }.isEqualTo(3)
        }

        expectThat(pokemonList.results[0]) {
            get { name }.isEqualTo("pikachu")
            get { url }.isEqualTo("https://pokeapi.co/api/v2/pokemon/25/")
        }

        val pikachuId = pokemonList.results[0].extractId()
        expectThat(pikachuId).isEqualTo(25)
    } }

    @Test
    fun `verificar que Retrofit maneja correctamente errores HTTP`() { runBlocking {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("""{"error": "Internal Server Error"}""")
        )

        val response = apiService.getPokemonList()

        expectThat(response) {
            get { isSuccessful }.isEqualTo(false)
            get { code() }.isEqualTo(500)
        }
    } }

    @Test
    fun `verificar serialización y deserialización de modelo Pokemon`() { runBlocking {
        val charizardJson = """
            {
                "id": 6,
                "name": "charizard",
                "height": 17,
                "weight": 905,
                "sprites": {
                    "front_default": "https://example.com/charizard.png"
                },
                "types": [
                    {
                        "slot": 1,
                        "type": {
                            "name": "fire",
                            "url": "https://pokeapi.co/api/v2/type/10/"
                        }
                    },
                    {
                        "slot": 2,
                        "type": {
                            "name": "flying",
                            "url": "https://pokeapi.co/api/v2/type/3/"
                        }
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(charizardJson)
        )

        val response = apiService.getPokemon("charizard")

        expectThat(response.body()!!) {
            get { id }.isEqualTo(6)
            get { name }.isEqualTo("charizard")
            get { height }.isEqualTo(17)
            get { weight }.isEqualTo(905)
            get { types.size }.isEqualTo(2)
            get { types[0].type.name }.isEqualTo("fire")
            get { types[1].type.name }.isEqualTo("flying")
            get { sprites.front_default }.isEqualTo("https://example.com/charizard.png")
        }
    } }

    @Test
    fun `verificar extractId extrae correctamente el ID de la URL`() {
        val testCases = listOf(
            PokemonListItem("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/") to 1,
            PokemonListItem("pikachu", "https://pokeapi.co/api/v2/pokemon/25/") to 25,
            PokemonListItem("mewtwo", "https://pokeapi.co/api/v2/pokemon/150/") to 150,
        )

        testCases.forEach { (pokemon, expectedId) ->
            expectThat(pokemon.extractId()).isEqualTo(expectedId)
        }
    }
}