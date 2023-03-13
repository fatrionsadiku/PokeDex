package com.example.pokedex.data.models

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val id : Int,
    val name : String,
    val sprites : Sprites

)

data class Sprites(
    @SerializedName("front_default")
    val pokeImageUrl : String
)
