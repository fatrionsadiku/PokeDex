package com.example.pokedex.di

import com.example.pokedex.data.network.PokeApiService
import com.example.pokedex.utils.Utility
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideOkHTTPClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            addInterceptor(interceptor)
        }.build()

    @Provides
    @Singleton
    fun provideRetrofitInstance(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(Utility.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    @Provides
    @Singleton
    fun providePokeAPIInstance(retrofit: Retrofit): PokeApiService =
        retrofit.create(PokeApiService::class.java)
}