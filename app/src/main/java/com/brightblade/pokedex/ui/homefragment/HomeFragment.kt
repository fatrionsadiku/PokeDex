package com.brightblade.pokedex.ui.homefragment

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.data.persistent.PokemonPhotoTypes
import com.brightblade.pokedex.databinding.FragmentHomeBinding
import com.brightblade.pokedex.ui.PokeSplashScreen
import com.brightblade.pokedex.ui.adapters.CheckedItemState
import com.brightblade.pokedex.ui.adapters.PokeAdapter
import com.brightblade.utils.Resource
import com.brightblade.utils.capitalize
import com.brightblade.utils.isNumeric
import com.brightblade.utils.requireMainActivity
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.yagmurerdogan.toasticlib.Toastic
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), CheckedItemState {
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel: HomeViewModel by activityViewModels()
    private val pokeAdapter: PokeAdapter =
        PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
    private var favoriteStatusToast: Toast? = null
    private lateinit var powerMenu: PowerMenu
    private var doubleBackToExitOnce = false
    private var pokemonPhotoType: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observePokemonPhotoType()
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

    override fun onDestroyView() {
        super.onDestroyView()
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
                true  -> {
                    viewModel.unFavoritePokemon(
                        FavoritePokemon(
                            pokeName = currentPokemon.name,
                            url = currentPokemon.url
                        )
                    )
                    if (favoriteStatusToast != null) {
                        favoriteStatusToast!!.cancel()
                    }
                    favoriteStatusToast = Toastic.toastic(
                        context = requireContext(),
                        message = "${currentPokemon.name.capitalize()} removed from favorites",
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

                false -> {
                    viewModel.favoritePokemon(
                        FavoritePokemon(
                            pokeName = currentPokemon.name,
                            url = currentPokemon.url
                        )
                    )
                    if (favoriteStatusToast != null) {
                        favoriteStatusToast!!.cancel()
                    }
                    favoriteStatusToast = Toastic.toastic(
                        context = requireContext(),
                        message = "${currentPokemon.name.capitalize()} saved to favorites",
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
    }

    private fun observePokemonPhotoType() {
        lifecycleScope.launch {
            viewModel.pokemonPhotoTypeFlow.first { photoType ->
                when (photoType.photoType) {
                    PokemonPhotoTypes.HOME       -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.HOME)
                        pokemonPhotoType = "home"
                        true
                    }

                    PokemonPhotoTypes.OFFICIAL   -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.OFFICIAL)
                        pokemonPhotoType = "official"
                        true
                    }

                    PokemonPhotoTypes.DREAMWORLD -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.DREAMWORLD)
                        pokemonPhotoType = "dreamworld"
                        true
                    }

                    PokemonPhotoTypes.XYANI      -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.XYANI)
                        pokemonPhotoType = "xyani"
                        true
                    }

                }
            }
        }
    }

    private fun setUpPowerMenu() {
        powerMenu = PowerMenu.Builder(requireContext())
            .addItem(PowerMenuItem("Official", pokemonPhotoType == "official"))
            .addItem(PowerMenuItem("Dreamworld", pokemonPhotoType == "dreamworld"))
            .addItem(PowerMenuItem("Xyani", pokemonPhotoType == "xyani"))
            .addItem(PowerMenuItem("Home", pokemonPhotoType == "home"))
            .setAnimation(MenuAnimation.FADE)
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
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.OFFICIAL)
                        val myAdapter = binding.recyclerView.adapter
                        binding.recyclerView.adapter = myAdapter
                    }

                    "Dreamworld" -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.DREAMWORLD)
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.DREAMWORLD)
                        val myAdapter = binding.recyclerView.adapter
                        binding.recyclerView.adapter = myAdapter
                    }

                    "Xyani"      -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.XYANI)
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.XYANI)
                        val myAdapter = binding.recyclerView.adapter
                        binding.recyclerView.adapter = myAdapter
                    }

                    "Home"       -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.HOME)
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.HOME)
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
                    val firstVisibleItem =
                        (binding.recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (firstVisibleItem == 0) {
                        navigateBackToSelectionScreen()
                    }
                    if (pokeAdapter.pokemons.size > 100) binding.recyclerView.scrollToPosition(0)
                    else binding.recyclerView.smoothScrollToPosition(0)
                    doubleBackToExitOnce = true
                }

                true  -> navigateBackToSelectionScreen()
            }
        }
    }

    private fun navigateBackToSelectionScreen() =
        Intent(requireMainActivity(), PokeSplashScreen::class.java).also {
            val animations = ActivityOptions.makeCustomAnimation(
                requireContext(),
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it, animations.toBundle())
            requireActivity().finish()
        }

}

