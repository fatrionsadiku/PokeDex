package com.example.pokedex.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.data.Repository
import com.example.pokedex.data.models.PokemonResult
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    val pokemonResponse = MutableLiveData<Resource<List<PokemonResult>>>()

    init {
        getPaginatedPokemons(Utility.PAGE_SIZE)
    }

    fun getPaginatedPokemons(limit: Int) = viewModelScope.launch {
        pokemonResponse.postValue(Resource.Loading())
        val response = repository.getPaginatedPokemons(limit)
        pokemonResponse.postValue(handlePaginatedPokemonsResponse(response))
    }

    private fun handlePaginatedPokemonsResponse(response : Response<PokemonsApiResult>) : Resource<List<PokemonResult>> {
        if(response.isSuccessful){
            response.body()?.let {
                return Resource.Success(it.results)
            }
        }
        return Resource.Error(message = response.message())
    }


    fun filterPokemonsByName(query : CharSequence?,adapter : PokeAdapter){
        this@HomeViewModel.pokemonResponse.value?.let {pokemons ->
            adapter.pokemons = (pokemons.data?.filter {
                val charSequence: CharSequence = query?.toString() ?: ""
                it.name.contains(charSequence,true)
            } ?: pokemons.data)!!
        }
    }
}

