package com.brightblade.pokedex.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.brightblade.pokedex.ui.pokemondetails.aboutpokemon.AboutPokemonFragment
import com.brightblade.pokedex.ui.pokemondetails.pokeabilities.PokeAbilities
import com.brightblade.pokedex.ui.pokemondetails.pokebasicinfo.PokeBasicInfo
import com.brightblade.pokedex.ui.pokemondetails.pokeevotree.PokeEvoTree

class FragmentAdapter(fragmentManager: FragmentManager,lifeCycle : Lifecycle) : FragmentStateAdapter(fragmentManager,lifeCycle) {

    private val pokemonInfos =
        listOf(AboutPokemonFragment(), PokeBasicInfo(), PokeAbilities(), PokeEvoTree())

    override fun getItemCount(): Int = pokemonInfos.size

    override fun createFragment(position: Int): Fragment = pokemonInfos[position]
}