package com.example.pokedex.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pokedex.data.RedirectState
import com.example.pokedex.data.Repository
import com.example.pokedex.data.UserPreferences
import com.example.pokedex.data.models.PokeAbilities
import com.example.pokedex.data.models.PokeHeldItems
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonEvolutionChain
import com.example.pokedex.data.server.PokeApiService
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.awaitResponse
import javax.inject.Inject

@HiltViewModel
class PokeDetailsSharedViewModel @Inject constructor(
    private val pokeApi: PokeApiService,
    private val repository: Repository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val apiCallResponse = MutableLiveData<Resource<Pokemon>>()
    val pokemonResponse = MutableLiveData<Pokemon?>()
    val abilitiesResponse = MutableLiveData<List<PokeAbilities?>>()
    val pokemonSpeciesResponse = MutableLiveData<PokemonEvolutionChain?>()
    val pokemonHeldItems = MutableLiveData<Resource<List<PokeHeldItems?>>>()
    val preferencesFlow = userPreferences.preferencesFlow
    val favoritePokemonsFlow = repository.getFavoritePokemons()

    fun getSinglePokemonByName(pokemonName: String) = viewModelScope.launch {
        apiCallResponse.postValue(Resource.Loading())
        val response = repository.getSinglePokemonByName(pokemonName)
        apiCallResponse.postValue(handleApiResponse(response))
        val abilities = getPokemonAbilitiesByName(response.body())
        abilitiesResponse.postValue(abilities)
        pokemonHeldItems.postValue(Resource.Loading())
        val heldItems = getPokemonHeldItems(response.body())
        pokemonHeldItems.postValue(Resource.Success(heldItems))
    }

    fun getPokemonSpeciesId(id: Int) = viewModelScope.launch {
        val pokeSpecies = repository.getPokemonSpeciesId(id)
        if (pokeSpecies.isSuccessful) {
            Log.d("ViewModelDebug", "getPokemonSpecies: ${pokeSpecies.body()}")
            val currentPokeEvoId = Utility.getPokemonSpeciesId(pokeSpecies.body()?.evoChain?.url!!)
            getPokemonSpecies(currentPokeEvoId)
        }
    }

    private fun getPokemonSpecies(id: Int) = viewModelScope.launch {
        val pokeSpecies = repository.getPokemonSpecies(id)
        if (pokeSpecies.isSuccessful) {
            Log.d("ViewModelDebug", "getPokemonSpecies: ${pokeSpecies.body()}")
            pokemonSpeciesResponse.postValue(pokeSpecies.body())
        }
    }

    private suspend fun getPokemonHeldItems(pokemon: Pokemon?): List<PokeHeldItems?> {
        val pokeHeldItems = mutableListOf<PokeHeldItems?>()
        pokemon?.heldItems?.forEach { heldItems ->
            val pokeHeldItemsApiCall = repository.getPokemonHeldItems(heldItems.item.name)
            val heldItemsResponse = pokeHeldItemsApiCall.awaitResponse()
            if (heldItemsResponse.isSuccessful) {
                Log.d("ViewModelDebug", "getPokemonSpecies: ${heldItemsResponse.body()}")
                pokeHeldItems.add(heldItemsResponse.body())
            }
        }
        return pokeHeldItems.toList()
    }


    private suspend fun getPokemonAbilitiesByName(pokemon: Pokemon?): MutableList<PokeAbilities?> {
        val pokeAbilities = mutableListOf<PokeAbilities?>()
        pokemon?.abilities?.forEach {
            val call = pokeApi.getPokemonAbilities(it?.ability?.name)
            val pokeAbility = call.awaitResponse()
            if (pokeAbility.isSuccessful) {
                pokeAbilities.add(pokeAbility.body())
            }
        }
        return pokeAbilities
    }

    private fun <T> handleApiResponse(response: Response<T>): Resource<T> {
        return if (response.isSuccessful) {
            response.body()?.let {
                Resource.Success(it)
            } ?: Resource.Error(message = "Empty response body")
        } else {
            Resource.Error(message = response.message())
        }
    }




    fun onRedirectStateSelecetd(redirectState: RedirectState) = viewModelScope.launch {
        userPreferences.updateRedirectState(redirectState)
    }
}