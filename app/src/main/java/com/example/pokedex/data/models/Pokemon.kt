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
) {
    fun getImageUrl() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/dream-world/$id.svg"
}

data class Sprites(
    @SerializedName("front_default")
    val pokeImageUrl : String
)

data class Types(
    @SerializedName("0")
    val typesHolder : TypeHolder,
    @SerializedName("1")
    val typesHolderX : TypeHolderX
)

data class TypeHolder(
    val slot : Int,
    val type : ActualType
)
data class TypeHolderX(
    val slot : Int,
    val type : ActualTypeX
)

data class ActualType(
    val name : String,
    val url : String
)
data class ActualTypeX(
    val name : String,
    val url : String
)
