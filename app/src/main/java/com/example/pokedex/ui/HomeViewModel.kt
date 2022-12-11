package com.example.pokedex.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.Sprites
import com.example.pokedex.data.server.Repository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {
    val repository = Repository()
    val pokemon : MutableLiveData<Pokemon> = MutableLiveData()

    fun getPokemons(){
        repository.apiService.getPokemons().enqueue(object : Callback<Pokemon?> {
            override fun onResponse(call: Call<Pokemon?>, response: Response<Pokemon?>) {
                pokemon.value = response.body()
            }

            override fun onFailure(call: Call<Pokemon?>, t: Throwable) {
                throw java.lang.IllegalArgumentException(t)
            }
        })
    }
}