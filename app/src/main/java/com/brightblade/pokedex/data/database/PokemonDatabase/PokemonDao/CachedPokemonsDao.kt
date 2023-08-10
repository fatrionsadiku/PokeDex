package com.brightblade.pokedex.data.database.PokemonDatabase.PokemonDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brightblade.pokedex.data.models.PokemonResult
import com.brightblade.pokedex.data.persistent.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedPokemonsDao {

    fun getPokemons(sortOrder: SortOrder) = when (sortOrder) {
        SortOrder.BY_ID_DESCENDING   -> {
            getCachedPokemonsDescending()
        }

        SortOrder.BY_ID_ASCENDING    -> {
            getCachedPokemonsAscending()
        }

        SortOrder.BY_NAME_DESCENDING -> {
            getCachedPokemonsByNameDescending()
        }

        SortOrder.BY_NAME_ASCENDING  -> {
            getCachedPokemonsByNameAscending()
        }
    }

    @Query("SELECT * FROM cached_pokemons ORDER BY primaryKey DESC ")
    fun getCachedPokemonsDescending(): Flow<List<PokemonResult>>

    @Query("SELECT * FROM cached_pokemons ORDER BY primaryKey ASC ")
    fun getCachedPokemonsAscending(): Flow<List<PokemonResult>>

    @Query("SELECT * FROM cached_pokemons ORDER BY pokemon_name ASC ")
    fun getCachedPokemonsByNameAscending(): Flow<List<PokemonResult>>

    @Query("SELECT * FROM cached_pokemons ORDER BY pokemon_name DESC ")
    fun getCachedPokemonsByNameDescending(): Flow<List<PokemonResult>>

    @Query("SELECT (SELECT COUNT(*) FROM cached_pokemons) == 0")
    fun doesDatabaseHaveItems(): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemon: List<PokemonResult>)

    @Query("DELETE FROM cached_pokemons")
    suspend fun deletePokemons()
}