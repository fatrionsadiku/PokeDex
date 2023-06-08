package com.example.pokedex.data.database.FavoriteDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.pokedex.data.models.FavoritePokemon
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePokemonDao {
    @Query("SELECT * FROM favorite_pokemons")
    fun getFavoritePokemons(): Flow<List<FavoritePokemon>>

    @Query("SELECT EXISTS (SELECT 1 FROM favorite_pokemons WHERE pokemon_name = :pokeName)")
    fun doesPokemonExist(pokeName : String) : Boolean

    @Query("SELECT COUNT(*) FROM favorite_pokemons")
    fun getTotalNumberOfFavorites() : Flow<Int>

    @Upsert()
    suspend fun favoritePokemon(pokemon : FavoritePokemon)

    @Delete
    suspend fun unFavoritePokemon(pokemon: FavoritePokemon)
}