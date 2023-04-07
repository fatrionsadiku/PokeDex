package com.example.pokedex.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import com.example.pokedex.R
import com.example.pokedex.databinding.PokeTypeCardViewBinding
import com.google.android.material.card.MaterialCardView

class CustomCardView(context: Context, pokemonType: String) : MaterialCardView(context) {
    private val binding = PokeTypeCardViewBinding.inflate(LayoutInflater.from(context),this,true)
    init {
        binding.apply{
            root.setBackgroundColor(Color.TRANSPARENT)
            firstStatName.text = pokemonType
            root.radius = resources.getDimension(R.dimen.card_corner_radius)
        }
    }
}