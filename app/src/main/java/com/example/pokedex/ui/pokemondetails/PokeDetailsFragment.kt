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
import com.example.pokedex.databinding.PokeDetailsLayoutBinding
import com.example.pokedex.adapters.FragmentAdapter
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.capitalize
import com.example.pokedex.viewmodels.PokeDetailsSharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokeDetailsFragment : Fragment() {
    lateinit var binding: PokeDetailsLayoutBinding
    val pokemonArgs: PokeDetailsFragmentArgs by navArgs()
    var currentPokemonId : Int = 0
    private lateinit var pokeViewModel: PokeDetailsSharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PokeDetailsLayoutBinding.inflate(inflater, container, false)
        pokeViewModel = ViewModelProvider(requireActivity())[PokeDetailsSharedViewModel::class.java]
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().viewModelStore.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pokemonPhoto.setImageResource(0)
        getPokemonDetails(pokemonArgs.pokemonName!!, pokemonArgs.pokemonId)
        setUpPokeDetailsViewPager()

    }

    fun getPokemonDetails(pokemonName : String,pokeId : Int) {
        pokeViewModel.getSinglePokemonByName(pokemonName)
        pokeViewModel.apiCallResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error -> Log.e("PokeDetailsFragment", "Error fetching pokemon")
                is Resource.Loading -> {
                    showProgressBar()
                }

                is Resource.Success -> {
                    currentPokemonId = pokeId
                    hideProgressBar()
                    fillPokemonDataOnScreen(pokemonName,pokeId)
                    pokeViewModel.getPokemonSpeciesId(pokemonArgs.pokemonId)
                    pokeViewModel.pokemonResponse.postValue(response.data)
                }
            }
        }
    }

    private fun fillPokemonDataOnScreen(pokeName: String, pokeId : Int) {
        binding.apply {
            pokemonPhoto.load(getImageUrl(pokeId)) {
                crossfade(500)
                decoderFactory { result, options, _ ->
                    SvgDecoder(result.source, options)
                }.lifecycle(viewLifecycleOwner)
            }
            progressBar.isVisible = false
            pokemonName.text = "${pokeName.capitalize()}"
            progressBar.isVisible = false
        }
    }

    private fun getImageUrl(id: Int) =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/dream-world/$id.svg"

    private fun setUpPokeDetailsViewPager() {
        binding.apply {
            val adapter = FragmentAdapter(childFragmentManager,viewLifecycleOwner.lifecycle)
            pokeInfosViewPager.adapter = adapter
            TabLayoutMediator(tabLayout, pokeInfosViewPager) { tab, position ->
                tab.apply {
                    when (position) {
                        0 -> {
                            text = "Details"
                        }

                        1 -> {
                            text = "Abilities/Items"
                        }

                        2 -> {
                            text = "Evolution Tree"
                        }
                    }
                }

            }.attach()
            tabLayout.background = ColorDrawable(Color.WHITE)
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}