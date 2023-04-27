package com.example.pokedex.ui.pokemondetails

import android.media.session.PlaybackState.CustomAction
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pokedex.databinding.PokemonEvoTreeBinding
import com.example.pokedex.utils.customviews.PokeAbilitiesLayout
import com.example.pokedex.viewmodels.PokeDetailsSharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokeAbilities : Fragment() {
    lateinit var binding: PokemonEvoTreeBinding
    private lateinit var viewModel: PokeDetailsSharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PokemonEvoTreeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[PokeDetailsSharedViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            viewModel.abilitiesResponse.apply {
                observe(viewLifecycleOwner) { pokeAbility ->
                    pokeAbility?.forEach {
                        val pokeAbilityTitle = it?.name?.capitalize()
                        val pokeAbilityDescription = it?.effectEntries?.get(1)?.effect
                        val pokemonAbility = PokeAbilitiesLayout(requireContext(),pokeAbilityTitle,pokeAbilityDescription)
                        pokeDetailsHolder.addView(pokemonAbility)
                    }
                }
            }
        }

    }

}