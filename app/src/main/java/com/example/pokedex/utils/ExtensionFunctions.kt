package com.example.pokedex.utils

import android.content.res.Resources
import android.util.TypedValue

fun Int.dpToPx(): Int {
    val displayMetrics = Resources.getSystem().displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), displayMetrics).toInt()
}

fun String.capitalize(): String {
    if (isEmpty()) {
        return this
    }
    val firstChar = Character.toUpperCase(this[0])
    return firstChar + substring(1)
}