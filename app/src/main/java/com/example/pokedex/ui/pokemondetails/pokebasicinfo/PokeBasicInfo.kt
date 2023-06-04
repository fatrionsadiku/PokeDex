package com.example.pokedex.ui.pokemondetails.pokebasicinfo

import android.annotation.SuppressLint
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
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.capitalize
import com.example.pokedex.utils.customviews.CustomCardView
import com.example.pokedex.viewmodels.PokeDetailsSharedViewModel
import com.skydoves.rainbow.Rainbow
import com.skydoves.rainbow.RainbowOrientation
import com.skydoves.rainbow.color
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokeBasicInfo : Fragment() {
    private lateinit var binding: PokemonBasicInfoBinding
    private lateinit var viewModel: PokeDetailsSharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PokemonBasicInfoBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[PokeDetailsSharedViewModel::class.java]
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            viewModel.apiCallResponse.apply {
                observe(viewLifecycleOwner) { apiResponse ->
                  when(apiResponse){
                      is Resource.Error -> Log.e("Error fetching data", apiResponse.message.toString())
                      is Resource.Loading -> Log.d("Loading...","Loading...")
                      is Resource.Success -> {
                          val pokemon = apiResponse.data
                          pokemon?.let {
                              Log.d("POKE DEBUG YAY", "onViewCreated: $it")
                              pokeBaseXP.text = it.baseEXP.toString()
                              pokeWeight.text = "${it.weight}kg"
                              pokeHeight.text = "${it.height}m"
                              showPokemonTypes(pokemon)
                              showPokemonStats(pokemon)
                          }

                      }
                  }
                }
                this.value = null
            }
        }

    }

    private fun showPokemonStats(pokemon: Pokemon) {
        pokemon.stats.forEach {
            val pokeStatName = it.stat.name
            binding.apply {
                when (pokeStatName) {
                    "hp" -> {
                        val currentHp = it.baseStat
                        progressHp.progress = currentHp.toFloat()
                        progressHp.labelText = "${currentHp}/${progressHp.max.toInt()}"
                    }
                    "attack" -> {
                        val currentAttack = it.baseStat
                        progressAttack.progress = currentAttack.toFloat()
                        progressAttack.labelText = "${currentAttack}/${progressAttack.max.toInt()}"
                    }
                    "defense" -> {
                        val currentDefense = it.baseStat
                        progressDef.progress = currentDefense.toFloat()
                        progressDef.labelText = "${currentDefense}/${progressAttack.max.toInt()}"
                    }
                    "speed" -> {
                        val currentSpeed = it.baseStat
                        progressSpeed.progress = currentSpeed.toFloat()
                        progressSpeed.labelText = "${currentSpeed}/${progressAttack.max.toInt()}"
                    }
                }
            }
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
                pokemonType.let {
                    val pokeType = pokemonType.type.name.capitalize()
                    val currentPokeColor = getColorForType(pokeType)
                    val currentPokeColorWithOpacity = Color.argb(160, Color.red(currentPokeColor), Color.green(currentPokeColor), Color.blue(currentPokeColor))
                    val currentPokeType = CustomCardView(requireContext(),pokeType)
                    Rainbow(currentPokeType).palette {
                        +color(currentPokeColor)
                        +color(currentPokeColorWithOpacity)
                    }.apply {
                        background(RainbowOrientation.BOTTOM_TOP, 14)
                    }
                    linearLayout.addView(currentPokeType,layoutParams)
                }
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
}