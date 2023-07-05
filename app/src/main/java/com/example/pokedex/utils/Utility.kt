package com.example.pokedex.utils

import android.view.Gravity
import android.widget.LinearLayout
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime

object Utility {

    const val BASE_URL = "https://pokeapi.co/api/v2/"
    const val PAGE_SIZE = 1281
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


    fun measure(codeBlock : () -> Unit){
        val nanoTime = measureNanoTime(codeBlock)
        val milliSeconds = TimeUnit.NANOSECONDS.toMillis(nanoTime)
        println("The code execution took ${milliSeconds}ms")
    }


}