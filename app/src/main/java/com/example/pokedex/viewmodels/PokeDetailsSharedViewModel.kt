package com.example.pokedex.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.models.PokeAbilities
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.server.PokeApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.awaitResponse
import javax.inject.Inject

@HiltViewModel
class PokeDetailsSharedViewModel @Inject constructor(
    private val pokeApi : PokeApiService
) : ViewModel() {

    val pokemonResponse = MutableLiveData<Pokemon>()
    val abilitiesResponse = MutableLiveData<List<PokeAbilities?>>()
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
                val abilities = getPokemonAbilitiesByName(pokeResponse.body())
                abilitiesResponse.postValue(abilities)
                cache[pokemonName] = pokeResponse.body()!!
                pokeResponse.body()
            } else
                throw Exception("Failed to fetch pokemon data")
        } catch (e: Exception) {
            Log.e("PokeAPI", "Error fetching pokemon data", e)
            null
        }
    }

    private suspend fun getPokemonAbilitiesByName(pokemon : Pokemon?) : MutableList<PokeAbilities?> {
        val pokeAbilities = mutableListOf<PokeAbilities?>()
        pokemon?.abilities?.forEach {
            val call = pokeApi.getPokemonAbilities(it.ability.name)
            val pokeAbility = call.awaitResponse()
            if (pokeAbility.isSuccessful){
                pokeAbilities.add(pokeAbility.body())
            }
        }
        return pokeAbilities
    }
}