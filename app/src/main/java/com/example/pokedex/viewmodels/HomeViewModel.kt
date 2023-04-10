package com.example.pokedex.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.data.models.PokemonResult
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.data.server.PokeApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pokeApi : PokeApiService
): ViewModel() {
    val pokemons : MutableLiveData<MutableList<PokemonResult>> = MutableLiveData()

    fun getPaginatedPokemons(limit: Int) {
        val call = pokeApi.getPaginatedPokemons(limit)
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

    fun filterPokemonsByName(query : CharSequence?,adapter : PokeAdapter){
        this@HomeViewModel.pokemons.value?.let {pokemons ->
            adapter.pokemons = pokemons.filter {
                val charSequence: CharSequence = query?.toString() ?: ""
                it.name.contains(charSequence,true)
            }
        }
    }
}

