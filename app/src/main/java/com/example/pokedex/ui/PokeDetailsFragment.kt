package com.example.pokedex.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.databinding.PokeDetailsLayoutBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonDisposableHandle.parent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pixplicity.sharp.Sharp

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
        setUpPokeDetailsViewPager()

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
            binding.progressBar.isVisible = true
            binding.pokemonPhoto.isVisible = false
            Log.d("SVG Url", "${pokemon.getImageUrl()}")
            Sharp.loadFromNetwork(pokemon.getImageUrl()).getSharpPicture(Sharp.PictureCallback {
                pokemonPhoto.setImageDrawable(it.drawable)
            })
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
                    0 -> tab.text = "Pokemon Details"
                    1 -> tab.text = "Pokemon Evolution"
                }
            }.attach()
            tabLayout.background = ColorDrawable(Color.WHITE)
        }
    }
}