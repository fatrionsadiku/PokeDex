package com.example.pokedex.ui.pokemondetails

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.databinding.PokeDetailsLayoutBinding
import com.example.pokedex.ui.FragmentAdapter
import com.example.pokedex.viewmodels.PokeDetailsSharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pixplicity.sharp.Sharp
@AndroidEntryPoint
class PokeDetailsFragment : Fragment() {
    lateinit var binding: PokeDetailsLayoutBinding
    val pokemonArgs: PokeDetailsFragmentArgs by navArgs()
    lateinit var pokeViewModel: PokeDetailsSharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PokeDetailsLayoutBinding.inflate(inflater, container, false)
        pokeViewModel = ViewModelProvider(requireActivity())[PokeDetailsSharedViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getPokemonDetails(pokemonArgs)
        setUpPokeDetailsViewPager()

    }

    private fun getPokemonDetails(pokeName: PokeDetailsFragmentArgs) {
        val pokemonName = pokeName.pokemonName
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
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
            binding.progressBar.isVisible = true
            binding.pokemonPhoto.isVisible = false
            Log.d("SVG Url", pokemon.getImageUrl())
            Sharp.loadFromNetwork(pokemon.getImageUrl()).getSharpPicture {
                pokemonPhoto.setImageDrawable(it.drawable)
            }
            pokemonName.text = "${pokemonArgs.pokemonName?.capitalize()}"
            binding.progressBar.isVisible = false
            binding.pokemonPhoto.isVisible = true
        }
    }

    private fun setUpPokeDetailsViewPager() {
        binding.apply {
            val adapter = FragmentAdapter(requireActivity())
            pokeInfosViewPager.adapter = adapter
            TabLayoutMediator(tabLayout,pokeInfosViewPager) {
                    tab, position ->
                when(position) {
                    0 -> {
                        tab.text = "Pokemon Details"
                    }
                    1 -> tab.text = "Pokemon Evolution"
                }
            }.attach()
            tabLayout.background = ColorDrawable(Color.WHITE)
        }
    }
}