package com.example.pokedex.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonResult
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.data.server.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse

class HomeViewModel : ViewModel() {
    val pokemons : MutableLiveData<MutableList<PokemonResult>> = MutableLiveData()


    fun getPaginatedPokemons(limit: Int) {
        val call = Retrofit.pokeApi.getPaginatedPokemons(limit)
        call.enqueue(object : Callback<PokemonsApiResult?> {
            override fun onResponse(
                call: Call<PokemonsApiResult?>,
                response: Response<PokemonsApiResult?>
            ) {
                if (response.isSuccessful) {
                    pokemons.postValue(response.body()?.results)
                }

            }

            override fun onFailure(call: Call<PokemonsApiResult?>, t: Throwable) {
                throw Exception(t.toString())
            }
        })
    }
}