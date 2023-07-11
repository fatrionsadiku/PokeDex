package com.example.pokedex.ui.homefragment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokedex.R
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.databinding.FragmentHomeBinding
import com.example.pokedex.ui.adapters.CheckedItemState
import com.example.pokedex.ui.adapters.PokeAdapter
import com.example.pokedex.ui.adapters.PokemonPhotoTypes
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.isNumeric
import com.example.pokedex.utils.requireMainActivity
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), CheckedItemState {
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel: HomeViewModel by activityViewModels()
    private val pokeAdapter: PokeAdapter =
        PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
    private lateinit var powerMenu: PowerMenu
    private var doubleBackToExitOnce = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpPowerMenu()
        setUpPokeRecyclerView()
        setUpPokeFiltering()
        fetchApiData()
        onBackPressed()
        setUpPokeSortOrder()
        viewModel.totalNumberOfFavs.observe(viewLifecycleOwner) {
            requireMainActivity().binding.bottomNavView.showBadge(1, "$it")
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("RecyclerView Activity", "onPause: ")
        val state = binding.recyclerView.layoutManager?.onSaveInstanceState()
        viewModel.recyclerViewState = state
    }

    override fun onResume() {
        super.onResume()
        Log.d("RecyclerView Activity", "onResume: ")
        val currentSavedState = viewModel.recyclerViewState
        if (currentSavedState != null) {
            binding.recyclerView.layoutManager?.onRestoreInstanceState(currentSavedState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("RecyclerView Activity", "onDestroy: ")
        val state = binding.recyclerView.layoutManager?.onSaveInstanceState()
        viewModel.recyclerViewState = state
    }

    override fun doesSelectedItemExist(itemName: String, doesItemExist: (result: Boolean) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            doesItemExist(viewModel.doesPokemonExist(itemName))
        }
    }

    private fun setUpPokeSortOrder() {
        binding.filterPokemons.setOnClickListener {
            powerMenu.showAsDropDown(it)
        }
    }

    private fun setUpPokeRecyclerView() =
        try {
            binding.recyclerView.apply {
                adapter = this@HomeFragment.pokeAdapter
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                addOnScrollListener(this@HomeFragment.scrollListener)
            }

        } catch (e: Exception) {
            Log.e("Error fetching poke", "setUpPokeRecyclerView: ${e.toString()}")
        }


    private fun fetchApiData() {
        viewModel.pokemonResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error   -> Log.e("HomeFragment", "Error fetching paginated pokemons")
                is Resource.Loading -> {
                    showProgressBar()
                }

                is Resource.Success -> {
                    hideProgressBar()
                    viewModel.doesAdapterHaveItems.value = true
                }
            }
            pokeAdapter.pokemons = response.data!!
        }
    }

    private fun setUpPokeFiltering() {
        binding.searchEditText.apply {
            this.addTextChangedListener { query ->
                viewModel.currentPokemoneQuery.value = query.toString()
                viewModel.filterPokemonByName(pokeAdapter)
                Log.d("IsNumeric", "${this.isNumeric()}")
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    this.clearFocus()
                }
                false
            }
        }
    }

    private fun adapterOnItemClickedListener(pokeName: String, pokeId: Int) {
        val action =
            HomeFragmentDirections.actionHomeFragmentToPokeDetailsFragment2(
                pokeName,
                pokeId
            )
        findNavController().navigate(action)
    }

    private fun favoritePokemon(position: Int) =
        viewLifecycleOwner.lifecycleScope.launch {
            val currentPokemon = pokeAdapter.pokemons[position]
            when (viewModel.doesPokemonExist(currentPokemon.name)) {
                true  -> viewModel.unFavoritePokemon(
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
            binding.paginationProgressBar.visibility = View.INVISIBLE
            binding.paginationProgressBar.cancelAnimation()
            binding.recyclerView.setPadding(0, 0, 0, 0)
        }
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        binding.paginationProgressBar.playAnimation()
        binding.recyclerView.setPadding(0, 0, 0, 130)
        isLoading = true
    }

    var isLoading = false
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

                RecyclerView.SCROLL_STATE_IDLE     -> {
                    isScrolling = false
                }
            }
        }

//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            super.onScrolled(recyclerView, dx, dy)
//
//            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//            val totalItemCount = layoutManager.itemCount
//
//            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
//            val isAtLastItem = lastVisibleItemPosition == totalItemCount - 1
//            val isNotAtBeginning = lastVisibleItemPosition >= 0
//            val isTotalMoreThanVisible = totalItemCount >= PAGE_SIZE
//            val shouldPaginate = isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
//                    isTotalMoreThanVisible && isScrolling
//            if (shouldPaginate) {
//                viewModel.getPaginatedPokemon()
//                isScrolling = false
//            }
//        }
    }

    private fun setUpPowerMenu() {
        powerMenu = PowerMenu.Builder(requireContext())
            .addItem(PowerMenuItem("Official", true)) // aad an item list.
            .addItem(PowerMenuItem("Dreamworld", false)) // aad an item list.
            .addItem(PowerMenuItem("Xyani", false)) // aad an item list.
            .addItem(PowerMenuItem("Home", false)) // aad an item list.
            .setAnimation(MenuAnimation.ELASTIC_TOP_RIGHT) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextGravity(Gravity.CENTER)
            .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
            .setOnMenuItemClickListener { position, item ->
                powerMenu.selectedPosition = position
                when (item.title) {
                    "Official"   -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.OFFICIAL)
                        val myAdapter = binding.recyclerView.adapter
                        binding.recyclerView.adapter = myAdapter
                    }
                    "Dreamworld" -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.DREAMWORLD)
                        val myAdapter = binding.recyclerView.adapter
                        binding.recyclerView.adapter = myAdapter
                    }
                    "Xyani"      -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.XYANI)
                        val myAdapter = binding.recyclerView.adapter
                        binding.recyclerView.adapter = myAdapter
                    }
                    "Home"     -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.HOME)
                        val myAdapter = binding.recyclerView.adapter
                        binding.recyclerView.adapter = myAdapter
                    }
                }
                powerMenu.dismiss()
            }
            .build()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when (doubleBackToExitOnce) {
                false -> {
                    if (pokeAdapter.pokemons.size > 100) binding.recyclerView.scrollToPosition(0)
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

