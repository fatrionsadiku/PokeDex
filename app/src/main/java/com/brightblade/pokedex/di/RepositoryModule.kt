package com.brightblade.pokedex.di

import com.brightblade.pokedex.data.database.PokemonDatabase.FavoriteDao.FavoritePokemonDao
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDao.CachedPokemonsDao
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDatabase
import com.brightblade.pokedex.data.network.PokeApiService
import com.brightblade.pokedex.repositories.DatabaseRepository
import com.brightblade.pokedex.repositories.NetworkRepository
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
    ) = DatabaseRepository(favoritePokemonDao)

    @Singleton
    @Provides
    fun provideNetworkRepository(
        pokeApi: PokeApiService,
        cachedPokemonsDao: CachedPokemonsDao,
        pokemonDatabase: PokemonDatabase,
    ) = NetworkRepository(pokeApi, cachedPokemonsDao, pokemonDatabase)
}