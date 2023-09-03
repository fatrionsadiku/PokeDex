package com.brightblade.pokedex.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


data class PokemonsApiResult(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: MutableList<PokemonResult>
)
@Entity(tableName = "cached_pokemons")
data class PokemonResult(
    @PrimaryKey(autoGenerate = true)
    val primaryKey: Int,
    @SerializedName("name")
    @ColumnInfo("pokemon_name")
    val name: String,
    @SerializedName("url")
    @ColumnInfo("url")
    val url: String,
)

