package com.example.pokedex.data.models

import com.google.gson.annotations.SerializedName

data class PokemonEvolutionChain(
    val id : Int,
    val chain : Chain,
    val species : SpeciesX,
    @SerializedName("is_baby")
    val isBaby : Boolean,
)

data class SpeciesX(
    val name : String,
    val url : String
)

data class Chain(
    @SerializedName("evolves_to")
    val evoDetails : List<EvolutionDetails>,
    val species: SpeciesX
)

data class EvolutionDetails(
    val species : SpeciesX,
    val name : String?,
    @SerializedName("evolves_to")
    val evoDetails : List<EvolutionDetails?>,
)

data class PokemonSpecies(
    @SerializedName("evolution_chain")
    val evoChain : EvolutionChain
)

data class EvolutionChain(
    val url : String
)
