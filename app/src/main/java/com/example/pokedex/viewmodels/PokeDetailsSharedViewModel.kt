package com.example.pokedex.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.server.PokeApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.awaitResponse
import javax.inject.Inject

@HiltViewModel
class PokeDetailsSharedViewModel @Inject constructor(
    private val pokeApi : PokeApiService
) : ViewModel() {

    val pokemonResponse = MutableLiveData<Pokemon>()
    private val cache = mutableMapOf<String, Pokemon>()

    suspend fun getSinglePokemonByName(pokemonName: String): Pokemon? {
        val cachedPokemon = cache[pokemonName]
        cachedPokemon?.let {
            pokemonResponse.postValue(it)
            return it
        }
        return try {
            val pokemon = pokeApi.getPokemonByName(pokemonName)
            val pokeResponse = pokemon.awaitResponse()
            if (pokeResponse.isSuccessful) {
                pokemonResponse.postValue(pokeResponse.body())
                cache[pokemonName] = pokeResponse.body()!!
                pokeResponse.body()
            } else
                throw Exception("Failed to fetch pokemon data")
        } catch (e: Exception) {
            Log.e("PokeAPI", "Error fetching pokemon data", e)
            null
        }
    }
}