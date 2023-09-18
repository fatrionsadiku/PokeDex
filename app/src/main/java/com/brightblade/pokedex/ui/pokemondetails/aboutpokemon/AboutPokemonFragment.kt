package com.brightblade.pokedex.ui.pokemondetails.aboutpokemon

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.FragmentAboutPokemonBinding
import com.brightblade.pokedex.ui.pokemondetails.PokeDetailsSharedViewModel
import com.brightblade.utils.Resource
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AboutPokemonFragment : Fragment(R.layout.fragment_about_pokemon) {
    val binding by viewBinding(FragmentAboutPokemonBinding::bind)
    private val pokeViewModel: PokeDetailsSharedViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pokeViewModel.pokemonDescription.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error   -> Unit
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    val stringBuilder = StringBuilder()
                    response.data?.forEach { pokeDescription ->
                        stringBuilder.append(
                            pokeDescription
                                .replace("\n", " ")
                                .replace(".", ".\n")
//                                Had to use this last replace in order to remove an ASCII character from showing up
                                .replace("\u000C", " ")
                        )
                    }
                    Timber.d(stringBuilder.toString())
                    binding.pokemonBio.text = stringBuilder.toString()
                }
            }
        }

    }
}