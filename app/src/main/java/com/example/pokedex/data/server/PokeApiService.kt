package com.example.pokedex.data.server

import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonApiResult
import com.example.pokedex.data.models.PokemonsApiResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {


//    https://pokeapi.co/api/v2/pokemon/?limit=30&offset=30

    @GET("pokemon/{name}")
    fun getPokemonByName(@Path("name") pokeId : String) : Call<Pokemon>


    @GET("pokemon")
     fun getPaginatedPokemons(@Query("limit") limit : Int) : Call<PokemonsApiResult>


}