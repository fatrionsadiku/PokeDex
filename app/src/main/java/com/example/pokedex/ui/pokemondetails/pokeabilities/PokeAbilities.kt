package com.example.pokedex.ui.pokemondetails.pokeabilities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.pokedex.R
import com.example.pokedex.data.models.PokeHeldItems
import com.example.pokedex.databinding.HeldItemDialogBinding
import com.example.pokedex.databinding.PokeAbilitiesBinding
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.capitalize
import com.example.pokedex.utils.customviews.PokeAbilitiesLayout
import com.example.pokedex.utils.second
import com.example.pokedex.viewmodels.PokeDetailsSharedViewModel
import com.google.android.material.textview.MaterialTextView
import com.skydoves.balloon.ArrowPositionRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.BalloonSizeSpec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokeAbilities : Fragment() {
    lateinit var binding: PokeAbilitiesBinding
    private lateinit var viewModel: PokeDetailsSharedViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = PokeAbilitiesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[PokeDetailsSharedViewModel::class.java]
        return binding.root
    }

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
                when(response){
                    is Resource.Error -> Toast.makeText(requireContext(),"Something happened idk", Toast.LENGTH_LONG).show()
                    is Resource.Loading -> showProgressBar()
                    is Resource.Success -> {
                        binding.hasNoHeldItems.isVisible = response.data!!.isEmpty()
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
                                    showBalloonDialog(heldItem,requireContext(),this)
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
                heldItem?.effectEntries?.first()?.effect
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