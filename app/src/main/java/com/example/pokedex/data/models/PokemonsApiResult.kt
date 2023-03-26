package com.example.pokedex.data.models

data class PokemonsApiResult(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: MutableList<PokemonResult>
)

data class PokemonResult(
    val name: String,
    val url: String,
) {
}

data class PokemonApiResult(
    val id: Int,
    val name: String,
    val sprites: Sprites
)

