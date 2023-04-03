package com.example.pokedex.ui

import android.os.Bundle
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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fillPokemonDataOnScreen()

    }

//    private fun getPokemonDetails(pokeName : PokeDetailsFragmentArgs){
//        val pokemonName = pokeName.pokemonName
//        viewLifecycleOwner.lifecycleScope.launch {
//            val fetchedPokemon = pokeViewModel.getSinglePokemonByName(pokemonName!!)
//            if (fetchedPokemon != null) {
//                fillPokemonDataOnScreen(fetchedPokemon)
//            }
//        }
//    }

    private fun fillPokemonDataOnScreen() {
        binding.apply {
            Glide.with(requireActivity()).load(pokemonArgs.pokemonUrl).into(binding.pokemonPhoto)
            pokemonName.text = pokemonArgs.pokemonName
        }
    }
}