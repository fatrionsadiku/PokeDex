package com.example.pokedex.di

import android.app.Application
import com.example.pokedex.data.Repository
import com.example.pokedex.data.server.PokeApiService
import com.example.pokedex.utils.Utility
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideOkHTTPClient(application: Application): OkHttpClient = OkHttpClient.Builder()
        .apply {
            val interceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
//            val cacheDirectory = File(application.cacheDir, "http-cache")
//            val cacheSize = 10 * 1024 * 1024 // 10 MB
//            val cache = Cache(cacheDirectory, cacheSize.toLong())
//            cache(cache)
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

    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Singleton
    @Provides
    fun providePokemonRepository(pokeApiService: PokeApiService) = Repository(pokeApiService)
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope