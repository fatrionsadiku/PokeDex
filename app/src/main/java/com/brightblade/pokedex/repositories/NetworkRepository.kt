package com.brightblade.pokedex.repositories

import androidx.room.withTransaction
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDao.CachedPokemonsDao
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDatabase
import com.brightblade.pokedex.data.models.PokeHeldItems
import com.brightblade.pokedex.data.models.Pokemon
import com.brightblade.pokedex.data.models.PokemonEvolutionChain
import com.brightblade.pokedex.data.models.PokemonSpecies
import com.brightblade.pokedex.data.network.PokeApiService
import com.brightblade.pokedex.data.persistent.SortOrder
import com.brightblade.pokedex.di.ApplicationScope
import com.brightblade.pokedex.utils.networkBoundResource
import kotlinx.coroutines.delay
import retrofit2.Call
import retrofit2.Response
import javax.inject.Inject

@ApplicationScope
class NetworkRepository @Inject constructor(
    private val pokeApi : PokeApiService,
    private val cachedPokemonsDao: CachedPokemonsDao,
    private val pokemonDatabase: PokemonDatabase
) {
    fun getCachedPokemons(sortOrder: SortOrder) = networkBoundResource(
        query = {
            cachedPokemonsDao.getPokemons(sortOrder)
        },
        fetch = {
            delay(1000)
            pokeApi.getPaginatedPokemons(1273, 0)
        },
        saveFetchResult = { pokeResponse ->
            pokemonDatabase.withTransaction {
                cachedPokemonsDao.deletePokemons()
                cachedPokemonsDao.insertPokemons(pokeResponse.results.toList())
            }
        },
        shouldFetch = { pokeList ->
            pokeList.isEmpty()
        }
    )
    suspend fun getSinglePokemonByName(pokemonName: String): Response<Pokemon> {
        val response = try {
            pokeApi.getPokemonByName(pokemonName)
        } catch (e: Exception) {
            throw Exception(e.toString())
        }
        return response
    }
    suspend fun getPokemonSpecies(id: Int): Response<PokemonEvolutionChain> {
        val response = try {
            pokeApi.getPokemonSpecies(id)
        } catch (e: Exception) {
            throw Exception(e.toString())
        }
        return response
    }

    suspend fun getPokemonSpeciesId(id: Int): Response<PokemonSpecies> {
        val response = try {
            pokeApi.getPokemonSpeciesId(id)
        } catch (e: Exception) {
            throw Exception(e.toString())
        }
        return response
    }

    fun getPokemonHeldItems(name: String): Call<PokeHeldItems> {
        val response = try {
            pokeApi.getPokemonHeldItems(name)
        } catch (e: Exception) {
            throw Exception(e.toString())
        }
        return response
    }
}