package com.brightblade.pokedex.ui.pokemondetails.pokeevotree

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.PokemonEvolutionChain
import com.brightblade.pokedex.data.persistent.RedirectState
import com.brightblade.pokedex.databinding.DialogEvolutionTreeSettingsBinding
import com.brightblade.pokedex.databinding.FragmentPokemonEvolutionTreeBinding
import com.brightblade.pokedex.ui.pokemondetails.PokeDetailsFragment
import com.brightblade.pokedex.ui.pokemondetails.PokeDetailsSharedViewModel
import com.brightblade.pokedex.ui.pokemondetails.pokeabilities.PokeAbilities
import com.brightblade.utils.Resource
import com.brightblade.utils.Utility.getPokemonID
import com.brightblade.utils.Utility.linearLayoutParams
import com.brightblade.utils.Utility.pokeNameParams
import com.brightblade.utils.capitalize
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class PokeEvoTree : Fragment(R.layout.fragment_pokemon_evolution_tree) {
    val binding by viewBinding(FragmentPokemonEvolutionTreeBinding::bind)
    private val viewModel: PokeDetailsSharedViewModel by activityViewModels()
    private lateinit var toggleButton: LottieAnimationView
    private var isSwitchOn = false

    override fun onDestroyView() {
        binding.linearLayout.removeAllViews()
        super.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toggleBackToDetails()
        binding.apply {
            viewModel.pokemonSpeciesResponse.observe(viewLifecycleOwner) { evoChainResponse ->
                when (evoChainResponse) {
                    is Resource.Error   -> {
                        hideProgressBar()
                        Log.e("EvoTree", evoChainResponse.message.toString())
                        binding.linearLayout.removeAllViews()
                        binding.noOtherForms.visibility = View.VISIBLE
                        binding.linearLayout.visibility = View.GONE
                    }

                    is Resource.Loading -> showProgressBar()
                    is Resource.Success -> {
                        hideProgressBar()
                        evoChainResponse.data?.let { evoChain ->
                            if (evoChain.chain.evoDetails.isNullOrEmpty()) {
                                binding.noOtherForms.visibility = View.VISIBLE
                                binding.linearLayout.visibility = View.GONE
                            } else {
                                getPokemonEvoTree(evoChain)
                                binding.noOtherForms.visibility = View.GONE
                                binding.linearLayout.visibility = View.VISIBLE
                            }

                        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getPokemonEvoTree(pokeSpecies: PokemonEvolutionChain) {
        binding.linearLayout.removeAllViews()
        val pokeDetailsFragment = parentFragment as PokeDetailsFragment
        val pokeAbilitiesFragment =
            parentFragmentManager.findFragmentByTag("f2") as? PokeAbilities
        val neonPointingGif = createNeonPointingGif()
        val firstPokemonId = getPokemonID(pokeSpecies.chain.species.url)
        val firstPokeName = createPokemonNameTextView(pokeSpecies.chain.species.name.capitalize())
        val firstPokeForm = createPokemonFormImageView(
            pokemonId = firstPokemonId,
            pokeName = firstPokeName.text.toString().lowercase(Locale.ROOT),
            pokeDetailsFragment = pokeDetailsFragment,
            pokeAbilitiesFragment = pokeAbilitiesFragment!!
        )
        val firstPokeFormLayout = createPokemonFormLayout(
            pokemonForm = firstPokeForm,
            pokemonName = firstPokeName,
            neonPointingGif = neonPointingGif,
            currentPokemonId = firstPokemonId,
            pokeDetailsFragment = pokeDetailsFragment
        )
        binding.linearLayout.addView(firstPokeFormLayout)
        pokeSpecies.chain.evoDetails.forEach { evoForm ->
            val secondPokemonId = getPokemonID(evoForm.species.url)
            val secondPokeName = createPokemonNameTextView(evoForm.species.name.capitalize())
            val secondPokemonForm = createPokemonFormImageView(
                pokemonId = secondPokemonId,
                pokeName = secondPokeName.text.toString().lowercase(Locale.ROOT),
                pokeDetailsFragment = pokeDetailsFragment,
                pokeAbilitiesFragment = pokeAbilitiesFragment
            )
            val secondPokeFormLayout = createPokemonFormLayout(
                pokemonForm = secondPokemonForm,
                pokemonName = secondPokeName,
                neonPointingGif = neonPointingGif,
                currentPokemonId = secondPokemonId,
                pokeDetailsFragment = pokeDetailsFragment
            )
            binding.linearLayout.addView(secondPokeFormLayout)
            if (evoForm.evoDetails.isNotEmpty()) {
                val thirdPokemonId = getPokemonID(evoForm.evoDetails[0]?.species?.url!!)
                val thirdPokeName =
                    createPokemonNameTextView(evoForm.evoDetails[0]?.species?.name?.capitalize())
                val thirdPokeForm = createPokemonFormImageView(
                    pokemonId = thirdPokemonId,
                    pokeName = thirdPokeName.text.toString().lowercase(Locale.ROOT),
                    pokeDetailsFragment = pokeDetailsFragment,
                    pokeAbilitiesFragment = pokeAbilitiesFragment
                )
                val thirdPokeFormLayout = createPokemonFormLayout(
                    pokemonForm = thirdPokeForm,
                    pokemonName = thirdPokeName,
                    neonPointingGif = neonPointingGif,
                    currentPokemonId = thirdPokemonId,
                    pokeDetailsFragment = pokeDetailsFragment
                )
                binding.linearLayout.addView(thirdPokeFormLayout)
            }
        }
    }

    private fun createPokemonFormLayout(
        pokemonForm: ImageView,
        pokemonName: TextView,
        neonPointingGif: LottieAnimationView,
        currentPokemonId: Int,
        pokeDetailsFragment: PokeDetailsFragment,
    ): LinearLayout {
        return LinearLayout(requireContext()).apply {
            layoutParams = linearLayoutParams
            orientation = LinearLayout.VERTICAL
            addView(pokemonForm)
            addView(pokemonName)
            if (pokeDetailsFragment.currentPokemonId == currentPokemonId) {
                addView(neonPointingGif)
            }
        }
    }

    private fun createNeonPointingGif(): LottieAnimationView {
        return LottieAnimationView(requireContext()).apply {
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
    }

    private fun createPokemonFormImageView(
        pokemonId: Int,
        pokeName: String,
        pokeDetailsFragment: PokeDetailsFragment,
        pokeAbilitiesFragment: PokeAbilities,
    ): ImageView {
        val pokeImageParams = LinearLayout.LayoutParams(
            resources.getDimensionPixelSize(R.dimen.pokeform_image_width),
            resources.getDimensionPixelSize(R.dimen.pokeform_image_height),
            0.5f
        ).also {
            it.gravity = Gravity.CENTER_HORIZONTAL
        }
        return ImageView(requireContext()).apply {
            layoutParams = pokeImageParams
            load(
                "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokemonId.png"
            ) {
                allowHardware(false)
                crossfade(500)
            }
            setOnClickListener {
                pokeDetailsFragment.getPokemonDetails(pokemonId)
                pokeAbilitiesFragment.binding.apply {
                    pokeItemsHolder.removeAllViews()
                    pokeDetailsHolder.removeAllViews()
                }
                viewLifecycleOwner.lifecycleScope.launch {
                    if (viewModel.preferencesFlow.first().redirectState == RedirectState.REDIRECT_TO_DETAILS) {
                        pokeDetailsFragment.binding.pokeInfosViewPager.currentItem = 0
                    }
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPokemonNameTextView(name: String?): TextView {
        return TextView(requireContext()).apply {
            layoutParams = pokeNameParams
            text = name?.capitalize()
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(Color.parseColor("#759EA1"))
            typeface = resources.getFont(R.font.ryogothic)
        }
    }

    private fun toggleBackToDetails() {
        binding.apply {
            toggleButton.setOnClickListener {
                if (isSwitchOn) {
                    toggleButton.setMinAndMaxProgress(0.5f, 1.0f)
                    toggleButton.playAnimation()
                    isSwitchOn = false
                    viewModel.onRedirectStateSelected(RedirectState.REDIRECT_TO_EVOTREE)
                } else {
                    toggleButton.setMinAndMaxProgress(0.0f, 0.5f)
                    toggleButton.playAnimation()
                    isSwitchOn = true
                    viewModel.onRedirectStateSelected(RedirectState.REDIRECT_TO_DETAILS)
                }
            }
        }
    }

    private fun setUpSettingsBalloon(view: View) {
        val dialogBinding =
            DialogEvolutionTreeSettingsBinding.inflate(LayoutInflater.from(requireContext()))
        val balloon = createBalloon(dialogBinding.root, view)
        toggleButton = balloon.getContentView().findViewById(R.id.toggleButton)
        viewLifecycleOwner.lifecycleScope.launch {
            when (viewModel.preferencesFlow.first().redirectState) {
                RedirectState.REDIRECT_TO_DETAILS -> {
                    toggleButton.setMinAndMaxProgress(0.5f, 1.0f)
                    isSwitchOn = true
                }

                RedirectState.REDIRECT_TO_EVOTREE -> {
                    toggleButton.setMinAndMaxProgress(0f, 0.5f)
                    isSwitchOn = false
                }
            }
        }
        toggleButton.setOnClickListener {
            val newRedirectState = if (isSwitchOn) {
                toggleButton.setMinAndMaxProgress(0.5f, 1.0f)
                toggleButton.playAnimation()
                RedirectState.REDIRECT_TO_EVOTREE
            } else {
                toggleButton.setMinAndMaxProgress(0.0f, 0.5f)
                toggleButton.playAnimation()
                RedirectState.REDIRECT_TO_DETAILS
            }
            isSwitchOn = !isSwitchOn
            viewModel.onRedirectStateSelected(newRedirectState)
        }

        balloon.showAlignBottom(view)
    }

    private fun createBalloon(contentView: View, view: View): Balloon {
        return Balloon.Builder(requireContext())
            .setLayout(contentView)
            .setArrowSize(10)
            .setArrowOrientation(ArrowOrientation.TOP)
            .setArrowPosition(0.5f)
            .setWidthRatio(0.55f)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setCornerRadius(4f)
            .setMarginRight(20)
            .setOnBalloonDismissListener {
                (view as LottieAnimationView).playAnimation()
            }
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            .setBalloonAnimation(BalloonAnimation.FADE)
            .build()
    }

    private fun hideProgressBar() {
        binding.loadingAnimation.visibility = View.INVISIBLE
        binding.linearLayout.visibility = View.VISIBLE
        binding.noOtherForms.visibility = View.VISIBLE
        binding.loadingAnimation.cancelAnimation()
    }

    private fun showProgressBar() {
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.linearLayout.visibility = View.INVISIBLE
        binding.noOtherForms.visibility = View.GONE
        binding.loadingAnimation.playAnimation()
    }
}



