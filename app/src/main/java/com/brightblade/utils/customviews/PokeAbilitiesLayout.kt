package com.brightblade.utils.customviews

import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.brightblade.pokedex.databinding.ItemPokemonAbilityBinding

class PokeAbilitiesLayout(
    ctx: Context,
    private val abilityTitle: String?,
    private val abilityDescription: String?
) : ConstraintLayout(ctx) {
    private val binding = ItemPokemonAbilityBinding.inflate(LayoutInflater.from(ctx), this, true)
    init {
        binding.apply {
            firstAbility.text = abilityTitle
            firstAbilityDescription.text = abilityDescription
        }
    }
}