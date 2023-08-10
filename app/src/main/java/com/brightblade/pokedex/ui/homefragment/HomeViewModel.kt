package com.brightblade.pokedex.ui.homefragment

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.data.models.PokemonResult
import com.brightblade.pokedex.data.persistent.PokemonPhotoTypes
import com.brightblade.pokedex.data.persistent.SortOrder
import com.brightblade.pokedex.data.persistent.UserPreferences
import com.brightblade.pokedex.repositories.DatabaseRepository
import com.brightblade.pokedex.repositories.NetworkRepository
import com.brightblade.pokedex.ui.adapters.PokeAdapter
import com.brightblade.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val networkRepository: NetworkRepository,
    private val databaseRepository: DatabaseRepository,
    private val preferences: UserPreferences
) : ViewModel() {
    private val sortOrderFlow = preferences.sortOrderFlow

    private val _pokemonResponse = MutableLiveData<Resource<List<PokemonResult>>>()
    val pokemonResponse
        get() = _pokemonResponse

    private val _totalNumberOfFavs = databaseRepository.getTotalNumberOfFavs().asLiveData()
    val totalNumberOfFavs
        get() = _totalNumberOfFavs

    private val _favoritePokemons = databaseRepository.getFavoritePokemons().asLiveData()
    val favoritePokemons
        get() = _favoritePokemons

    private val _doesDatabaseHaveITems = databaseRepository.doesDatabaseHaveItems().asLiveData()
    val doesDatabaseHaveItems
        get() = _doesDatabaseHaveITems

    val pokemonPhotoTypeFlow = preferences.pokemonPhotoTypeFlow

    val pokemonSortOrderFlow = preferences.sortOrderFlow

    val currentPokemoneQuery = MutableStateFlow("")

    var recyclerViewState: Parcelable? = null

    val doesAdapterHaveItems = MutableLiveData(false)

    init {
        viewModelScope.launch { Log.d("ViewModelSort", sortOrderFlow.first().sortOrder.name) }
        viewModelScope.launch {
            getPokemonResponse(sortOrderFlow.first().sortOrder)
        }
    }

    private fun getPokemonResponse(sortOrder: SortOrder) = viewModelScope.launch {
        networkRepository.getCachedPokemons(sortOrder).collectLatest {
            _pokemonResponse.postValue(it)
        }
    }

    fun onSortOrderChanged(sortOrder: SortOrder) = viewModelScope.launch {
        preferences.updateSortOrder(sortOrder)
        getPokemonResponse(sortOrder)
    }
    fun onPokemonPhotoTypeSelected(pokemonPhotoTypes: PokemonPhotoTypes) = viewModelScope.launch {
        preferences.updatePokemonPhotoType(pokemonPhotoTypes)
    }
    fun favoritePokemon(pokemon: FavoritePokemon) = viewModelScope.launch {
        databaseRepository.favoritePokemon(pokemon)
    }
    fun unFavoritePokemon(pokemon: FavoritePokemon) = viewModelScope.launch {
        databaseRepository.unFavoritePokemon(pokemon)
    }
    suspend fun doesPokemonExist(pokeName: String): Boolean = withContext(Dispatchers.IO) {
        databaseRepository.doesPokemonExist(pokeName)
    }
    fun filterPokemonByName(adapter: PokeAdapter) = viewModelScope.launch {
        currentPokemoneQuery.collectLatest {currentPokeQuery ->
            this@HomeViewModel._pokemonResponse.value?.let { pokemons ->
                adapter.pokemons = (pokemons.data?.filter {
                    val charSequence: CharSequence = currentPokeQuery
                    it.name.contains(charSequence, true)
                } ?: pokemons.data)!!
            }
        }
    }

}

