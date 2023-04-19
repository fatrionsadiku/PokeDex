package com.example.pokedex.data.models

import com.google.gson.annotations.SerializedName

data class PokeAbilities(
    val id : Int,
    val name : String,
    @SerializedName("effect_entries")
    val effectEntries : List<EffectEntries>
)

data class EffectEntries(
    val effect : String,
    @SerializedName("short_effect")
    val shortEffect : String,
    val language : Language
)

data class Language(
    val name : String,
    val url : String
)