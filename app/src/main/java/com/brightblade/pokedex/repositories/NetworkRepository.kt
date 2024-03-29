package com.brightblade.pokedex.repositories

import androidx.room.withTransaction
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDao.CachedPokemonsDao
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDatabase
import com.brightblade.pokedex.data.models.PokeAbilities
import com.brightblade.pokedex.data.models.PokeCharacteristics
import com.brightblade.pokedex.data.models.PokeHeldItems
import com.brightblade.pokedex.data.models.Pokemon
import com.brightblade.pokedex.data.models.PokemonEncounters
import com.brightblade.pokedex.data.models.PokemonEvolutionChain
import com.brightblade.pokedex.data.models.PokemonSpecies
import com.brightblade.pokedex.data.network.PokeApiService
import com.brightblade.pokedex.data.persistent.SortOrder
import com.brightblade.pokedex.di.ApplicationScope
import com.brightblade.utils.Utility.MAX_POKEMON_SIZE
import com.brightblade.utils.networkBoundResource
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
            delay(500)
            pokeApi.getPaginatedPokemons(MAX_POKEMON_SIZE, 0)
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

    suspend fun getSinglePokemonByName(pokemonId: Int): Response<Pokemon> {
        val response = try {
            pokeApi.getPokemonByName(pokemonId)
        } catch (e: Exception) {
            throw Exception(e.toString())
        }
        return response
    }

    suspend fun getPokemonAbility(pokemonAbilityName: String): Response<PokeAbilities> {
        val response = try {
            pokeApi.getPokemonAbilities(pokemonAbilityName)
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

    suspend fun getPokemonCharacteristics(id: Int): Response<PokeCharacteristics> {
        val response = try {
            pokeApi.getPokemonCharacteristics(id)
        } catch (e: Exception) {
            throw Exception(e.toString())
        }
        return response
    }

    suspend fun getPokemonEncounters(id: Int): Response<List<PokemonEncounters>> {
        val response = try {
            pokeApi.getPokemonEncounters(id)
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