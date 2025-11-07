package com.eam.tarea_retrofitenandroid.repository

import com.eam.tarea_retrofitenandroid.model.PokemonListItem
import com.eam.tarea_retrofitenandroid.model.PokemonListResponse
import com.eam.tarea_retrofitenandroid.remote.PokeApiService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.io.IOException

class PokemonRepositoryTest {
    private lateinit var apiService: PokeApiService
    private lateinit var repository: PokemonRepository

    @Before
    fun setup() {
        apiService = mockk()
    }

    @Test
    fun `getPokemonList debe emitir Loading y luego Success`() = runTest {
        val mockPokemonList = listOf(
            PokemonListItem("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
            PokemonListItem("ivysaur", "https://pokeapi.co/api/v2/pokemon/2/"),
            PokemonListItem("venusaur", "https://pokeapi.co/api/v2/pokemon/3/")
        )
        val mockResponse = PokemonListResponse(
            count = 1302,
            next = null,
            previous = null,
            results = mockPokemonList
        )

        coEvery { apiService.getPokemonList(any(), any()) } returns Response.success(mockResponse)

        val testRepository = TestPokemonRepository(apiService)
        val emissions = testRepository.getPokemonList().toList()

        expectThat(emissions.size).isEqualTo(2)
        expectThat(emissions[0]).isA<Resource.Loading<*>>()
        expectThat(emissions[1]).isA<Resource.Success<*>>()

        (emissions[1] as Resource.Success<List<PokemonListItem>>).let { success ->
            expectThat(success.data.size).isEqualTo(3)
            expectThat(success.data[0].name).isEqualTo("bulbasaur")
            expectThat(success.data[1].name).isEqualTo("ivysaur")
            expectThat(success.data[2].name).isEqualTo("venusaur")
        }
    }

    @Test
    fun `getPokemonList debe emitir Error cuando la respuesta no es exitosa`() = runTest {
        coEvery { apiService.getPokemonList(any(), any()) } returns Response.error(
            404,
            "Not Found".toResponseBody()
        )

        val testRepository = TestPokemonRepository(apiService)
        val emissions = testRepository.getPokemonList().toList()

        expectThat(emissions.size).isEqualTo(2)
        expectThat(emissions[0]).isA<Resource.Loading<*>>()
        expectThat(emissions[1]).isA<Resource.Error<*>>()
    }

    @Test
    fun `getPokemonList debe emitir Error cuando hay excepción de red`() = runTest {
        coEvery { apiService.getPokemonList(any(), any()) } throws IOException("Network error")

        val testRepository = TestPokemonRepository(apiService)
        val emissions = testRepository.getPokemonList().toList()

        expectThat(emissions.size).isEqualTo(2)
        expectThat(emissions[0]).isA<Resource.Loading<*>>()
        expectThat(emissions[1]).isA<Resource.Error<*>>()

        (emissions[1] as Resource.Error<List<PokemonListItem>>).let { error ->
            expectThat(error.message).isEqualTo("Network error: Please check your connection")
        }
    }

    private class TestPokemonRepository(
        private val apiService: PokeApiService
    ) {
        fun getPokemonList() = flow {
            emit(Resource.Loading())
            try {
                val response = apiService.getPokemonList(limit = 20, offset = 0)
                if (response.isSuccessful) {
                    response.body()?.let {
                        emit(Resource.Success(it.results))
                    } ?: emit(Resource.Error("Response body is null"))
                } else {
                    emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
                }
            } catch (e: IOException) {
                emit(Resource.Error("Network error: Please check your connection"))
            } catch (e: Exception) {
                emit(Resource.Error("Unexpected error: ${e.message}"))
            }
        }.flowOn(Dispatchers.IO)
    }
}