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
import androidx.navigation.fragment.navArgs
import coil.decode.SvgDecoder
import coil.load
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.databinding.PokeDetailsLayoutBinding
import com.example.pokedex.ui.FragmentAdapter
import com.example.pokedex.utils.Resource
import com.example.pokedex.viewmodels.PokeDetailsSharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

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
        pokeViewModel.getSinglePokemonByName(pokemonName!!)
        pokeViewModel.apiCallResponse.observe(viewLifecycleOwner){response ->
            when(response){
                is Resource.Error -> Log.e("PokeDetailsFragment", "Error fetching pokemon")
                is Resource.Loading -> {
                    binding.apply {
                        progressBar.isVisible = true
                    }
                }
                is Resource.Success -> {
                    pokeViewModel.pokemonResponse.postValue(response.data)
                    fillPokemonDataOnScreen(response.data)
                }
            }
        }
    }

    private fun fillPokemonDataOnScreen(pokemon: Pokemon?) {
        binding.apply {
            binding.pokemonPhoto.load(pokemon?.getImageUrl()) {
                crossfade(500)
                decoderFactory { result, options, _ ->
                    SvgDecoder(result.source, options)
                }
            }
            progressBar.isVisible = false
            pokemonName.text = "${pokemonArgs.pokemonName?.capitalize()}"
            progressBar.isVisible = false
        }
    }

    private fun setUpPokeDetailsViewPager() {
        binding.apply {
            val adapter = FragmentAdapter(requireActivity())
            pokeInfosViewPager.adapter = adapter
            TabLayoutMediator(tabLayout, pokeInfosViewPager) { tab, position ->
                tab.apply {
                    when (position) {
                        0 -> {
                            text = "Poke Details"
                        }

                        1 -> {
                            text = "Poke Abilities"
                        }
                    }
                }

            }.attach()
            tabLayout.background = ColorDrawable(Color.WHITE)
        }
    }
}