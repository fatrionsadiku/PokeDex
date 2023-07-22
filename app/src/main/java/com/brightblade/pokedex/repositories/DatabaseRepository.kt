package com.brightblade.pokedex.repositories

import com.brightblade.pokedex.data.database.PokemonDatabase.FavoriteDao.FavoritePokemonDao
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDao.CachedPokemonsDao
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.di.ApplicationScope
import javax.inject.Inject


@ApplicationScope
class DatabaseRepository @Inject constructor(
    private val favPokeDao: FavoritePokemonDao,
    private val cachedPokemonsDao: CachedPokemonsDao,
) {
    fun getFavoritePokemons() = favPokeDao.getFavoritePokemons()

    fun doesDatabaseHaveItems() = favPokeDao.doesDatabaseHaveItems()

    fun doesCachedDatabaseHaveItems() = cachedPokemonsDao.doesDatabaseHaveItems()

    suspend fun doesPokemonExist(pokemonName: String) = favPokeDao.doesPokemonExist(pokemonName)

    fun getTotalNumberOfFavs() = favPokeDao.getTotalNumberOfFavorites()
    suspend fun favoritePokemon(pokemon: FavoritePokemon) = favPokeDao.favoritePokemon(pokemon)
    suspend fun unFavoritePokemon(pokemon: FavoritePokemon) = favPokeDao.unFavoritePokemon(pokemon)

}