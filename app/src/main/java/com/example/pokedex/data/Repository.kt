package com.example.pokedex.data

import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonEvolutionChain
import com.example.pokedex.data.models.PokemonSpecies
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.data.server.PokeApiService
import com.example.pokedex.di.ApplicationScope
import retrofit2.Response
import javax.inject.Inject


@ApplicationScope
class Repository @Inject constructor(
    private val pokeApi : PokeApiService
) {
    suspend fun getSinglePokemonByName(pokemonName: String): Response<Pokemon> {
        val response = try {
            pokeApi.getPokemonByName(pokemonName)
        }catch (e : Exception){
            throw Exception(e.toString())
        }
        return response
    }

    suspend fun getPaginatedPokemons(limit : Int, offset : Int) : Response<PokemonsApiResult> {
        val response = try {
            pokeApi.getPaginatedPokemons(limit,offset)
        }catch (e : Exception){
            throw Exception(e.toString())
        }
        return response
    }
    suspend fun getPokemonSpecies(id : Int) : Response<PokemonEvolutionChain> {
        val response = try {
            pokeApi.getPokemonSpecies(id)
        }catch (e : Exception){
            throw Exception(e.toString())
        }
        return response
    }
    suspend fun getPokemonSpeciesId(id : Int) : Response<PokemonSpecies> {
        val response = try {
            pokeApi.getPokemonSpeciesId(id)
        }catch (e : Exception){
            throw Exception(e.toString())
        }
        return response
    }
}