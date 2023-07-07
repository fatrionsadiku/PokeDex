package com.example.pokedex.ui.homefragment

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.repositories.DatabaseRepository
import com.example.pokedex.repositories.NetworkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    networkRepository: NetworkRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel() {
    val pokemonResponse = networkRepository.getCachedPokemons().asLiveData()
    val totalNumberOfFavs = databaseRepository.getTotalNumberOfFavs().asLiveData()
    val favoritePokemons = databaseRepository.getFavoritePokemons().asLiveData()
    val doesDatabaseHaveITems = databaseRepository.doesDatabaseHaveItems().asLiveData()
    val doesCachedPokemonDatabaseHaveItems = databaseRepository.doesCachedDatabaseHaveItems().asLiveData()
    val currentPokemoneQuery = MutableStateFlow("")
    var recyclerViewState: Parcelable? = null
    val doesAdapterHaveItems = MutableLiveData(false)
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
            this@HomeViewModel.pokemonResponse.value?.let {pokemons ->
                adapter.pokemons = (pokemons.data?.filter {
                    val charSequence: CharSequence = currentPokeQuery
                    it.name.contains(charSequence,true)
                } ?: pokemons.data)!!
            }
        }
    }

}

