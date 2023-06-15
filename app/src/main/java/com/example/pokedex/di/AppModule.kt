package com.example.pokedex.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.example.pokedex.data.Repository
import com.example.pokedex.data.database.FavoriteDao.FavoritePokemonDao
import com.example.pokedex.data.database.FavoriteDatabase.FavoritePokemonDatabase
import com.example.pokedex.data.server.PokeApiService
import com.example.pokedex.utils.Utility
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton


private const val USER_PREFERENCES = "user_preferences"

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFavoritePokemonDatabase(application: Application): FavoritePokemonDatabase =
        Room.databaseBuilder(
            application,
            FavoritePokemonDatabase::class.java,
            "favoritepokemon_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoritePokemonDao(database : FavoritePokemonDatabase) = database.FavoritePokemonDao()

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

    @Singleton
    @Provides
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(appContext, USER_PREFERENCES)),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }

    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Singleton
    @Provides
    fun providePokemonRepository(pokeApiService: PokeApiService, favoritePokemonDao: FavoritePokemonDao) = Repository(pokeApiService,favoritePokemonDao)
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope