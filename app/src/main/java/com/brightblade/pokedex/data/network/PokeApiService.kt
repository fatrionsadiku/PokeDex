package com.brightblade.pokedex.data.network

import com.brightblade.pokedex.data.models.PokeAbilities
import com.brightblade.pokedex.data.models.PokeHeldItems
import com.brightblade.pokedex.data.models.Pokemon
import com.brightblade.pokedex.data.models.PokemonEvolutionChain
import com.brightblade.pokedex.data.models.PokemonSpecies
import com.brightblade.pokedex.data.models.PokemonsApiResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {


    @GET("pokemon/{id}")
    suspend fun getPokemonByName(
        @Path("id") pokeId: Int,
    ): Response<Pokemon>


    @GET("pokemon")
    suspend fun getPaginatedPokemons(
        @Query("limit") limit: Int,
        @Query("offset") offset : Int,
    ): PokemonsApiResult

    @GET("evolution-chain/{id}")
    suspend fun getPokemonSpecies(
        @Path("id") id : Int,
    ): Response<PokemonEvolutionChain>

    @GET("item/{name}")
    fun getPokemonHeldItems(
        @Path("name") name : String,
    ): Call<PokeHeldItems>

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpeciesId(
        @Path("id") id : Int,
    ): Response<PokemonSpecies>

    @GET("ability/{name}")
    suspend fun getPokemonAbilities(
        @Path("name") pokemonName: String?,
    ): Response<PokeAbilities>


}