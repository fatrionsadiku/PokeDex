package com.brightblade.utils

import android.view.Gravity
import android.widget.LinearLayout
import com.brightblade.pokedex.R
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

object Utility {

    const val MAX_POKEMON_SIZE: Int = 1010
    const val HIGHEST_POKEMON_ID: Int = 1010
    const val BASE_URL = "https://pokeapi.co/api/v2/"
    val listOfColors =
        listOf(
            "#FF80AC",
            "#B6C8D8",
            "#7F8080",
            "#8FBCE8",
            "#F1A1F8",
            "#B6C9E8",
            "#F0C080",
            "#C0E4F0",
            "#8AD8E8",
            "#F1C080",
            "#F1C080",
            "#F1BF80",
            "#C5D3E8",
            "#E6C8D8",
            "#99A0B0",
            "#99A0B0",
            "#F1C080",
            "#A2BBE8",
            "#C0D1E0",
            "#B6C8E0",
            "#B9C48F",
            "#C4DAD8",
            "#C5D3E8",
            "#CAE1E8",
            "#D9C2B0",
            "#E0D838",
            "#7E8078",
            "#8FA7D8",
            "#F1A1F8",
            "#F1C080",
            "#F1C080",
            "#F0A1B8",
            "#D3AA80",
            "#7E8080",
            "#E4A8B8",
            "#CE7F80",
            "#F0B4A8",
            "#D9D880",
            "#D9D880",
            "#E4A6E8",
            "#D9E080",
            "#E0D980",
            "#82C8E8",
            "#82C7D0"
        )
    val listOfIcons = listOf(
        R.drawable.quiz_icon_1,
        R.drawable.quiz_icon_2,
        R.drawable.quiz_icon_3,
        R.drawable.quiz_icon_1,
        R.drawable.quiz_icon_2,
        R.drawable.quiz_icon_3,
        R.drawable.quiz_icon_1,
        R.drawable.quiz_icon_2,
        R.drawable.quiz_icon_3,
    )
    val listOfSilhouettes = listOf(
        R.drawable.pika_silhouette,
        R.drawable.squirtle_silhouette,
        R.drawable.bulbasaur_silhouette,
    )
    val pokeNameParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        0.3f
    ).also {
        it.gravity = Gravity.CENTER_HORIZONTAL
    }
    val linearLayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT,
        1f
    ).also {
        it.gravity = Gravity.CENTER
    }
    fun getPokemonID(url: String) = url.replace(
        "https://pokeapi.co/api/v2/pokemon-species/",
        ""
    ).replace("/", "").toInt()

    fun getPokemonSpeciesId(url: String) = url.replace(
        "https://pokeapi.co/api/v2/evolution-chain/",
        ""
    ).replace("/", "").toInt()


    fun measure(codeBlock: () -> Unit) {
        val nanoTime = measureNanoTime(codeBlock)
        val milliSeconds = TimeUnit.NANOSECONDS.toMillis(nanoTime)
        println("The code execution took ${milliSeconds}ms")
    }


}