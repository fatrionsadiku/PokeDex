package com.example.pokedex.data.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


data class PokemonsApiResult(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: MutableList<PokemonResult>
)
@Entity(tableName = "cached_pokemons")
@Parcelize
data class PokemonResult(
    @PrimaryKey
    @SerializedName("name")
    @ColumnInfo("pokemon_name")
    val name: String,
    @SerializedName("url")
    @ColumnInfo("url")
    val url: String,
) : Parcelable  {
}

