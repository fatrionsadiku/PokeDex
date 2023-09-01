package com.brightblade.pokedex.ui.pokemondetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.repositories.DatabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PokemonDatabaseViewModel @Inject constructor(
    private val databaseRepository: DatabaseRepository,
) : ViewModel() {

    val isPokemonFavoritedState = MutableLiveData<Boolean>()
    fun favoritePokemon(pokemon: FavoritePokemon) = viewModelScope.launch {
        databaseRepository.favoritePokemon(pokemon)
    }

    fun unFavoritePokemon(pokemon: FavoritePokemon) = viewModelScope.launch {
        databaseRepository.unFavoritePokemon(pokemon)
    }

    suspend fun doesPokemonExist(pokeName: String, shouldPostValue: Boolean = false): Boolean =
        withContext(Dispatchers.IO) {
            val isPokemonFavorited = databaseRepository.doesPokemonExist(pokeName)
            if (shouldPostValue) isPokemonFavoritedState.postValue(isPokemonFavorited)
            isPokemonFavorited
        }
}

