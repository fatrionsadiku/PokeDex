package com.example.pokedex.data.database.PokemonDatabase.PokemonDao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pokedex.data.models.PokemonResult
import com.example.pokedex.data.persistent.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedPokemonsDao {

    fun getPokemons(sortOrder: SortOrder) = when(sortOrder){
        SortOrder.BY_ID_DESCENDING -> {
            getCachedPokemonsDescending()
        }
        SortOrder.BY_ID_ASCENDING  -> {
            getCachedPokemonsAscending()
        }
    }
    @Query("SELECT * FROM cached_pokemons ORDER BY primaryKey DESC ")
    fun getCachedPokemonsDescending(): Flow<List<PokemonResult>>

    @Query("SELECT * FROM cached_pokemons ORDER BY primaryKey ASC ")
    fun getCachedPokemonsAscending(): Flow<List<PokemonResult>>
    @Query("SELECT (SELECT COUNT(*) FROM cached_pokemons) == 0")
    fun doesDatabaseHaveItems(): Flow<Boolean>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemons(pokemon : List<PokemonResult>)

    @Query("DELETE FROM cached_pokemons")
    suspend fun deletePokemons()
}