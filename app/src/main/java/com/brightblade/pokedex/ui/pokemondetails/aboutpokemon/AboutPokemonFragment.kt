package com.brightblade.pokedex.ui.pokemondetails.aboutpokemon

import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.PokeCharacteristics
import com.brightblade.pokedex.data.models.PokemonEncounters
import com.brightblade.pokedex.databinding.FragmentAboutPokemonBinding
import com.brightblade.pokedex.ui.pokemondetails.PokeDetailsSharedViewModel
import com.brightblade.utils.Resource
import com.brightblade.utils.Utility
import com.brightblade.utils.capitalize
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import com.zhuinden.livedatacombinetuplekt.combineTuple
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AboutPokemonFragment : Fragment(R.layout.fragment_about_pokemon) {
    val binding by viewBinding(FragmentAboutPokemonBinding::bind)
    private val pokeViewModel: PokeDetailsSharedViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        combineTuple(
            pokeViewModel.pokemonDescription,
            pokeViewModel.pokemonName,
            pokeViewModel.pokeCharacteristicsLiveData,
            pokeViewModel.pokeEncountersLiveData
        ).observe(viewLifecycleOwner) { (pokeDescriptions, pokemonName, pokeCharacteristics, pokeEncounters) ->
            when (pokeEncounters) {
                is Resource.Error   -> Timber.tag("PokemonEncounters").e(pokeEncounters.message)
                is Resource.Loading -> showProgressBar()
                is Resource.Success -> {
                    hideProgressBar()
                    handlePokemonEncounters(pokeEncounters.data!!)
                    handlePokemonTitle(pokemonName, pokeCharacteristics)
                    handlePokemonDescriptions(pokeDescriptions ?: emptyList())
                }
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlePokemonEncounters(pokemonEncounters: List<PokemonEncounters>) {
        binding.locationsLinearLayout.removeAllViews()
        if (pokemonEncounters.isNotEmpty()) {
            showLocationsLinearLayout()
            pokemonEncounters.forEachIndexed { index, encounter ->
                if (index > 3) return
                val encounterTextView =
                    createPokemonLocationTextView(index, encounter.locationArea.name)
                binding.locationsLinearLayout.addView(encounterTextView)
            }
        } else showNoLocationsImageView()
    }

    private fun handlePokemonTitle(pokeName: String?, pokeCharacteristics: PokeCharacteristics?) {
        val englishPokeCharacteristic = pokeCharacteristics?.pokemonDescriptionsList?.find {
            it.language.name == "en"
        }
        binding.titleText.text =
            if (englishPokeCharacteristic != null)
                "${pokeName?.capitalize()}, the one who's ${englishPokeCharacteristic?.description?.lowercase()}"
            else
                "${pokeName?.capitalize()}, the awesome one"

    }

    private fun handlePokemonDescriptions(pokeDescriptions: List<String>) {
        val stringBuilder = StringBuilder()
        pokeDescriptions.forEach { pokeDescription ->
            stringBuilder.append(
                pokeDescription
                    .replace("\n", " ")
                    .replace(".", ".\n")
                    //                      Had to use this last replace in order to remove an ASCII character from showing up
                    .replace("\u000C", " ")
            )
        }
        Timber.d(stringBuilder.toString())
        binding.pokemonBio.text = stringBuilder.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPokemonLocationTextView(index: Int, locationArea: String): TextView {
        val locationAreaName = locationArea.split("-").joinToString(" ") { it.capitalize() }
        return TextView(requireContext()).apply {
            layoutParams = Utility.pokeNameParams
            text = "Location no.$index : $locationAreaName"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(resources.getColor(R.color.black))
            typeface = resources.getFont(R.font.sailec_medium)
        }
    }

    private fun showLocationsLinearLayout() {
        binding.locationsLinearLayout.visibility = View.VISIBLE
        binding.hasNoLocations.visibility = View.GONE
    }

    private fun showNoLocationsImageView() {
        binding.locationsLinearLayout.visibility = View.GONE
        binding.hasNoLocations.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.loadingAnimation.visibility = View.INVISIBLE
        binding.locationsLinearLayout.visibility = View.VISIBLE
        binding.loadingAnimation.cancelAnimation()
    }

    private fun showProgressBar() {
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.locationsLinearLayout.visibility = View.INVISIBLE
        binding.hasNoLocations.visibility = View.GONE
        binding.titleText.text = "..."
        binding.pokemonBio.text = "..."
        binding.loadingAnimation.playAnimation()
    }
}