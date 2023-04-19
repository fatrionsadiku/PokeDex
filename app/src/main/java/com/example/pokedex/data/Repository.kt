package com.example.pokedex.data

import com.example.pokedex.data.models.Pokemon
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

    suspend fun getPaginatedPokemons(limit : Int) : Response<PokemonsApiResult> {
        val response = try {
            pokeApi.getPaginatedPokemons(limit)
        }catch (e : Exception){
            throw Exception(e.toString())
        }
        return response
    }
}