package com.brightblade.pokedex.di

import android.app.Application
import androidx.room.Room
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun providePokemonDatabase(application: Application): PokemonDatabase =
        Room.databaseBuilder(
            application,
            PokemonDatabase::class.java,
            "favoritepokemon_database"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideFavoritePokemonDao(database: PokemonDatabase) = database.FavoritePokemonDao()

    @Provides
    @Singleton
    fun provideCachedPokemonDao(database: PokemonDatabase) = database.CachedPokemonsDao()
}