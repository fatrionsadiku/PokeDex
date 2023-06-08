package com.example.pokedex.data.database.FavoriteDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.pokedex.data.database.FavoriteDao.FavoritePokemonDao
import com.example.pokedex.data.models.FavoritePokemon

@Database(entities = [FavoritePokemon::class], version = 1)
abstract class FavoritePokemonDatabase : RoomDatabase(){

    abstract fun FavoritePokemonDao() : FavoritePokemonDao

}