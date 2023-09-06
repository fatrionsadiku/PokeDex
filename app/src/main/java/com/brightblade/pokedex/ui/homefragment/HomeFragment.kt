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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.data.models.PokemonResult
import com.brightblade.pokedex.data.persistent.PokemonPhotoTypes
import com.brightblade.pokedex.data.persistent.SortOrder
import com.brightblade.pokedex.databinding.FragmentHomeBinding
import com.brightblade.pokedex.ui.MainActivity
import com.brightblade.pokedex.ui.PokeSplashScreen
import com.brightblade.pokedex.ui.adapters.CheckedItemState
import com.brightblade.pokedex.ui.adapters.PokeAdapter
import com.brightblade.pokedex.ui.pokemondetails.PokemonDatabaseViewModel
import com.brightblade.utils.Resource
import com.brightblade.utils.capitalize
import com.brightblade.utils.requireMainActivity
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.yagmurerdogan.toasticlib.Toastic
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nl.joery.animatedbottombar.AnimatedBottomBar

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home), CheckedItemState,
    MainActivity.OnHomeButtonReselected {
    private val binding by viewBinding(FragmentHomeBinding::bind)
    private val viewModel: HomeViewModel by activityViewModels()
    private val pokeDbViewModel: PokemonDatabaseViewModel by viewModels()
    private lateinit var pokeAdapter: PokeAdapter
    private var favoriteStatusToast: Toast? = null
    private var listOfPokemons: List<PokemonResult>? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var pokePhotoPowerMenu: PowerMenu
    private lateinit var pokeSortyByNamePowerMenu: PowerMenu
    private var doubleBackToExitOnce = false
    private var pokemonPhotoType: String = ""
    private var sortOrderType: String = ""
    private var fragmentResultState = Pair<Boolean, Int?>(false, null)

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

    override fun onDestroyView() {
        super.onDestroyView()
        val state = recyclerView.layoutManager?.onSaveInstanceState()
        viewModel.recyclerViewState = state
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFragmentResultListener()
        recyclerView = binding.recyclerView
        observePokemonSortOrder()
        observePokemonPhotoType()
        setUpPokemonPhotoPowerMenu()
        setUpPokemonNameOrderPowerMenu()
        setUpPokeFiltering()
        onBackPressed()
        setUpPokeSortOrder()
        setUpPokePhotoType()
        observeBottomNaw()
        fetchApiData()
    }

    private fun setFragmentResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "pokemon_id",
            viewLifecycleOwner
        ) { requestKey, bundle ->
            val currentPokemonId = bundle.getInt("pokemon_id")
            val hasNavigatedState = bundle.getBoolean("navigation_state")
            when (hasNavigatedState) {
                true  -> lifecycleScope.launch {
                    fragmentResultState = Pair(true, currentPokemonId)
                }

                false -> {
                    fragmentResultState = Pair(false, null)
                }
            }
        }
    }

    private fun setUpPokeRecyclerView(pokeAdapter: PokeAdapter) {
        recyclerView.apply {
            adapter = pokeAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(this@HomeFragment.scrollListener)
        }.also {
            when (fragmentResultState.first) {
                true  -> it.scrollToPosition(fragmentResultState.second!!)
                false -> Unit
            }
        }
    }

    private fun fetchApiData() {
        viewModel.pokemonResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error   -> Log.e("HomeFragment", "Error fetching paginated pokemons")
                is Resource.Loading -> {
                    showProgressBar()
                }

                is Resource.Success -> {
                    viewModel.currentSelectedPokemonPhoto.observe(viewLifecycleOwner) { pokemonPhotoType ->
                        pokeAdapter = PokeAdapter(
                            ::adapterOnItemClickedListener,
                            ::favoritePokemon,
                            this,
                            pokemonPhotoType
                        )
                        pokeAdapter.pokemons = response.data!!
                        setUpPokeRecyclerView(pokeAdapter)
                        hideProgressBar()
                    }
                }
            }
        }
    }

    private fun setUpPokeFiltering() {
        binding.searchEditText.apply {
            this.addTextChangedListener { query ->
                viewModel.currentPokemonQuery.value = query.toString()
                viewModel.filterPokemonByName(pokeAdapter)
            }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    this.clearFocus()
                }
                false
            }
        }
    }

    private fun adapterOnItemClickedListener(
        pokeName: String,
        pokeId: Int,
        formattedId: String,
        dominantColor: Int,
    ) {
        val action =
            HomeFragmentDirections.actionHomeFragmentToPokeDetailsFragment2(
                pokeName,
                pokeId,
                formattedId,
                dominantColor
            )
        findNavController().navigate(action)
    }

    private fun favoritePokemon(position: Int) =
        viewLifecycleOwner.lifecycleScope.launch {
            val currentPokemon = pokeAdapter.pokemons[position]
            when (pokeDbViewModel.doesPokemonExist(currentPokemon.name)) {
                true  -> {
                    pokeDbViewModel.unFavoritePokemon(
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
                    pokeDbViewModel.favoritePokemon(
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
        binding.paginationProgressBar.visibility = View.INVISIBLE
        binding.paginationProgressBar.cancelAnimation()
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        binding.paginationProgressBar.playAnimation()
        isLoading = true
    }


    private fun observePokemonPhotoType() {
        lifecycleScope.launch {
            viewModel.pokemonPhotoTypeFlow.first { photoType ->
                when (photoType.photoType) {
                    PokemonPhotoTypes.HOME       -> {
                        pokemonPhotoType = PokemonPhotoTypes.HOME.name.lowercase()
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.HOME.name.lowercase()
                        true
                    }

                    PokemonPhotoTypes.OFFICIAL   -> {
                        pokemonPhotoType = PokemonPhotoTypes.OFFICIAL.name.lowercase()
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.OFFICIAL.name.lowercase()
                        true
                    }

                    PokemonPhotoTypes.DREAMWORLD -> {
                        pokemonPhotoType = PokemonPhotoTypes.DREAMWORLD.name.lowercase()
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.DREAMWORLD.name.lowercase()
                        true
                    }

                    PokemonPhotoTypes.XYANI      -> {
                        pokemonPhotoType = PokemonPhotoTypes.XYANI.name.lowercase()
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.XYANI.name.lowercase()
                        true
                    }

                }
            }
        }
    }

    private fun observePokemonSortOrder() {
        lifecycleScope.launch {
            viewModel.pokemonSortOrderFlow.first { sortOrder ->
                when (sortOrder.sortOrder) {
                    SortOrder.BY_ID_ASCENDING    -> {
                        sortOrderType = SortOrder.BY_ID_ASCENDING.name.lowercase()
                        true
                    }

                    SortOrder.BY_ID_DESCENDING   -> {
                        sortOrderType = SortOrder.BY_ID_DESCENDING.name.lowercase()
                        true
                    }

                    SortOrder.BY_NAME_ASCENDING  -> {
                        sortOrderType = SortOrder.BY_NAME_ASCENDING.name.lowercase()
                        true
                    }

                    SortOrder.BY_NAME_DESCENDING -> {
                        sortOrderType = SortOrder.BY_NAME_DESCENDING.name.lowercase()
                        true
                    }
                }
            }
        }
    }

    private fun setUpPokemonPhotoPowerMenu() {
        pokePhotoPowerMenu = PowerMenu.Builder(requireContext())
            .addPowerMenuItems(
                PowerMenuItem("Official", pokemonPhotoType == "official"),
                PowerMenuItem("Dreamworld", pokemonPhotoType == "dreamworld"),
                PowerMenuItem("Xyani", pokemonPhotoType == "xyani"),
                PowerMenuItem("Home", pokemonPhotoType == "home")
            )
            .addGenericItems()
            .setOnMenuItemClickListener { position, item ->
                pokePhotoPowerMenu.selectedPosition = position
                when (item.title) {
                    "Official"   -> {
                        pokemonPhotoType = "official"
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.OFFICIAL)
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.OFFICIAL.name.lowercase()
                        recyclerView.swapAdapter(pokeAdapter, true)
                    }

                    "Dreamworld" -> {
                        pokemonPhotoType = "dreamworld"
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.DREAMWORLD)
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.DREAMWORLD.name.lowercase()
                        recyclerView.swapAdapter(pokeAdapter, true)
                    }

                    "Xyani"      -> {
                        pokemonPhotoType = "xyani"
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.XYANI)
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.XYANI.name.lowercase()
                        recyclerView.swapAdapter(pokeAdapter, true)
                    }

                    "Home"       -> {
                        pokemonPhotoType = "home"
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.HOME)
                        viewModel.currentSelectedPokemonPhoto.value =
                            PokemonPhotoTypes.HOME.name.lowercase()
                        recyclerView.swapAdapter(pokeAdapter, true)
                    }
                }
                pokePhotoPowerMenu.dismiss()
            }
            .build()
    }

    private fun setUpPokemonNameOrderPowerMenu() {
        pokeSortyByNamePowerMenu = PowerMenu.Builder(requireContext())
            .addGenericItems()
            .addPowerMenuItems(
                PowerMenuItem(
                    "Name A-Z",
                    sortOrderType == SortOrder.BY_NAME_ASCENDING.name.lowercase()
                ),
                PowerMenuItem(
                    "Name Z-A",
                    sortOrderType == SortOrder.BY_NAME_DESCENDING.name.lowercase()
                ),
                PowerMenuItem(
                    "ID Asc",
                    sortOrderType == SortOrder.BY_ID_ASCENDING.name.lowercase()
                ),
                PowerMenuItem(
                    "ID Desc",
                    sortOrderType == SortOrder.BY_ID_DESCENDING.name.lowercase()
                )
            )
            .setOnMenuItemClickListener { position, item ->
                pokeSortyByNamePowerMenu.selectedPosition = position
                when (item.title) {
                    "Name A-Z" -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_NAME_ASCENDING)
                    }

                    "Name Z-A" -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_NAME_DESCENDING)
                    }

                    "ID Asc"   -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_ID_ASCENDING)
                    }

                    "ID Desc"  -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_ID_DESCENDING)
                    }
                }
                pokeSortyByNamePowerMenu.dismiss()
            }
            .build()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            when (doubleBackToExitOnce) {
                false -> {
                    val firstVisibleItem =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                    if (firstVisibleItem == 0) {
                        navigateBackToSelectionScreen()
                    }
                    if (pokeAdapter.pokemons.size > 100) recyclerView.scrollToPosition(0)
                    else recyclerView.smoothScrollToPosition(0)
                    doubleBackToExitOnce = true
                }

                true  -> navigateBackToSelectionScreen()
            }
        }
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

    private fun observeBottomNaw() {
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

    private fun setUpPokePhotoType() {
        binding.filterPokemonPhotos.setOnClickListener {
            pokePhotoPowerMenu.showAsDropDown(it)
        }
    }

    private fun setUpPokeSortOrder() {
        binding.filterPokemonByName.setOnClickListener {
            pokeSortyByNamePowerMenu.showAsDropDown(it)
        }
    }

    private fun PowerMenu.Builder.addGenericItems(): PowerMenu.Builder {
        this.setAnimation(MenuAnimation.FADE)
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextGravity(Gravity.CENTER)
            .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(Color.WHITE)
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(requireContext(), R.color.purple_500))
        return this
    }

    private fun PowerMenu.Builder.addPowerMenuItems(vararg items: PowerMenuItem): PowerMenu.Builder {
        items.forEach {
            this.addItem(
                it
            )
        }
        return this
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

    override fun onHomeButtonReSelected() {
        binding.recyclerView.smoothScrollToPosition(0)
    }
}

