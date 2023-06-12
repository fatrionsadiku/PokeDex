package com.example.pokedex.utils

import android.content.res.Resources
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.pokedex.ui.MainActivity

/**
 * Converts a value in density-independent pixels (dp) to pixels (px) based on the device's display metrics.
 *
 * @return The converted value in pixels (px).
 */
fun Int.dpToPx(): Int {
    val displayMetrics = Resources.getSystem().displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), displayMetrics).toInt()
}
/**
 * Capitalizes the first letter of a string
 */
fun String.capitalize(): String {
    if (isEmpty()) {
        return this
    }
    val firstChar = Character.toUpperCase(this[0])
    return firstChar + substring(1)
}

/**
 * Returns the main activity from any fragment by
 * making use of fragment's "Fragment Activity"
 * @throws [ClassNotFoundException]
 * @return [MainActivity]
 */
fun Fragment.requireMainActivity() = this.activity as MainActivity
/**
 * Returns the second member of a list, in other words
 * it returns the [1] element
 */
fun <T> List<T>.second() : T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[1]
}
/**
 * Returns the third member of a list, in other words
 * it returns the [2] element
 */
fun <T> List<T>.third() : T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[2]
}

