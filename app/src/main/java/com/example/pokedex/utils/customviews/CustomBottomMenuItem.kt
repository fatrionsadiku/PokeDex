package com.example.pokedex.utils.customviews

import android.content.Context
import android.view.MenuItem
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.pokedex.R
import com.example.pokedex.utils.dpToPx
import com.skydoves.androidbottombar.BottomMenuItem

class CustomBottomMenuItem(
    context: Context,
    private val title: String,
    @ColorRes private val titleColorRes: Int = R.color.black,
    @ColorRes private val titleActiveColorRes: Int = R.color.white,
    private val titlePadding: Int = 6,
    private val titleSize: Float = 14f,
    private val iconColorRes: Int = R.color.white,
    private val iconActiveColorRes: Int = R.color.purple_700,
    private val iconSize: Int = 20,
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
