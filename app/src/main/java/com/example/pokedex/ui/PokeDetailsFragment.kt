package com.example.pokedex.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.databinding.PokeDetailsLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PokeDetailsFragment : Fragment() {
    lateinit var binding: PokeDetailsLayoutBinding
    val pokemonArgs: PokeDetailsFragmentArgs by navArgs()
    lateinit var pokeViewModel: PokemonDetailsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PokeDetailsLayoutBinding.inflate(inflater, container, false)
        pokeViewModel = ViewModelProvider(requireActivity())[PokemonDetailsViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPokemonDetails(pokemonArgs)

    }

    private fun getPokemonDetails(pokeName: PokeDetailsFragmentArgs) {
        val pokemonName = pokeName.pokemonName
        viewLifecycleOwner.lifecycleScope.launch {
            var fetchedPokemon : Pokemon?
            withContext(Dispatchers.IO) {
                 fetchedPokemon = pokeViewModel.getSinglePokemonByName(pokemonName!!)
            }
            if (fetchedPokemon != null) {
                Log.d("Pokemon Debug", fetchedPokemon.toString())
                fillPokemonDataOnScreen(fetchedPokemon!!)
            }
        }
    }

    private fun fillPokemonDataOnScreen(pokemon: Pokemon) {
        binding.apply {
            Glide.with(requireActivity()).load(pokemonArgs.pokemonUrl).into(binding.pokemonPhoto)
            pokemonName.text = "${pokemonArgs.pokemonName?.capitalize()}"
            pokeWeight.text = "${pokemon.weight}kg"
            pokeHeight.text = "${pokemon.height}m"
            pokeBaseXP.text = pokemon.baseEXP.toString()
        }
    }
}