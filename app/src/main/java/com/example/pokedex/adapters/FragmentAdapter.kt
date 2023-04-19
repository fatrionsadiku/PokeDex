package com.example.pokedex.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pokedex.ui.pokemondetails.PokeBasicInfo
import com.example.pokedex.ui.pokemondetails.PokeAbilities

class FragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val pokemonInfos = listOf(PokeBasicInfo(), PokeAbilities())
    override fun getItemCount(): Int = pokemonInfos.size

    override fun createFragment(position: Int): Fragment = pokemonInfos[position]
}