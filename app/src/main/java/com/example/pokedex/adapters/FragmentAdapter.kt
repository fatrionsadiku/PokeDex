package com.example.pokedex.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.pokedex.ui.pokemondetails.pokebasicinfo.PokeBasicInfo
import com.example.pokedex.ui.pokemondetails.pokeabilities.PokeAbilities
import com.example.pokedex.ui.pokemondetails.pokeevotree.PokeEvoTree

class FragmentAdapter(fragmentManager: FragmentManager,lifeCycle : Lifecycle) : FragmentStateAdapter(fragmentManager,lifeCycle) {

    private val pokemonInfos = listOf(PokeBasicInfo(), PokeAbilities(), PokeEvoTree())
    override fun getItemCount(): Int = pokemonInfos.size

    override fun createFragment(position: Int): Fragment = pokemonInfos[position]
}