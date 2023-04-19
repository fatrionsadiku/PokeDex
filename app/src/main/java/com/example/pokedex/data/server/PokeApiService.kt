package com.example.pokedex.data.server

import com.example.pokedex.data.models.PokeAbilities
import com.example.pokedex.data.models.Pokemon
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
        @Header("Cache-Control") cacheControl: String = "public, max-age=3600"
    ): Response<Pokemon>


    @GET("pokemon")
    suspend fun getPaginatedPokemons(
        @Query("limit") limit: Int,
        @Header("Cache-Control") cacheControl: String = "public, max-age=3600"
    ): Response<PokemonsApiResult>

    @GET("ability/{name}")
    fun getPokemonAbilities(
        @Path("name") pokemonName : String?
    ) : Call<PokeAbilities>


}