package com.example.pokedex.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    val pokemonInfos = listOf(PokeBasicInfo(),PokeEvolutionTree())
    override fun getItemCount(): Int = pokemonInfos.size

    override fun createFragment(position: Int): Fragment = pokemonInfos[position]
}