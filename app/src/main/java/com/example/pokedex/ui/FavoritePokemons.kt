package com.example.pokedex.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.adapters.CheckedItemState
import com.example.pokedex.adapters.FavoritePokemonsAdapter
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.databinding.FavoritePokemonsBinding
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.SpacesItemDecoration
import com.example.pokedex.utils.Utility.PAGE_SIZE
import com.example.pokedex.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class FavoritePokemons : Fragment() {
    private lateinit var binding: FavoritePokemonsBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: FavoritePokemonsAdapter
    private lateinit var recyclerView: RecyclerView
    private var doubleBackToExitOnce = false
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
    }
    private fun setUpPokeRecyclerView() =
        try {
            recyclerView = binding.recyclerView
            adapter = FavoritePokemonsAdapter(::adapterOnItemClickedListener)
            viewModel.favoritePokemons.observe(viewLifecycleOwner){
                adapter.pokemons = it
            }
            recyclerView.adapter = adapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.addItemDecoration(SpacesItemDecoration())
            recyclerView.addOnScrollListener(this@FavoritePokemons.scrollListener)

        } catch (e: Exception) {
            Log.e("Error fetching poke", "setUpPokeRecyclerView: ${e.toString()}")
        }
    private fun adapterOnItemClickedListener(pokeName: String, pokeId: Int?) {

        val action = FavoritePokemonsDirections.actionFavoritePokemonsToPokeDetailsFragment2(
            pokeName,
            pokeId ?: 0
        )
        findNavController().navigate(action)
    }

    private fun hideProgressBar() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            binding.paginationProgressBar.visibility = View.INVISIBLE
            binding.paginationProgressBar.cancelAnimation()
            recyclerView.setPadding(0, 0, 0, 0)
        }
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        binding.paginationProgressBar.playAnimation()
        recyclerView.setPadding(0, 0, 0, 130)
        isLoading = true
    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    doubleBackToExitOnce = false
                    isScrolling = true
                }

                RecyclerView.SCROLL_STATE_SETTLING -> {
                    isScrolling = true
                }

                RecyclerView.SCROLL_STATE_IDLE -> {
                    isScrolling = false
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as GridLayoutManager
            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = lastVisibleItemPosition == totalItemCount - 1
            val isNotAtBeginning = lastVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                    isTotalMoreThanVisible && isScrolling
            if (shouldPaginate) {
                viewModel.getPaginatedPokemon()
                isScrolling = false
            }
        }
    }

}

