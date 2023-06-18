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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import coil.decode.SvgDecoder
import coil.load
import com.example.pokedex.adapters.FragmentAdapter
import com.example.pokedex.data.HideDetails
import com.example.pokedex.databinding.PokeDetailsLayoutBinding
import com.example.pokedex.ui.PokeDetailsSharedViewModel
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.capitalize
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokeDetailsFragment : Fragment() {
    lateinit var binding: PokeDetailsLayoutBinding
    private val pokemonArgs: PokeDetailsFragmentArgs by navArgs()
    var currentPokemonId: Int = 0
    private var hideDetails = false
    private val pokeViewModel: PokeDetailsSharedViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = PokeDetailsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pokemonPhoto.setImageResource(0)
        getPokemonDetails(pokemonArgs.pokemonName!!, pokemonArgs.pokemonId)
        setUpPokeDetailsViewPager()
        setUpDetailsState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pokeViewModel.pokemonDescription.value = null
    }

    fun getPokemonDetails(pokemonName: String, pokeId: Int) {
        pokeViewModel.getSinglePokemonByName(pokemonName, pokeId)
        pokeViewModel.singlePokemonResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error   -> Log.e("PokeDetailsFragment", "Error fetching pokemon")
                is Resource.Loading -> {
                    showProgressBar()
                }
                is Resource.Success -> {
                    currentPokemonId = pokeId
                    hideProgressBar()
                    fillPokemonDataOnScreen(pokemonName, pokeId)
                }
            }
        }
        pokeViewModel.pokemonDescription.observe(viewLifecycleOwner) { descriptionResponse ->
            when (descriptionResponse) {
                is Resource.Error   -> {
                    binding.pokemonDescription.text =
                        "Whoops, this pokemon's bio seems to be missing\n we apologize for the inconvenience"
                }
                is Resource.Loading -> {
                    binding.pokemonDescription.text = ""
                    if (hideDetails){
                        binding.pokeDescriptionLoadingAnimation.apply {
                            visibility = View.VISIBLE
                            playAnimation()
                        }
                    }
                }
                is Resource.Success -> {
                    lifecycleScope.launch {
                        delay(500)
                        binding.pokeDescriptionLoadingAnimation.apply {
                            visibility = View.INVISIBLE
                            cancelAnimation()
                        }
                        val stringBuilder = StringBuilder()
                        descriptionResponse.data?.forEach { pokeDescription ->
                            stringBuilder.append(pokeDescription.replace("\n", " ").replace(".", ".\n"))
                                .append("\n")
                        }
                        binding.pokemonDescription.text = stringBuilder.toString()
                    }


                }
            }
        }
    }

    private fun fillPokemonDataOnScreen(pokeName: String, pokeId: Int) {
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
            val adapter = FragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
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

    private fun setUpDetailsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (pokeViewModel.hideDetailsFlow.first().detailsState) {
                HideDetails.SHOW_ONLY_POKEMON -> {
                    binding.hideDetailsButton.progress = 1f
                    binding.topPartPokeDetails.visibility = View.INVISIBLE
                    binding.pokemonDescription.visibility = View.VISIBLE
                    hideDetails = true
                }

                HideDetails.SHOW_ALL_DETAILS  -> {
                    binding.hideDetailsButton.progress = 0f
                    binding.topPartPokeDetails.visibility = View.VISIBLE
                    binding.pokemonDescription.visibility = View.INVISIBLE
                    hideDetails = false
                }
            }
        }
        binding.hideDetailsButton.setOnClickListener {
            if (hideDetails) {
                pokeViewModel.onHideDetailsStateChanged(HideDetails.SHOW_ALL_DETAILS)
                binding.hideDetailsButton.progress = 0f
                binding.pokemonDescription.apply {
                    alpha = 1f
                    animate().apply {
                        visibility = View.INVISIBLE
                        duration = 500
                        alpha(0f)
                    }.start()
                }
                binding.topPartPokeDetails.apply {
                    alpha = 0f
                    animate().apply {
                        visibility = View.VISIBLE
                        duration = 500
                        alpha(1f)
                    }.start()
                    hideDetails = !hideDetails
                }
            } else {
                pokeViewModel.onHideDetailsStateChanged(HideDetails.SHOW_ONLY_POKEMON)
                binding.hideDetailsButton.progress = 1f
                binding.pokemonDescription.apply {
                    alpha = 0f
                    animate().apply {
                        visibility = View.VISIBLE
                        duration = 500
                        alpha(1f)
                    }.start()
                }
                binding.topPartPokeDetails.apply {
                    alpha = 1f
                    animate().apply {
                        visibility = View.INVISIBLE
                        duration = 500
                        alpha(0f)
                    }.start()
                    hideDetails = !hideDetails
                }
            }
        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }
}