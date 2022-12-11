package com.example.pokedex.data.server

import com.example.pokedex.data.models.Pokemon
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {


    @GET("https://pokeapi.co/api/v2/pokemon/")
    fun getPokemons() : Call<Pokemon>

//    @GET("https://pokeapi.co/api/v2/pokemon/{name}/")
//    fun getPokemons(@Path("name") pokeName : String) : Call<Pokemon>

}