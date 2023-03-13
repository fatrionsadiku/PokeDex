package com.example.pokedex.data.server

import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory

object Retrofit {

    private val _retrofit by lazy {
        val client = OkHttpClient.Builder().build()
        retrofit2.Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val pokeApi by lazy {
        _retrofit.create(PokeApiService::class.java)
    }
}