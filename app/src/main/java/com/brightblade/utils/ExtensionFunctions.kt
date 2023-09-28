package com.brightblade.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.brightblade.pokedex.R
import com.brightblade.pokedex.ui.MainActivity
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import kotlin.properties.ReadOnlyProperty

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
 * Returns the first member of a list, in other words
 * it returns the [0] element
 */

fun <T> List<T>.first(): T? {
    return getOrNull(0)
}

/**
 * Returns the second member of a list, in other words
 * it returns the [1] element
 */
fun <T> List<T>.second(): T? {
    return getOrNull(1)
}

/**
 * Returns the third member of a list, in other words
 * it returns the [2] element
 */
fun <T> List<T>.third(): T? {
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

inline fun <reified R : ActivityResultLauncher<String>> Fragment.requestPermission(
    permission: String,
    noinline granted: (permission: String) -> Unit = {},
    noinline denied: (permission: String) -> Unit = {},
    noinline explained: (permission: String) -> Unit = {},
): ReadOnlyProperty<Fragment, R> =
    PermissionResultDelegate(this, permission, granted, denied, explained)

fun PowerMenu.Builder.addGenericItems(ctx: Context): PowerMenu.Builder {
    this.setAnimation(MenuAnimation.FADE)
        .setMenuRadius(10f) // sets the corner radius.
        .setMenuShadow(10f) // sets the shadow.
        .setTextGravity(Gravity.CENTER)
        .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
        .setSelectedTextColor(Color.WHITE)
        .setMenuColor(Color.WHITE)
        .setSelectedMenuColor(ContextCompat.getColor(ctx, R.color.purple_500))
    return this
}

fun PowerMenu.Builder.addPowerMenuItems(vararg items: PowerMenuItem): PowerMenu.Builder {
    items.forEach {
        this.addItem(
            it
        )
    }
    return this
}

fun Drawable.getDominantColor(onFinish: (Int) -> Unit) {
    val bitMap = (this as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
    Palette.from(bitMap).generate { palette ->
        palette?.dominantSwatch?.let { dominantColor ->
            val color = dominantColor.rgb
            onFinish(color)
        }
    }
}

fun View.setMarginExtensionFunction(left: Int, top: Int, right: Int, bottom: Int) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(left, top, right, bottom)
    layoutParams = params
}

fun View.fadeIn(duration: Long = 500L, alpha: Float = 1f): ViewPropertyAnimator {
    return animate()
        .setDuration(duration)
        .alpha(alpha)
}

fun View.fadeOut(duration: Long = 300L): ViewPropertyAnimator {
    return animate()
        .setDuration(duration)
        .alpha(0f)
}



