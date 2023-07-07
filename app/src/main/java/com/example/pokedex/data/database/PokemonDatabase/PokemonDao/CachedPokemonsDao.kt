package com.example.pokedex.data.database.PokemonDatabase.PokemonDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pokedex.data.models.PokemonResult
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedPokemonsDao {
    @Query("SELECT * FROM cached_pokemons")
    fun getCachedPokemons(): Flow<List<PokemonResult>>
    @Query("SELECT (SELECT COUNT(*) FROM cached_pokemons) == 0")
    fun doesDatabaseHaveItems(): Flow<Boolean>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemon : List<PokemonResult>)

    @Query("DELETE FROM cached_pokemons")
    suspend fun deletePokemons()
}