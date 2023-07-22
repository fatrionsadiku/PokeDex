package com.brightblade.utils.customviews

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.ItemPokemonTypeBinding
import com.google.android.material.card.MaterialCardView

class CustomCardView(context: Context, pokemonType: String) : MaterialCardView(context) {
    private val binding = ItemPokemonTypeBinding.inflate(LayoutInflater.from(context),this,true)
    init {
        binding.apply{
            root.setBackgroundColor(Color.TRANSPARENT)
            firstStatName.text = pokemonType
            root.radius = resources.getDimension(R.dimen.card_corner_radius)
        }
    }
}