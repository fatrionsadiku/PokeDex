package com.example.pokedex.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class PokemonsApiResult(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: MutableList<PokemonResult>
)
@Parcelize
data class PokemonResult(
    val name: String,
    val url: String,
) : Parcelable {
    fun getPokemonPicture() : String {
        val pokeId = url.replace(
            "https://pokeapi.co/api/v2/pokemon/",
            ""
        ).replace("/", "").toInt()

        return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokeId.png"
    }
}

data class PokemonApiResult(
    val id: Int,
    val name: String,
    val sprites: Sprites
)

