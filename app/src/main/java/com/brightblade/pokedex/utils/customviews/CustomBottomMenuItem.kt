package com.brightblade.pokedex.utils.customviews

import android.content.Context
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.brightblade.pokedex.R
import com.brightblade.pokedex.utils.dpToPx
import com.skydoves.androidbottombar.BottomMenuItem

class CustomBottomMenuItem(
    context: Context,
    title: String,
    @ColorRes private val titleColorRes: Int = R.color.black,
    @ColorRes private val titleActiveColorRes: Int = R.color.white,
    titlePadding: Int = 6,
    titleSize: Float = 14f,
    iconColorRes: Int = R.color.white,
    iconActiveColorRes: Int = R.color.purple_700,
    iconSize: Int = 20,
) : BottomMenuItem(context) {

    init {
        this.setTitle(title)
        this.setTitleColor(ContextCompat.getColor(this.context, titleColorRes))
        this.setTitleActiveColor(ContextCompat.getColor(this.context, titleActiveColorRes))
        this.setTitlePadding(titlePadding)
        this.setTitleSize(titleSize)
        this.setIconColor(ContextCompat.getColor(this.context, iconColorRes))
        this.iconForm.iconActiveColor = (ContextCompat.getColor(this.context, iconActiveColorRes))
        this.iconForm.iconSize = 30.dpToPx()
        this.setIconSize(iconSize)
    }
}
