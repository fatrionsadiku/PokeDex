package com.example.pokedex.utils

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.pokedex.ui.MainActivity

/**
 * Converts a value in density-independent pixels (dp) to pixels (px) based on the device's display metrics.
 *
 * @return The converted value in pixels (px).
 */
fun Int.dpToPx(): Int {
    val displayMetrics = Resources.getSystem().displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), displayMetrics)
        .toInt()
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
 * Gets or sets the visibility state of the View.
 *
 * When getting the value, it returns `true` if the View's visibility is [View.VISIBLE],
 * indicating that the View is visible on the screen. Otherwise, it returns `false`.
 *
 * When setting the value, it sets the visibility of the View based on the given [value].
 * If [value] is `true`, the View's visibility is set to [View.VISIBLE], making it visible.
 * If [value] is `false`, the View's visibility is set to [View.INVISIBLE], making it invisible
 * while still taking up space in the layout.
 *
 * @see View.getVisibility
 * @see View.VISIBLE
 * @see View.INVISIBLE
 */
inline var View.isViewVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.INVISIBLE
    }

/**
 * Returns the second member of a list, in other words
 * it returns the [1] element
 */
fun <T> List<T>.second(): T {
    if (isEmpty())
        throw NoSuchElementException("List is empty.")
    return this[1]
}

/**
 * Returns the third member of a list, in other words
 * it returns the [2] element
 */
fun <T> List<T>.third(): T? {
    require(size >= 3) { "List has less than 3 elements." }
    return getOrNull(2)
}

fun EditText.isNumeric(): Boolean {
    if (this.text.toString().all {
            it.isLetter()
        }) return false
    if (this.text.toString().contains("[a-z]".toRegex()) && this.text.toString()
            .contains("[0-9]".toRegex())
    ) return false
    return (this.text.toString().toInt() >= 0 || this.text.toString()
        .toInt() <= 0) && this.text.toString().all { !it.isLetter() }
}


