package com.example.pokedex.ui.pokemondetails.pokeabilities

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.pokedex.R
import com.example.pokedex.data.models.PokeHeldItems
import com.example.pokedex.databinding.HeldItemDialogBinding
import com.example.pokedex.databinding.PokeAbilitiesBinding
import com.example.pokedex.ui.PokeDetailsSharedViewModel
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.capitalize
import com.example.pokedex.utils.customviews.PokeAbilitiesLayout
import com.example.pokedex.utils.isViewVisible
import com.google.android.material.textview.MaterialTextView
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokeAbilities : Fragment(R.layout.poke_abilities) {
    val binding by viewBinding(PokeAbilitiesBinding::bind)
    private val viewModel: PokeDetailsSharedViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            pokeDetailsHolder.removeAllViews()
            viewModel.abilitiesResponse.apply {
                observe(viewLifecycleOwner) { pokeAbility ->
                    pokeAbility?.forEach {
                        val pokeAbilityTitle = it?.name?.capitalize()
                        val pokeAbilityDescription = it?.effectEntries?.get(1)?.effect
                        val pokemonAbility = PokeAbilitiesLayout(
                            requireContext(),
                            pokeAbilityTitle,
                            pokeAbilityDescription
                        )
                        pokeDetailsHolder.addView(pokemonAbility)
                    }
                }
            }
            viewModel.pokemonHeldItems.observe(viewLifecycleOwner) { response ->
                when (response) {
                    is Resource.Error -> Log.e("PokeAbilities", "${response.message}", )
                    is Resource.Loading -> showProgressBar()
                    is Resource.Success -> {
                        binding.hasNoHeldItems.isViewVisible = response.data!!.isEmpty()
                        hideProgressBar()
                        response.data.forEach { heldItem ->
                            val currentHeldItem = ImageView(requireContext()).apply {
                                this.layoutParams = LayoutParams(
                                    resources.getDimensionPixelSize(R.dimen.image_width),
                                    resources.getDimensionPixelSize(R.dimen.image_height)
                                )
                                load(
                                    heldItem?.sprites?.default
                                ) {
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

            }
        }
    }

    private fun showBalloonDialog(heldItem: PokeHeldItems?, context: Context, view: View) {
        val heldItemsDialog =
            HeldItemDialogBinding.inflate(LayoutInflater.from(requireContext()))
        val balloon = Balloon.Builder(context)
            .setWidthRatio(1f)
            .setWidth(BalloonSizeSpec.WRAP)
            .setHeight(BalloonSizeSpec.WRAP)
            .setTextSize(15f)
            .setLayout(heldItemsDialog)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(10)
            .setArrowPosition(0.5f)
            .setPadding(24)
            .setCornerRadius(8f)
            .setBackgroundColorResource(R.color.white)
            .setBalloonAnimation(BalloonAnimation.FADE)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()
        balloon.getContentView().apply {
            findViewById<MaterialTextView>(R.id.itemTitle).text =
                heldItem?.name?.replace("-", " ")?.capitalize()
            findViewById<MaterialTextView>(R.id.itemEffect).text =
                "\n ${
                    heldItem?.effectEntries?.first()?.effect?.replace("\n", "")
                        ?.replace(".", ".\n\n")
                }"
        }
        balloon.showAlignTop(view)
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        binding.paginationProgressBar.playAnimation()
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.GONE
        binding.paginationProgressBar.cancelAnimation()
    }

}