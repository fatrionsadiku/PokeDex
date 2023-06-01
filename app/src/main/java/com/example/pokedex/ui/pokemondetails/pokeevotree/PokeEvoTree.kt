package com.example.pokedex.ui.pokemondetails.pokeevotree

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.LayoutDirection
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.decode.GifDecoder
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.example.pokedex.R
import com.example.pokedex.data.RedirectState
import com.example.pokedex.data.models.PokemonEvolutionChain
import com.example.pokedex.databinding.EvoTreeSettingsBinding
import com.example.pokedex.databinding.PokeEvoTreeLayoutBinding
import com.example.pokedex.ui.pokemondetails.PokeDetailsFragment
import com.example.pokedex.ui.pokemondetails.pokeabilities.PokeAbilities
import com.example.pokedex.utils.Utility.getPokemonID
import com.example.pokedex.utils.Utility.linearLayoutParams
import com.example.pokedex.utils.Utility.pokeNameParams
import com.example.pokedex.utils.capitalize
import com.example.pokedex.viewmodels.PokeDetailsSharedViewModel
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokeEvoTree : Fragment() {
    private lateinit var binding: PokeEvoTreeLayoutBinding
    private lateinit var viewModel: PokeDetailsSharedViewModel
    private lateinit var toggleButton: LottieAnimationView
    private var isSwitchOn = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PokeEvoTreeLayoutBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[PokeDetailsSharedViewModel::class.java]
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleBackToDetails()

        binding.apply {
            viewModel.pokemonSpeciesResponse.apply {
                observe(viewLifecycleOwner) { pokeSpecies ->
                    pokeSpecies?.let {
                        getPokemonEvoTree(it)
                    }
                }
            }
            toggleButton.setOnClickListener {
                it as LottieAnimationView
                it.playAnimation()
                setUpSettingsBalloon(it)
            }
        }
    }


    // Will soon implement custom views to reduce boiler plate code as it's pretty unreadable as is
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPokemonEvoTree(pokeSpecies: PokemonEvolutionChain) {
        binding.linearLayout.removeAllViews()
        val pokeDetailsFragment =
            this.parentFragment as PokeDetailsFragment
        val pokeAbilitiesFragment = parentFragmentManager.findFragmentByTag("f1") as? PokeAbilities
        val pokeImageParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.image_width),
            resources.getDimensionPixelSize(R.dimen.image_height),
            0.5f
        ).also {
            it.gravity = Gravity.CENTER_HORIZONTAL
        }
        val neonPointingGif = LottieAnimationView(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                resources.getDimensionPixelSize(R.dimen.pointing_up_width),
                resources.getDimensionPixelSize(R.dimen.pointing_up_height),
                0.2f
            ).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
            }
            alpha = 0f
            animate().setDuration(500).alpha(1f).start()
            rotation = 90f
            setAnimation(R.raw.pixel_neon_arrow)
            repeatCount = 999
            playAnimation()
        }
        val firstPokemonId = getPokemonID(pokeSpecies.chain.species.url)
        val firstPokeForm = ImageView(requireContext()).apply {
            this.layoutParams = pokeImageParams
            load(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${
                    firstPokemonId
                }.png"
            ) {
                crossfade(500)
            }
            setOnClickListener {
                val pokeId = getPokemonID(pokeSpecies.chain.species.url)
                val pokeName = pokeSpecies.chain.species.name
                pokeAbilitiesFragment?.binding?.pokeDetailsHolder?.removeAllViews()
                pokeDetailsFragment.getPokemonDetails(pokeName, pokeId)
                viewLifecycleOwner.lifecycleScope.launch {
                    if (viewModel.preferencesFlow.first().redirectState == RedirectState.REDIRECT_TO_DETAILS) {
                        pokeDetailsFragment.binding.pokeInfosViewPager.currentItem = 0
                    }
                }
            }
        }
        val firstPokeName = TextView(requireContext()).apply {
            layoutParams = pokeNameParams
            text = pokeSpecies.chain.species.name.capitalize()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#759EA1"))
            typeface = resources.getFont(R.font.ryogothic)
        }
        val firstPokeFormLayout = LinearLayout(requireContext()).apply {
            layoutParams = linearLayoutParams
            orientation = LinearLayout.VERTICAL
            addView(firstPokeForm)
            addView(firstPokeName)
            if (pokeDetailsFragment.currentPokemonId == firstPokemonId) {
                addView(neonPointingGif)
            }

        }
        binding.linearLayout.addView(firstPokeFormLayout)
        pokeSpecies.chain.evoDetails.forEach { evoForm ->
            val secondPokemonId = getPokemonID(evoForm.species.url)
            val secondPokemonForm = ImageView(requireContext()).apply {
                this.layoutParams = pokeImageParams
                load(
                    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${
                        secondPokemonId
                    }.png"
                ) {
                    crossfade(500)
                }
                setOnClickListener {
                    val pokeId = getPokemonID(evoForm.species.url)
                    pokeAbilitiesFragment?.binding?.pokeDetailsHolder?.removeAllViews()
                    pokeDetailsFragment.getPokemonDetails(evoForm.species.name, pokeId)
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (viewModel.preferencesFlow.first().redirectState == RedirectState.REDIRECT_TO_DETAILS) {
                            pokeDetailsFragment.binding.pokeInfosViewPager.currentItem = 0
                        }
                    }

                }
            }
            val secondPokeName = TextView(requireContext()).apply {
                layoutParams = pokeNameParams
                text = evoForm.species.name.capitalize()
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(Color.parseColor("#759EA1"))
                typeface = resources.getFont(R.font.ryogothic)
            }
            val secondPokeFormLayout = LinearLayout(requireContext()).apply {
                layoutParams = linearLayoutParams
                orientation = LinearLayout.VERTICAL
                addView(secondPokemonForm)
                addView(secondPokeName)
                if (pokeDetailsFragment.currentPokemonId == secondPokemonId) {
                    addView(neonPointingGif)
                }

            }
            binding.linearLayout.addView(secondPokeFormLayout)
            if (evoForm.evoDetails.isNotEmpty()) {
                val thirdPokemonId = getPokemonID(evoForm.evoDetails[0]?.species?.url!!)
                val thirdPokeForm = ImageView(requireContext()).apply {
                    this.layoutParams = pokeImageParams
                    load(
                        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${
                            thirdPokemonId
                        }.png"
                    ) {
                        crossfade(500)
                    }
                    setOnClickListener {
                        val pokeId = getPokemonID(evoForm.evoDetails[0]?.species?.url!!)
                        pokeAbilitiesFragment?.binding?.pokeDetailsHolder?.removeAllViews()
                        pokeDetailsFragment.getPokemonDetails(
                            evoForm.evoDetails[0]?.species?.name!!,
                            pokeId
                        )
                        viewLifecycleOwner.lifecycleScope.launch {
                            if (viewModel.preferencesFlow.first().redirectState == RedirectState.REDIRECT_TO_DETAILS) {
                                pokeDetailsFragment.binding.pokeInfosViewPager.currentItem = 0
                            }
                        }

                    }
                }
                val thirdPokeName = TextView(requireContext()).apply {
                    layoutParams = pokeNameParams
                    text = evoForm.evoDetails[0]?.species?.name?.capitalize()
                    gravity = Gravity.CENTER_VERTICAL
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                    setTextColor(Color.parseColor("#759EA1"))
                    typeface = resources.getFont(R.font.ryogothic)
                }
                val thirdPokeFormLayout = LinearLayout(requireContext()).apply {
                    layoutParams = linearLayoutParams
                    orientation = LinearLayout.VERTICAL
                    addView(thirdPokeForm)
                    addView(thirdPokeName)
                    if (pokeDetailsFragment.currentPokemonId == thirdPokemonId) {
                        addView(neonPointingGif)
                    }
                }
                binding.linearLayout.addView(thirdPokeFormLayout)
            }
        }
    }

    private fun toggleBackToDetails() {
        binding.apply {
            toggleButton.setOnClickListener {
                if (isSwitchOn) {
                    toggleButton.setMinAndMaxProgress(0.5f, 1.0f)
                    toggleButton.playAnimation()
                    isSwitchOn = false
                    viewModel.onRedirectStateSelecetd(RedirectState.REDIRECT_TO_EVOTREE)
                } else {
                    toggleButton.setMinAndMaxProgress(0.0f, 0.5f)
                    toggleButton.playAnimation()
                    isSwitchOn = true
                    viewModel.onRedirectStateSelecetd(RedirectState.REDIRECT_TO_DETAILS)
                }
            }
        }
    }

    private fun setUpSettingsBalloon(view: View) {
        val dialogBinding = EvoTreeSettingsBinding.inflate(LayoutInflater.from(requireContext()))
        val balloon = Balloon.Builder(requireContext())
            .setLayout(dialogBinding.root)
            .setArrowSize(10)
            .setArrowOrientation(ArrowOrientation.TOP)
            .setArrowPosition(0.5f)
            .setWidthRatio(0.55f)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setCornerRadius(4f)
            .setMarginRight(20)
            .setOnBalloonDismissListener {
                view as LottieAnimationView
                view.playAnimation()
            }
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setBalloonAnimation(BalloonAnimation.FADE)
            .build()
        toggleButton =
            balloon.getContentView().findViewById(R.id.toggleButton)
        viewLifecycleOwner.lifecycleScope.launch {
            isSwitchOn = when (viewModel.preferencesFlow.first().redirectState) {
                RedirectState.REDIRECT_TO_DETAILS -> {
                    toggleButton.setMinAndMaxProgress(0.5f, 1.0f)
                    true
                }

                RedirectState.REDIRECT_TO_EVOTREE -> {
                    toggleButton.setMinAndMaxProgress(0f, 0.5f)
                    false
                }
            }
        }
        toggleButton.setOnClickListener {
            toggleButton.setOnClickListener {
                if (isSwitchOn) {
                    toggleButton.setMinAndMaxProgress(0.5f, 1.0f)
                    toggleButton.playAnimation()
                    isSwitchOn = false
                    viewModel.onRedirectStateSelecetd(RedirectState.REDIRECT_TO_EVOTREE)
                } else {
                    toggleButton.setMinAndMaxProgress(0.0f, 0.5f)
                    toggleButton.playAnimation()
                    isSwitchOn = true
                    viewModel.onRedirectStateSelecetd(RedirectState.REDIRECT_TO_DETAILS)
                }
            }
        }
        balloon.showAlignBottom(view)
    }

}
