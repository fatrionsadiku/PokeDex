package com.example.pokedex.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.pokedex.databinding.PokemonBasicInfoBinding

class PokeBasicInfo : Fragment() {
    lateinit var binding : PokemonBasicInfoBinding
    lateinit var viewModel: PokemonDetailsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PokemonBasicInfoBinding.inflate(inflater,container,false)
        viewModel = ViewModelProvider(requireActivity())[PokemonDetailsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            viewModel.pokemonResponse.observe(viewLifecycleOwner){
                Log.d("POKE DEBUG YAY", "onViewCreated: ${it.toString()}")
                pokeBaseXP.text = it.baseEXP.toString()
                pokeWeight.text = "${it.weight}kg"
                pokeHeight.text = "${it.height}cm"
            }
        }

    }


}