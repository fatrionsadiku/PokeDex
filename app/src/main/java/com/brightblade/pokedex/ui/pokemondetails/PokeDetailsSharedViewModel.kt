package com.brightblade.pokedex.ui.pokemondetails

import android.accounts.NetworkErrorException
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brightblade.pokedex.data.models.PokeAbilities
import com.brightblade.pokedex.data.models.PokeCharacteristics
import com.brightblade.pokedex.data.models.PokeHeldItems
import com.brightblade.pokedex.data.models.Pokemon
import com.brightblade.pokedex.data.models.PokemonEncounters
import com.brightblade.pokedex.data.models.PokemonEvolutionChain
import com.brightblade.pokedex.data.persistent.RedirectState
import com.brightblade.pokedex.data.persistent.UserPreferences
import com.brightblade.pokedex.repositories.NetworkRepository
import com.brightblade.utils.Resource
import com.brightblade.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.awaitResponse
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PokeDetailsSharedViewModel @Inject constructor(
    private val repository: NetworkRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val singlePokemonResponse = MutableLiveData<Resource<Pokemon>>()

    private val pokemonResponse = MutableLiveData<Pokemon?>()

    val abilitiesResponse = MutableLiveData<Resource<List<PokeAbilities?>>>()

    val pokemonSpeciesResponse = MutableLiveData<Resource<PokemonEvolutionChain?>>()

    val pokemonHeldItems = MutableLiveData<Resource<List<PokeHeldItems?>>>()

    val pokeCharacteristicsLiveData = MutableLiveData<PokeCharacteristics>()

    val pokeEncountersLiveData = MutableLiveData<Resource<List<PokemonEncounters>>>()

    val preferencesFlow = userPreferences.preferencesFlow

    var pokemonDescription = MutableLiveData<List<String>>()

    var pokemonName = MutableLiveData("")

    fun getSinglePokemonByName(pokemonId: Int) = viewModelScope.launch {
        singlePokemonResponse.postValue(Resource.Loading())
        pokeEncountersLiveData.postValue(Resource.Loading())
        try {
            val response = repository.getSinglePokemonByName(pokemonId)
            singlePokemonResponse.postValue(handleApiResponse(response))
            pokemonResponse.postValue(handleApiResponse(response).data)
            val abilities = getPokemonAbilitiesByName(response.body())
            abilitiesResponse.postValue(Resource.Success(abilities))
            pokemonHeldItems.postValue(Resource.Loading())
            val heldItems = getPokemonHeldItems(response.body())
            pokemonHeldItems.postValue(Resource.Success(heldItems))
            getPokemonCharacteristics(pokemonId)
            getPokemonSpeciesId(pokemonId)
            getPokemonEncounters(pokemonId)
        } catch (t: Throwable) {
            when (t) {
                is IOException           -> singlePokemonResponse.postValue(
                    Resource.Error(
                        data = null,
                        message = "An IO error has occurred"
                    )
                )

                is NetworkErrorException -> singlePokemonResponse.postValue(
                    Resource.Error(
                        data = null,
                        message = "No network connection"
                    )
                )
            }
        }
    }

    private fun getPokemonSpeciesId(id: Int) = viewModelScope.launch {
        val pokeSpecies = repository.getPokemonSpeciesId(id)
        if (pokeSpecies.isSuccessful) {
            Log.d("ViewModelDebug", "getPokemonSpecies: ${pokeSpecies.body()}")
            val filteredPokeDescriptions =
                pokeSpecies.body()?.textEntries?.filter { it.language.name == "en" }
            Timber.tag("PokeDescriptions").d(filteredPokeDescriptions.toString())
            val pokeDescriptions = listOf(
                filteredPokeDescriptions?.first()?.pokemonDescription ?: ""
            )
            pokemonDescription.postValue(pokeDescriptions)
            val currentPokeEvoId = Utility.getPokemonSpeciesId(pokeSpecies.body()?.evoChain?.url!!)
            Log.d("ViewModelDebug", "currentPokeEvoId: $currentPokeEvoId")
            Log.d("ViewModelDebug", "chainUrl: ${pokeSpecies.body()?.evoChain?.url!!}")
            getPokemonSpecies(currentPokeEvoId)
        }
    }

    private fun getPokemonSpecies(id: Int) = viewModelScope.launch {
        pokemonSpeciesResponse.postValue(Resource.Loading())
        val pokeSpecies = repository.getPokemonSpecies(id)
        if (pokeSpecies.isSuccessful) {
            Log.d("ViewModelDebug", "getPokemonSpecies: ${pokeSpecies.body()}")
            delay(300)
            pokemonSpeciesResponse.postValue(Resource.Success(pokeSpecies.body()))
        } else pokemonSpeciesResponse.postValue(Resource.Error(message = "Error while trying to fetch pokemon data"))
    }

    private fun getPokemonCharacteristics(id: Int) = viewModelScope.launch {
        val pokeCharacteristics = repository.getPokemonCharacteristics(id)
        if (pokeCharacteristics.isSuccessful) {
            pokeCharacteristicsLiveData.postValue(pokeCharacteristics.body())
        } else Timber.tag("PokeCharacteristics").e(pokeCharacteristics.message())
    }

    private fun getPokemonEncounters(id: Int) = viewModelScope.launch {
        val pokemonEncountersResponse = repository.getPokemonEncounters(id)
        if (pokemonEncountersResponse.isSuccessful) {
            delay(300)
            pokeEncountersLiveData.postValue(Resource.Success(pokemonEncountersResponse.body()))
            Timber.tag("PokeEncounters").d(pokemonEncountersResponse.body().toString())
        } else Timber.tag("PokeCharacteristics").e(pokemonEncountersResponse.message()).also {
            pokeEncountersLiveData.postValue(Resource.Error(message = pokemonEncountersResponse.message()))
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
        abilitiesResponse.postValue(Resource.Loading())
        pokemon?.abilities?.forEach {
            val currentPokemonAbilityName = it?.ability?.name?.trim()
            val pokeAbility = viewModelScope.async {
                repository.getPokemonAbility(currentPokemonAbilityName!!)
            }
            pokeAbilities.add(pokeAbility.await().body())

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

    fun onRedirectStateSelected(redirectState: RedirectState) = viewModelScope.launch {
        userPreferences.updateRedirectState(redirectState)
    }
}