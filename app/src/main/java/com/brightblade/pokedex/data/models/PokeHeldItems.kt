package com.brightblade.pokedex.data.models

import com.google.gson.annotations.SerializedName


data class PokeHeldItems(
    val cost : Int,
    @SerializedName("effect_entries")
    val effectEntries : List<EffectEntries>,
    val name : String,
    val sprites : ItemSprites
)

data class ItemSprites(
    val default : String
)
