package com.example.pokedex.ui.pokemondetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pokedex.databinding.PokemonEvoTreeBinding
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
            viewModel.abilitiesResponse.observe(viewLifecycleOwner) { pokeAbility ->
                if (pokeAbility != null){
                    firstAbility.text = pokeAbility.first()?.name?.capitalize()
                    firstAbilityDescription.text = pokeAbility.first()?.effectEntries?.get(1)?.effect
                    secondAbility.text = pokeAbility[1]?.name?.capitalize() ?: ""
                    secondAbilityDescription.text = pokeAbility[1]?.effectEntries?.get(1)?.effect ?: ""
                }
            }
        }

    }

}