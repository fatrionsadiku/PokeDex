package com.brightblade.pokedex.ui.pokemondetails.pokeabilities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import coil.load
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.PokeAbilities
import com.brightblade.pokedex.data.models.PokeHeldItems
import com.brightblade.pokedex.databinding.DialogHeldItemBinding
import com.brightblade.pokedex.databinding.FragmentPokemonAbilitiesBinding
import com.brightblade.pokedex.ui.pokemondetails.PokeDetailsSharedViewModel
import com.brightblade.utils.Resource
import com.brightblade.utils.capitalize
import com.brightblade.utils.customviews.PokeAbilitiesLayout
import com.brightblade.utils.isViewVisible
import com.google.android.material.textview.MaterialTextView
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokeAbilities : Fragment(R.layout.fragment_pokemon_abilities) {
    val binding by viewBinding(FragmentPokemonAbilitiesBinding::bind)
    private val TAG = "PokeAbilitiesTag"
    private val viewModel: PokeDetailsSharedViewModel by activityViewModels()

    override fun onStop() {
        super.onStop()
        binding.pokeDetailsHolder.removeAllViews()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePokemonHeldItems()
        observePokemonAbilities()
    }

    private fun observePokemonAbilities() {
        binding.pokeDetailsHolder.removeAllViews()
        viewModel.abilitiesResponse.observe(viewLifecycleOwner) { pokeAbilityResponse ->
            when (pokeAbilityResponse) {
                is Resource.Error   -> {
                    hideProgressBar()
                    Log.e(TAG, "An error occurred while fetching abilities")
                }

                is Resource.Loading -> showProgressBar()
                is Resource.Success -> {
                    addPokemonAbilities(pokeAbilityResponse, pokeAbilityResponse.data!!)
                }
            }
        }
    }

    private fun observePokemonHeldItems() {
        viewModel.pokemonHeldItems.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error   -> Log.e("PokeAbilities", "${response.message}")
                is Resource.Loading -> {}
                is Resource.Success -> {
                    addHeldItems(response, response.data!!)
                }
            }

        }
    }

    private fun addHeldItems(
        response: Resource<List<PokeHeldItems?>>,
        data: List<PokeHeldItems?>,
    ) {
        binding.pokeItemsHolder.removeAllViews()
        lifecycleScope.launch {
            delay(500)
            binding.hasNoHeldItems.isViewVisible = response.data!!.isEmpty()
            data.forEach { heldItem ->
                val currentHeldItem = ImageView(requireContext()).apply {
                    this.layoutParams = LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.image_width),
                        resources.getDimensionPixelSize(R.dimen.image_height)
                    )
                    load(
                        heldItem?.sprites?.default
                    ) {
                        allowHardware(false)
                        crossfade(500)
                    }
                    setOnClickListener {
                        showBalloonDialog(heldItem, requireContext(), this)
                    }
                }
                binding.pokeItemsHolder.addView(currentHeldItem)
            }
        }
    }

    private fun addPokemonAbilities(
        pokeAbilityResponse: Resource<List<PokeAbilities?>>,
        data: List<PokeAbilities?>,
    ) {
        binding.pokeDetailsHolder.removeAllViews()
        lifecycleScope.launch {
            delay(500)
            hideProgressBar()
            if (pokeAbilityResponse.data?.isNotEmpty() == true) data.forEach {
                val pokeAbilityTitle = it?.name?.capitalize() ?: "Missing Data!"
                val pokeAbilityDescription =
                    if (it?.effectEntries?.getOrNull(1) != null) {
                        it.effectEntries[1]?.effect
                    } else if (it?.effectEntries?.getOrNull(0) != null) {
                        it.effectEntries[0]?.effect
                    } else "Missing Data!"

                val pokemonAbility = PokeAbilitiesLayout(
                    requireContext(),
                    pokeAbilityTitle,
                    pokeAbilityDescription
                )
                binding.pokeDetailsHolder.addView(pokemonAbility)
            }
        }
    }

    private fun showBalloonDialog(heldItem: PokeHeldItems?, context: Context, view: View) {
        val heldItemsDialog =
            DialogHeldItemBinding.inflate(LayoutInflater.from(requireContext()))
        val balloon = Balloon.Builder(context)
            .addGenericAttributes(heldItemsDialog)
            .build()
        balloon.getContentView().apply {
            findViewById<MaterialTextView>(R.id.itemTitle).text =
                heldItem?.name?.replace("-", " ")?.capitalize()
            findViewById<MaterialTextView>(R.id.itemEffect).text = "\n ${
                heldItem?.effectEntries?.first()?.effect?.replace("\n", "")
                    ?.replace(".", ".\n\n")
            }"

        }
        balloon.showAlignTop(view)
    }

    private fun hideProgressBar() {
        binding.loadingAnimation.visibility = View.INVISIBLE
        binding.pokeItemsHolder.visibility = View.VISIBLE
        binding.pokeDetailsHolder.visibility = View.VISIBLE
        binding.loadingAnimation.cancelAnimation()
    }

    private fun showProgressBar() {
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.pokeItemsHolder.visibility = View.INVISIBLE
        binding.pokeDetailsHolder.visibility = View.INVISIBLE
        binding.hasNoHeldItems.visibility = View.INVISIBLE
        binding.loadingAnimation.playAnimation()
    }

    private fun <T : ViewBinding> Balloon.Builder.addGenericAttributes(viewBinding: T): Balloon.Builder =
        this.setWidthRatio(1f)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setTextSize(15f)
            .setLayout(viewBinding)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(10)
            .setArrowPosition(0.5f)
            .setPadding(24)
            .setCornerRadius(8f)
            .setBackgroundColorResource(R.color.white)
            .setBalloonAnimation(BalloonAnimation.FADE)
            .setLifecycleOwner(viewLifecycleOwner).also {
                return this
            }

}