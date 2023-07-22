package com.brightblade.pokedex.data.database.PokemonDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDao.CachedPokemonsDao
import com.brightblade.pokedex.data.database.PokemonDatabase.FavoriteDao.FavoritePokemonDao
import com.brightblade.pokedex.data.models.PokemonResult

@Database(entities = [FavoritePokemon::class,PokemonResult::class], version = 2, exportSchema = false)
abstract class PokemonDatabase : RoomDatabase(){

    abstract fun FavoritePokemonDao() : FavoritePokemonDao
    abstract fun CachedPokemonsDao() : CachedPokemonsDao

}