package com.brightblade.utils

import android.view.Gravity
import android.widget.LinearLayout
import com.brightblade.pokedex.R
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

object Utility {

    const val BASE_URL = "https://pokeapi.co/api/v2/"
    val listOfColors =
        listOf(
            "#80B362", "#FF7377", "#E6676B", "#BF565A", "#3EB39F", "#FF7276", "#A5A9A0", "#FFB673",
            "#FFFCC9", "#FFC1CC", "#DBE2E9", "#BF9972", "#B67233"
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
        R.drawable.bulbasaur_silhouette
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