package com.brightblade.pokedex.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity(tableName = "favorite_pokemons")
data class FavoritePokemon(
    @PrimaryKey
    @SerializedName("name")
    @ColumnInfo("pokemon_name")
    val pokeName: String,
    @SerializedName("url")
    @ColumnInfo("url")
    val url: String?,
)
