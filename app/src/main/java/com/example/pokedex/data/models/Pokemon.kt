package com.example.pokedex.data.models

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val id : Int,
    val name : String,
    val sprites : Sprites,
    @SerializedName("base_experience")
    val baseEXP : Int,
    val height : Int,
    val weight : Int,
    val types : List<PokemonType>,
    val stats : List<Stats>
) {
    fun getImageUrl() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/dream-world/$id.svg"
}

data class Sprites(
    @SerializedName("front_default")
    val pokeImageUrl : String
)

data class PokemonType(
    val type: Type
)

data class Type(
    val name: String,
    val url: String
)
data class Stats(
    @SerializedName("base_stat")
    val baseStat : Int,
    val effort : Int,
    val stat : Statx
)
data class Statx(
    val name : String,
    val url : String
)
