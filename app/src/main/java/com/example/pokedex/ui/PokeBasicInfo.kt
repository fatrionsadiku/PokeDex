package com.example.pokedex.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.databinding.PokemonBasicInfoBinding
import kotlin.random.Random

class PokeBasicInfo : Fragment() {
    lateinit var binding: PokemonBasicInfoBinding
    lateinit var viewModel: PokemonDetailsViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PokemonBasicInfoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[PokemonDetailsViewModel::class.java]
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            viewModel.pokemonResponse.observe(viewLifecycleOwner) {
                Log.d("POKE DEBUG YAY", "onViewCreated: ${it.toString()}")
                pokeBaseXP.text = it.baseEXP.toString()
                pokeWeight.text = "${it.weight}kg"
                pokeHeight.text = "${it.height}m"
                pokeStatsDummyData()
                showPokemonTypes(it)
            }
        }

    }

    private fun pokeStatsDummyData() {
        val currentHp = Random.nextDouble() * (300 - 100) + 100
        val currentAttack = Random.nextDouble() * (300 - 100) + 100
        val currentDef = Random.nextDouble() * (300 - 100) + 100
        binding.apply {
            progressHp.progress = currentHp.toFloat()
            progressHp.labelText = "${currentHp.toInt()}/${progressHp.max.toInt()}"
            progressAttack.progress = currentAttack.toFloat()
            progressAttack.labelText = "${currentAttack.toInt()}/${progressAttack.max.toInt()}"
            progressDef.progress = currentDef.toFloat()
            progressDef.labelText = "${currentDef.toInt()}/${progressDef.max.toInt()}"
        }
    }

    private fun showPokemonTypes(pokemon: Pokemon) {
        binding.linearLayout.removeAllViews()
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        ).apply {
            setMargins(16)
        }
        binding.apply {
            pokemon.types.forEach { pokemonType ->
                val pokeType = pokemonType.type.name.capitalize()
                val currentPokeColor = getColorForType(pokeType)
                val currentPokeType = CustomCardView(requireContext(),pokeType)
                currentPokeType.setBackgroundColor(currentPokeColor)
                linearLayout.addView(currentPokeType,layoutParams)
            }
        }

    }
    private fun getColorForType(type: String): Int {
        return when (type) {
            "Normal" -> Color.parseColor("#A8A878")
            "Fighting" -> Color.parseColor("#C03028")
            "Flying" -> Color.parseColor("#A890F0")
            "Poison" -> Color.parseColor("#A040A0")
            "Ground" -> Color.parseColor("#E0C068")
            "Rock" -> Color.parseColor("#B8A038")
            "Bug" -> Color.parseColor("#A8B820")
            "Ghost" -> Color.parseColor("#705898")
            "Steel" -> Color.parseColor("#B8B8D0")
            "Fire" -> Color.parseColor("#F08030")
            "Water" -> Color.parseColor("#6890F0")
            "Grass" -> Color.parseColor("#78C850")
            "Electric" -> Color.parseColor("#F8D030")
            "Psychic" -> Color.parseColor("#F85888")
            "Ice" -> Color.parseColor("#98D8D8")
            "Dragon" -> Color.parseColor("#7038F8")
            "Dark" -> Color.parseColor("#705848")
            "Fairy" -> Color.parseColor("#EE99AC")
            else -> Color.WHITE // default color
        }
    }

//    fun showPokemonTypes(pokemon : Pokemon) {
//        binding.apply {
//            firstStat.apply {
//                firstStatName.text = pokemon.types.first().type.name
//            }
//            if (pokemon.types[1] != null){
//                secondStatName.text = pokemon.types[1].type.name
//            } else
//                secondStat.isVisible = false
//
//            if (pokemon.types[2] != null){
//                thirdStatName.text = pokemon.types[2].type.name
//            }else
//                thirdStat.isVisible = false
//        }
//    }
}