package com.example.pokedex.data.server

import com.example.pokedex.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class Repository {
    var apiService : PokeApiService

    init {
        val client = OkHttpClient.Builder().build()
        apiService = Retrofit.Builder().
            baseUrl(Constants.BASE_URL).
            client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokeApiService::class.java)

    }
}