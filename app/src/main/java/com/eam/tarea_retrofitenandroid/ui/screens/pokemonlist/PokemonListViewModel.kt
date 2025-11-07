package com.eam.tarea_retrofitenandroid.ui.screens.pokemonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eam.tarea_retrofitenandroid.model.Pokemon
import com.eam.tarea_retrofitenandroid.model.PokemonListItem
import com.eam.tarea_retrofitenandroid.repository.PokemonRepository
import com.eam.tarea_retrofitenandroid.repository.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PokemonListViewModel : ViewModel() {
    private val repository = PokemonRepository()

    private val _pokemonList = MutableStateFlow<List<PokemonListItem>>(emptyList())
    val pokemonList: StateFlow<List<PokemonListItem>> = _pokemonList.asStateFlow()

    private val _selectedPokemon = MutableStateFlow<Pokemon?>(null)
    val selectedPokemon: StateFlow<Pokemon?> = _selectedPokemon.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 20

    init {
        loadPokemonList()
    }

    fun loadPokemonList() {
        viewModelScope.launch {
            repository.getPokemonList(offset = 0, limit = pageSize).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Resource.Success -> {
                        _pokemonList.value = resource.data
                        _isLoading.value = false
                        currentOffset = pageSize
                    }
                    is Resource.Error -> {
                        _error.value = resource.message
                        _isLoading.value = false
                    }
                }
            }
        }
    }

    fun loadMorePokemon() {
        if (_isLoadingMore.value) return

        viewModelScope.launch {
            repository.getPokemonList(offset = currentOffset, limit = pageSize).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoadingMore.value = true
                    }
                    is Resource.Success -> {
                        _pokemonList.value = _pokemonList.value + resource.data
                        _isLoadingMore.value = false
                        currentOffset += pageSize
                    }
                    is Resource.Error -> {
                        _error.value = resource.message
                        _isLoadingMore.value = false
                    }
                }
            }
        }
    }

    fun loadPokemonDetail(name: String) {
        viewModelScope.launch {
            repository.getPokemon(name).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _isLoadingDetail.value = true
                    }
                    is Resource.Success -> {
                        _selectedPokemon.value = resource.data
                        _isLoadingDetail.value = false
                    }
                    is Resource.Error -> {
                        _error.value = resource.message
                        _isLoadingDetail.value = false
                    }
                }
            }
        }
    }

    fun clearSelectedPokemon() {
        _selectedPokemon.value = null
    }
}