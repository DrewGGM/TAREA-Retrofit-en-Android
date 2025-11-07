package com.eam.tarea_retrofitenandroid.repository

import com.eam.tarea_retrofitenandroid.model.Pokemon
import com.eam.tarea_retrofitenandroid.model.PokemonListItem
import com.eam.tarea_retrofitenandroid.remote.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException

class PokemonRepository {
    fun getPokemonList(offset: Int = 0, limit: Int = 20): Flow<Resource<List<PokemonListItem>>> = flow {
        emit(Resource.Loading())
        try {
            val response = RetrofitInstance.api.getPokemonList(limit = limit, offset = offset)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it.results))
                } ?: emit(Resource.Error("Response body is null"))
            } else {
                emit(Resource.Error("Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Network error: Please check your connection"))
        } catch (e: Exception) {
            emit(Resource.Error("Unexpected error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    fun getPokemon(name: String): Flow<Resource<Pokemon>> = flow {
        emit(Resource.Loading())
        try {
            val response = RetrofitInstance.api.getPokemon(name)
            if (response.isSuccessful) {
                response.body()?.let {
                    emit(Resource.Success(it))
                } ?: emit(Resource.Error("Pokemon not found"))
            } else {
                emit(Resource.Error("Error loading Pokemon"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)
}

sealed class Resource<T> {
    class Loading<T> : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
}