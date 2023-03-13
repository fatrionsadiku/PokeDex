package com.example.pokedex.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.data.server.Retrofit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {
    val pokemon: MutableLiveData<MutableList<Pokemon>> = MutableLiveData()
    val paginatedPokemon = MutableLiveData<PokemonsApiResult>()



//    init {
//        viewModelScope.launch {
//            getPaginatedPokemons(20)
//        }
//    }

    fun getPaginatedPokemons(limit: Int) = viewModelScope.launch {
        val call = Retrofit.pokeApi.getPaginatedPokemons(limit)
        call.enqueue(object : Callback<PokemonsApiResult?> {
            override fun onResponse(
                call: Call<PokemonsApiResult?>,
                response: Response<PokemonsApiResult?>
            ) {
                if (response.isSuccessful) {
                    val pokeList = mutableListOf<Pokemon>()
                    response.body()?.let { paginatedPokeResponse ->
                        paginatedPokeResponse.results.forEach {
                            val pokeId = it.url.replace(
                                "https://pokeapi.co/api/v2/pokemon/",
                                ""
                            ).replace("/", "").toInt()
                            getPokemon(pokeId, pokeList)
                        }
                    }
                    Log.d("PokeList", pokeList.toString())
                    pokemon.postValue(pokeList)
                }

            }

            override fun onFailure(call: Call<PokemonsApiResult?>, t: Throwable) {
                throw Exception(t.toString())
            }
        })
    }

    fun getPokemon(number: Int, pokeList: MutableList<Pokemon>) = viewModelScope.launch {
        val call = Retrofit.pokeApi.getPokemons(number)
        call.enqueue(object : Callback<Pokemon?> {
            override fun onResponse(call: Call<Pokemon?>, response: Response<Pokemon?>) {
                if (response.isSuccessful) {
                    Log.d("PokeResponse", response.body().toString())
                    response.body()?.let {
                        pokeList.add(it)
                    }
                }
            }
            override fun onFailure(call: Call<Pokemon?>, t: Throwable) {
                TODO("Not yet implemented")
            }
        })
    }

}