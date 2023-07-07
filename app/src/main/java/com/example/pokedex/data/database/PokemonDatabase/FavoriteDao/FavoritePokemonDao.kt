package com.example.pokedex.data.database.PokemonDatabase.FavoriteDao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.pokedex.data.models.FavoritePokemon
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritePokemonDao {
    @Query("SELECT * FROM favorite_pokemons")
    fun getFavoritePokemons(): Flow<List<FavoritePokemon>>

    @Query("SELECT EXISTS (SELECT 1 FROM favorite_pokemons WHERE pokemon_name = :pokeName)")
    suspend fun doesPokemonExist(pokeName : String) : Boolean

    @Query("SELECT COUNT(*) FROM favorite_pokemons")
    fun getTotalNumberOfFavorites() : Flow<Int>

    @Query("SELECT (SELECT COUNT(*) FROM favorite_pokemons) == 0")
    fun doesDatabaseHaveItems(): Flow<Boolean>

    @Upsert()
    suspend fun favoritePokemon(pokemon : FavoritePokemon)

    @Delete
    suspend fun unFavoritePokemon(pokemon: FavoritePokemon)
}