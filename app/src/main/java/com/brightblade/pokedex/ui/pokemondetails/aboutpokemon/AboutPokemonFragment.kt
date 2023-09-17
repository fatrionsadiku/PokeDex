package com.brightblade.pokedex.ui.pokemondetails.aboutpokemon

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.FragmentAboutPokemonBinding
import com.brightblade.pokedex.ui.pokemondetails.PokeDetailsSharedViewModel
import com.brightblade.utils.Resource
import com.brightblade.utils.capitalize
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutPokemonFragment : Fragment(R.layout.fragment_about_pokemon) {
    val binding by viewBinding(FragmentAboutPokemonBinding::bind)
    private val pokeViewModel: PokeDetailsSharedViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pokeViewModel.singlePokemonResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error   -> binding.aboutMeText.text = "Hello Porld"
                is Resource.Loading -> Unit
                is Resource.Success -> binding.aboutMeText.text = response.data?.name?.capitalize()
            }
        }

    }
}