package com.example.pokedex.ui.pokemondetails.homefragment

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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.adapters.CheckedItemState
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.databinding.FragmentHomeBinding
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.SpacesItemDecoration
import com.example.pokedex.utils.Utility.PAGE_SIZE
import com.example.pokedex.utils.requireMainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(), CheckedItemState {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var adapter: PokeAdapter
    private lateinit var recyclerView: RecyclerView
    private var doubleBackToExitOnce = false
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPokeRecyclerView()
        setUpPokeFiltering()
        onBackPressed()
        viewModel.totalNumberOfFavs.observe(viewLifecycleOwner) {
            requireMainActivity().binding.bottomNavView.showBadge(1, "$it")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("RecyclerView Activity", "onPause: ")
        val state = recyclerView.layoutManager?.onSaveInstanceState()
        viewModel.recyclerViewState = state
    }

    override fun onResume() {
        super.onResume()
        Log.d("RecyclerView Activity", "onResume: ")
        val currentSavedState = viewModel.recyclerViewState
        if (currentSavedState != null) {
            recyclerView.layoutManager?.onRestoreInstanceState(currentSavedState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RecyclerView Activity", "onDestroy: ")
        val state = recyclerView.layoutManager?.onSaveInstanceState()
        viewModel.recyclerViewState = state
    }

    override fun doesSelectedItemExist(itemName: String, doesItemExist: (result: Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            doesItemExist(viewModel.doesPokemonExist(itemName))
        }
    }

    private fun setUpPokeRecyclerView() =
        try {
            recyclerView = binding.recyclerView
            adapter = PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
            fetchApiData()
            recyclerView.apply {
                adapter = this@HomeFragment.adapter
                layoutManager = GridLayoutManager(requireContext(), 2)
                addItemDecoration(SpacesItemDecoration())
                addOnScrollListener(this@HomeFragment.scrollListener)
            }

        } catch (e: Exception) {
            Log.e("Error fetching poke", "setUpPokeRecyclerView: ${e.toString()}")
        }


    private fun fetchApiData() {
        viewModel.pokemonResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error -> Log.e("HomeFragment", "Error fetching paginated pokemons")
                is Resource.Loading -> {
                    showProgressBar()
                }

                is Resource.Success -> {
                    hideProgressBar()
                    adapter.pokemons = response.data!!.toList()
                }
            }
        }
    }

    private fun setUpPokeFiltering() {
        binding.searchEditText.apply {
            addTextChangedListener { query ->
                viewModel.filterPokemonByName(query, adapter)
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    this.clearFocus()
                }
                false
            }
        }
    }

    private fun adapterOnItemClickedListener(pokeName: String, pokeId: Int?) {

        val action =
            HomeFragmentDirections.actionHomeFragmentToPokeDetailsFragment2(
                pokeName,
                pokeId ?: 0
            )
        findNavController().navigate(action)
    }

    private fun favoritePokemon(position: Int) =
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val currentPokemon = adapter.pokemons[position]
            when (viewModel.doesPokemonExist(currentPokemon.name)) {
                true -> viewModel.unFavoritePokemon(
                    FavoritePokemon(
                        pokeName = currentPokemon.name,
                        url = currentPokemon.url
                    )
                )

                false -> viewModel.favoritePokemon(
                    FavoritePokemon(
                        pokeName = currentPokemon.name,
                        url = currentPokemon.url
                    )
                )
            }
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

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when (doubleBackToExitOnce) {
                false -> {
                    if (adapter.pokemons.size > 100) binding.recyclerView.scrollToPosition(0)
                    else binding.recyclerView.smoothScrollToPosition(0)
                    doubleBackToExitOnce = true
                }

                true -> {
                    AlertDialog.Builder(requireContext()).apply {
                        setTitle("Do you want to exit out of the app?")
                            .setNegativeButton(
                                "Yes"
                            ) { _, _ ->
                                requireActivity().finish()
                            }
                            .setPositiveButton(
                                "No",
                                DialogInterface.OnClickListener { dialogInterface, i ->
                                    dialogInterface.dismiss()
                                    doubleBackToExitOnce = false
                                }).setOnCancelListener {
                                doubleBackToExitOnce = false
                            }
                            .create().show()
                    }
                }
            }
        }
    }

}

