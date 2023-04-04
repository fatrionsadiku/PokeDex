package com.example.pokedex.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.server.Retrofit
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.awaitResponse

class PokemonDetailsViewModel : ViewModel() {


    suspend fun getSinglePokemonByName(pokemonName: String): Pokemon? {
        return try {
            val pokemon = Retrofit.pokeApi.getPokemonByName(pokemonName)
            val pokeResponse = pokemon.awaitResponse()
            if (pokeResponse.isSuccessful) {
                pokeResponse.body()
            } else
                throw Exception("Failed to fetch pokemon data")
        } catch (e: Exception) {
            Log.e("PokeAPI", "Error fetching pokemon data", e)
            null
        }
    }
}