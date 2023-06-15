package com.example.pokedex.ui.pokemondetails.favoritepokemons

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.adapters.CheckedItemState
import com.example.pokedex.adapters.FavoritePokemonsAdapter
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.databinding.FavoritePokemonsBinding
import com.example.pokedex.ui.pokemondetails.homefragment.HomeViewModel
import com.example.pokedex.utils.SpacesItemDecoration
import com.example.pokedex.utils.requireMainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritePokemons : Fragment(), CheckedItemState {
    private lateinit var binding: FavoritePokemonsBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: FavoritePokemonsAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FavoritePokemonsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPokeRecyclerView()
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
            adapter = FavoritePokemonsAdapter(::adapterOnItemClickedListener,::favoritePokemon,this)
            viewModel.favoritePokemons.observe(viewLifecycleOwner) {
                adapter.pokemons = it
            }
            recyclerView.adapter = adapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.addItemDecoration(SpacesItemDecoration())

        } catch (e: Exception) {
            Log.e("Error fetching poke", "setUpPokeRecyclerView: ${e.toString()}")
        }

    private fun favoritePokemon(position: Int) =
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val currentPokemon = adapter.pokemons[position]
            when (viewModel.doesPokemonExist(currentPokemon.pokeName)) {
                true -> viewModel.unFavoritePokemon(
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
}



