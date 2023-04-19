package com.example.pokedex.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class PokemonsApiResult(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: MutableList<PokemonResult>
)

data class PokemonResult(
    val name: String,
    val url: String,
)  {
}

