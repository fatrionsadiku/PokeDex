package com.example.pokedex.data.models

import com.google.gson.annotations.SerializedName

data class Versions(
    @SerializedName("generation-i") val generationi: GenerationI,
    @SerializedName("generation-ii") val generationii: GenerationIi,
    @SerializedName("generation-iii") val generationiii: GenerationIii,
    @SerializedName("generation-iv") val generationiv: GenerationIv,
    @SerializedName("generation-v") val generationv: GenerationV,
    @SerializedName("generation-vi") val generationvi: GenerationVi,
    @SerializedName("generation-vii") val generationvii: GenerationVii,
    @SerializedName("generation-viii") val generationviii: GenerationViii
)