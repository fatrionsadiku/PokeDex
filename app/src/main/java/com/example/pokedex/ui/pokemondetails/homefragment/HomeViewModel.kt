package com.example.pokedex.ui.pokemondetails.homefragment

import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.data.Repository
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.data.models.PokemonResult
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.Utility.PAGE_SIZE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
): ViewModel() {
    val pokemonResponse = MutableLiveData<Resource<List<PokemonResult>>>()
    private var currentPokemonList : MutableList<PokemonResult>? = null
    private var currentPokemonPage = 0
    val totalNumberOfFavs = repository.getTotalNumberOfFavs().asLiveData()
    val favoritePokemons = repository.getFavoritePokemons().asLiveData()
    var recyclerViewState : Parcelable? = null


    override fun onCleared() {
        super.onCleared()
        Log.d("ViewModelD", "DED RIP!")
    }

    init {
        getPaginatedPokemon()
        Log.d("ViewModelD", "REBORN HAHAH")
    }

    fun getPaginatedPokemon() = viewModelScope.launch {
        pokemonResponse.postValue(Resource.Loading())
        val response = repository.getPaginatedPokemons(PAGE_SIZE, currentPokemonPage * PAGE_SIZE)
        pokemonResponse.postValue(handlePaginatedPokemonResponse(response))
    }

    private fun handlePaginatedPokemonResponse(response : Response<PokemonsApiResult>) : Resource<List<PokemonResult>> {
        if(response.isSuccessful){
            currentPokemonPage++
            response.body()?.let {
                if (currentPokemonList == null){
                    currentPokemonList = it.results
                }
                else{
                    val currentPokeList = currentPokemonList
                    val newPokemonList = it.results
                    currentPokeList?.addAll(newPokemonList)
                }
                return Resource.Success(currentPokemonList)
            }
        }
        return Resource.Error(message = response.message())
    }

    fun favoritePokemon(pokemon: FavoritePokemon) = viewModelScope.launch {
        repository.favoritePokemon(pokemon)
    }

    suspend fun doesPokemonExist(pokeName : String) : Boolean = withContext(Dispatchers.IO) {
        repository.doesPokemonExist(pokeName)
    }
    fun unFavoritePokemon(pokemon: FavoritePokemon) = viewModelScope.launch {
        repository.unFavoritePokemon(pokemon)
    }


    fun filterPokemonByName(query : CharSequence?, adapter : PokeAdapter){
        this@HomeViewModel.pokemonResponse.value?.let {pokemons ->
            adapter.pokemons = (pokemons.data?.filter {
                val charSequence: CharSequence = query?.toString() ?: ""
                it.name.contains(charSequence,true)
            } ?: pokemons.data)!!
        }
    }
}

