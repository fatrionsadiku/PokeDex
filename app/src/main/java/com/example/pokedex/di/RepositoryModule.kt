package com.example.pokedex.di

import com.example.pokedex.repositories.DatabaseRepository
import com.example.pokedex.data.database.PokemonDatabase.FavoriteDao.FavoritePokemonDao
import com.example.pokedex.data.database.PokemonDatabase.PokemonDao.CachedPokemonsDao
import com.example.pokedex.data.database.PokemonDatabase.PokemonDatabase
import com.example.pokedex.data.network.PokeApiService
import com.example.pokedex.repositories.NetworkRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Singleton
    @Provides
    fun provideDatabaseRepository(
        favoritePokemonDao: FavoritePokemonDao,
        cachedPokemonsDao: CachedPokemonsDao,
    ) = DatabaseRepository(favoritePokemonDao,cachedPokemonsDao)
    @Singleton
    @Provides
    fun provideNetworkRepository(
        pokeApi : PokeApiService,
        cachedPokemonsDao: CachedPokemonsDao,
        pokemonDatabase: PokemonDatabase
    ) = NetworkRepository(pokeApi, cachedPokemonsDao, pokemonDatabase)
}