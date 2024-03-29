package com.brightblade.pokedex.ui.favoritepokemons

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.decode.GifDecoder
import coil.load
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.databinding.FragmentFavoritePokemonsBinding
import com.brightblade.pokedex.ui.adapters.CheckedItemState
import com.brightblade.pokedex.ui.adapters.FavoritePokemonsAdapter
import com.brightblade.pokedex.ui.homefragment.HomeViewModel
import com.brightblade.pokedex.ui.pokemondetails.PokemonDatabaseViewModel
import com.brightblade.utils.capitalize
import com.brightblade.utils.requireMainActivity
import com.yagmurerdogan.toasticlib.Toastic
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nl.joery.animatedbottombar.AnimatedBottomBar

@AndroidEntryPoint
class FavoritePokemons : Fragment(R.layout.fragment_favorite_pokemons), CheckedItemState {
    private var favoriteStatusToast: Toast? = null
    private val binding by viewBinding(FragmentFavoritePokemonsBinding::bind)
    private val viewModel: HomeViewModel by activityViewModels()
    private val pokeDbViewModel: PokemonDatabaseViewModel by activityViewModels()
    private lateinit var adapter: FavoritePokemonsAdapter
    private lateinit var recyclerView: RecyclerView
    private var doubleBackToExitOnce = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPokeRecyclerView()
        onBackPressed()
        doFavoritePokemonsExist()
        observeBottomNav()
    }

    private fun observeBottomNav() {
        viewModel.totalNumberOfFavs.observe(viewLifecycleOwner) {
            requireMainActivity().binding.bottomNavView.setBadgeAtTabId(
                R.id.favorites,
                AnimatedBottomBar.Badge(it.toString())
            )
        }
    }

    override fun doesSelectedItemExist(itemName: String, doesItemExist: (result: Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            doesItemExist(pokeDbViewModel.doesPokemonExist(itemName))
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
            Log.e("Error fetching poke", "setUpPokeRecyclerView: $e")
        }

    private fun favoritePokemon(position: Int) =
        viewLifecycleOwner.lifecycleScope.launch {
            val currentPokemon = adapter.pokemons[position]
            when (pokeDbViewModel.doesPokemonExist(currentPokemon.pokeName)) {
                true -> {
                    pokeDbViewModel.unFavoritePokemon(
                        FavoritePokemon(
                            pokeName = currentPokemon.pokeName,
                            url = currentPokemon.url
                        )
                    )
                    if (favoriteStatusToast != null) {
                        favoriteStatusToast!!.cancel()
                    }
                    favoriteStatusToast = Toastic.toastic(
                        context = requireContext(),
                        message = "${currentPokemon.pokeName.capitalize()} removed from favorites",
                        duration = Toastic.LENGTH_SHORT,
                        type = Toastic.DEFAULT,
                        isIconAnimated = true,
                        customIcon = R.drawable.pokeball,
                        font = R.font.ryogothic,
                        textColor = Color.BLACK,
                        customIconAnimation = R.anim.rotate_anim
                    )
                    favoriteStatusToast!!.show()
                }

                false -> pokeDbViewModel.favoritePokemon(
                    FavoritePokemon(
                        pokeName = currentPokemon.pokeName,
                        url = currentPokemon.url
                    )
                )
            }
        }

    private fun adapterOnItemClickedListener(pokeName: String, pokeId: Int?, dominantColor: Int) {

        val action =
            FavoritePokemonsDirections.actionFavoritePokemonsToPokeDetailsFragment2(
                pokeName,
                pokeId ?: 0,
                "a",
                dominantColor
            )
        findNavController().navigate(action)
    }

    private fun doFavoritePokemonsExist() {
        viewModel.doesDatabaseHaveItems.observe(viewLifecycleOwner) { doesDbHaveItems ->
            when (doesDbHaveItems) {
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
                    val firstVisibleItem =
                        (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (firstVisibleItem == 0) {
                        navigateBackToHomeScreen()
                    }
                    if (adapter.pokemons.size > 100) binding.recyclerView.scrollToPosition(0)
                    else binding.recyclerView.smoothScrollToPosition(0)
                    doubleBackToExitOnce = true
                }

                true  -> navigateBackToHomeScreen()
            }
        }
    }

    private fun navigateBackToHomeScreen() = findNavController().navigate(
        resId = R.id.homeFragment,
        args = null,
        navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_left)
            .setExitAnim(R.anim.slide_out_right).build()
    ).also {
        requireMainActivity().binding.bottomNavView.selectTabAt(0, true)
    }

}



