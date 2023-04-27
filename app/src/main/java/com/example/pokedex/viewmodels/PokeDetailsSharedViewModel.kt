package com.example.pokedex.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.Repository
import com.example.pokedex.data.models.PokeAbilities
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.server.PokeApiService
import com.example.pokedex.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.awaitResponse
import javax.inject.Inject

@HiltViewModel
class PokeDetailsSharedViewModel @Inject constructor(
    private val pokeApi : PokeApiService,
    private val repository: Repository
) : ViewModel() {

    val apiCallResponse = MutableLiveData<Resource<Pokemon>>()
    val pokemonResponse = MutableLiveData<Pokemon?>()
    val abilitiesResponse = MutableLiveData<List<PokeAbilities?>>()

    fun getSinglePokemonByName(pokemonName: String) = viewModelScope.launch {
        apiCallResponse.postValue(Resource.Loading())
        val response = repository.getSinglePokemonByName(pokemonName)
        apiCallResponse.postValue(handlePokemonApiCallResponse(response))
        val abilities = getPokemonAbilitiesByName(response.body())
        abilitiesResponse.postValue(abilities)
    }

    private suspend fun getPokemonAbilitiesByName(pokemon : Pokemon?) : MutableList<PokeAbilities?> {
        val pokeAbilities = mutableListOf<PokeAbilities?>()
        pokemon?.abilities?.forEach {
            val call = pokeApi.getPokemonAbilities(it?.ability?.name)
            val pokeAbility = call.awaitResponse()
            if (pokeAbility.isSuccessful){
                pokeAbilities.add(pokeAbility.body())
            }
        }
        return pokeAbilities
    }

    private fun handlePokemonApiCallResponse(response : Response<Pokemon>) : Resource<Pokemon> {
        if(response.isSuccessful){
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(message = response.message())
    }
}