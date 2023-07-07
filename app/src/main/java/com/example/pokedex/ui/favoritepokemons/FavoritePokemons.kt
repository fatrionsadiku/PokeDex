package com.example.pokedex.ui.favoritepokemons

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.decode.GifDecoder
import coil.load
import com.example.pokedex.R
import com.example.pokedex.adapters.CheckedItemState
import com.example.pokedex.adapters.FavoritePokemonsAdapter
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.databinding.FragmentFavoritePokemonsBinding
import com.example.pokedex.ui.homefragment.HomeViewModel
import com.example.pokedex.utils.requireMainActivity
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritePokemons : Fragment(R.layout.fragment_favorite_pokemons), CheckedItemState {
    private val binding by viewBinding(FragmentFavoritePokemonsBinding::bind)
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: FavoritePokemonsAdapter
    private lateinit var recyclerView: RecyclerView
    private var doubleBackToExitOnce = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPokeRecyclerView()
        onBackPressed()
        doFavoritePokemonsExist()
        viewModel.totalNumberOfFavs.observe(viewLifecycleOwner) {
            requireMainActivity().binding.bottomNavView.showBadge(1, "$it")
        }
    }

    override fun doesSelectedItemExist(itemName: String, doesItemExist: (result: Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            doesItemExist(viewModel.doesPokemonExist(itemName))
        }
    }

    private fun setUpPokeRecyclerView() =
        try {
            recyclerView = binding.recyclerView
            adapter =
                FavoritePokemonsAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
            viewModel.favoritePokemons.observe(viewLifecycleOwner) {
                adapter.pokemons = it
            }
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        } catch (e: Exception) {
            Log.e("Error fetching poke", "setUpPokeRecyclerView: ${e.toString()}")
        }

    private fun favoritePokemon(position: Int) =
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val currentPokemon = adapter.pokemons[position]
            when (viewModel.doesPokemonExist(currentPokemon.pokeName)) {
                true  -> viewModel.unFavoritePokemon(
                    FavoritePokemon(
                        pokeName = currentPokemon.pokeName,
                        url = currentPokemon.url
                    )
                )

                false -> viewModel.favoritePokemon(
                    FavoritePokemon(
                        pokeName = currentPokemon.pokeName,
                        url = currentPokemon.url
                    )
                )
            }
        }

    private fun adapterOnItemClickedListener(pokeName: String, pokeId: Int?) {

        val action =
            FavoritePokemonsDirections.actionFavoritePokemonsToPokeDetailsFragment2(
                pokeName,
                pokeId ?: 0
            )
        findNavController().navigate(action)
    }

    private fun doFavoritePokemonsExist(){
        viewModel.doesDatabaseHaveITems.observe(viewLifecycleOwner) { doesDbHaveItems ->
            when(doesDbHaveItems){
                true  -> {
                    binding.noFavPokemons.apply {
                        visibility = View.VISIBLE
                        load(R.drawable.no_fav_pokemons) {
                            decoderFactory { result, options, _ ->
                                GifDecoder(result.source, options)
                            }
                        }
                        alpha = 0f
                    }.animate().setDuration(1000).alpha(1f)
                }
                false -> {
                    binding.noFavPokemons.apply {
                        visibility = View.GONE
                        alpha = 1f
                    }.animate().setDuration(500).alpha(0f)
                }
            }
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when (doubleBackToExitOnce) {
                false -> {
                    if (adapter.pokemons.size > 100) binding.recyclerView.scrollToPosition(0)
                    else binding.recyclerView.smoothScrollToPosition(0)
                    doubleBackToExitOnce = true
                }

                true  -> {
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("Do you want to exit out of the app?")
                            .setNegativeButton(
                                "Yes"
                            ) { _, _ ->
                                requireActivity().finish()
                            }
                            .setPositiveButton(
                                "No"
                            ) { dialogInterface, _ ->
                                dialogInterface.dismiss()
                                doubleBackToExitOnce = false
                            }.setOnCancelListener {
                                doubleBackToExitOnce = false
                            }
                            .create().show()
                    }
                }
            }
        }
    }

}



