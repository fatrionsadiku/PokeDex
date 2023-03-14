package com.example.pokedex.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.data.server.Retrofit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class HomeViewModel : ViewModel() {
    val pokemon: MutableLiveData<MutableList<Pokemon?>> = MutableLiveData()


    fun getPaginatedPokemons(limit: Int)  {
        val pokeList = mutableListOf<Pokemon?>()
        val call = Retrofit.pokeApi.getPaginatedPokemons(limit)
        call.enqueue(object : Callback<PokemonsApiResult?> {
            override fun onResponse(
                call: Call<PokemonsApiResult?>,
                response: Response<PokemonsApiResult?>
            ) {
                if (response.isSuccessful) {
                    viewModelScope.launch {
                        response.body()?.let { paginatedPokeResponse ->
                            paginatedPokeResponse.results.forEach { Poke ->
                                val pokeId = Poke.url.replace(
                                    "https://pokeapi.co/api/v2/pokemon/",
                                    ""
                                ).replace("/", "").toInt()
                                val currentPoke = async(Dispatchers.IO){
                                    getPokemon(pokeId)
                                }
                                currentPoke.await()?.let {
                                    pokeList.add(it)
                                }
                            }
                            pokemon.postValue(pokeList)
                        }
                        Log.d("PokeList", pokeList.toString())
                    }

                }

            }

            override fun onFailure(call: Call<PokemonsApiResult?>, t: Throwable) {
                throw Exception(t.toString())
            }
        })
    }

    suspend fun getPokemon(number: Int): Pokemon? {
        return try {
            val call = Retrofit.pokeApi.getPokemons(number)
            val response = call.awaitResponse()
            if (response.isSuccessful) {
                Log.d("Current Pokemon :", response.body().toString())
                response.body()
            } else {
                throw Exception("Failed to fetch pokemon data")
            }
        } catch (e: Exception) {
            Log.e("PokeAPI", "Error fetching pokemon data", e)
            null
        }
    }

}