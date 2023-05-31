package com.example.pokedex.data.server

import com.example.pokedex.data.models.PokeAbilities
import com.example.pokedex.data.models.PokeHeldItems
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonEvolutionChain
import com.example.pokedex.data.models.PokemonSpecies
import com.example.pokedex.data.models.PokemonsApiResult
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {


    @GET("pokemon/{name}")
    suspend fun getPokemonByName(
        @Path("name") pokeId: String,
    ): Response<Pokemon>


    @GET("pokemon")
    suspend fun getPaginatedPokemons(
        @Query("limit") limit: Int,
        @Query("offset") offset : Int,
    ): Response<PokemonsApiResult>

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
    fun getPokemonAbilities(
        @Path("name") pokemonName : String?
    ) : Call<PokeAbilities>


}