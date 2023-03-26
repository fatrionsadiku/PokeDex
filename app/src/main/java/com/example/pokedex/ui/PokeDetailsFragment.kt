package com.example.pokedex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.databinding.PokeDetailsLayoutBinding
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.launch

class PokeDetailsFragment : Fragment() {
    lateinit var binding : PokeDetailsLayoutBinding
    val pokemonArgs : PokeDetailsFragmentArgs by navArgs()
    lateinit var pokeViewModel : PokemonDetailsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PokeDetailsLayoutBinding.inflate(inflater,container,false)
        pokeViewModel = ViewModelProvider(requireActivity())[PokemonDetailsViewModel::class.java]
        getPokemonDetails(pokemonArgs)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    private fun getPokemonDetails(pokeName : PokeDetailsFragmentArgs){
        val pokemonName = pokeName.pokemonName
        viewLifecycleOwner.lifecycleScope.launch {
            val fetchedPokemon = pokeViewModel.getSinglePokemonByName(pokemonName!!)
            if (fetchedPokemon != null) {
                fillPokemonDataOnScreen(fetchedPokemon)
            }
        }
    }

    private fun fillPokemonDataOnScreen(pokemon : Pokemon) {
        binding.apply {
            pokemonName.text = pokemon.name
            Glide.with(requireContext()).load(pokemon.getImageUrl()).into(pokemonPhoto)
        }
    }
}