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
import androidx.fragment.app.setFragmentResultListener
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
    private val pokeAdapter: PokeAdapter =
        PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
    private var favoriteStatusToast: Toast? = null
    private var listOfPokemons: List<PokemonResult>? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var pokePhotoPowerMenu: PowerMenu
    private lateinit var pokeSortyByNamePowerMenu: PowerMenu
    private var doubleBackToExitOnce = false
    private var pokemonPhotoType: String = ""
    private var sortOrderType: String = ""

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
        setUpPokeRecyclerView()
        fetchApiData()
        observePokemonSortOrder()
        observePokemonPhotoType()
        setUpPokemonPhotoPowerMenu()
        setUpPokemonNameOrderPowerMenu()
        setUpPokeFiltering()
        onBackPressed()
        setUpPokeSortOrder()
        setUpPokePhotoType()
        observeBottomNaw()
        setFragmentResultListener()
    }

    private fun setFragmentResultListener() {
        setFragmentResultListener("pokemon_id") { requestKey, bundle ->
            val currentPokemonId = bundle.getInt("pokemon_id")
            val hasNavigatedState = bundle.getBoolean("navigation_state")
            when (hasNavigatedState) {
                true  -> recyclerView.scrollToPosition(
                    if (currentPokemonId >= 10000) currentPokemonId / 10 else currentPokemonId
                )

                false -> Unit
            }
        }
    }

    private fun setUpPokeRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.apply {
            adapter = this@HomeFragment.pokeAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(this@HomeFragment.scrollListener)
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
                    hideProgressBar()
                    viewModel.doesAdapterHaveItems.value = true
                    pokeAdapter.pokemons = response.data!!
                    listOfPokemons = response.data
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
        recyclerView.setPadding(0, 0, 0, 0)
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        binding.paginationProgressBar.playAnimation()
        recyclerView.setPadding(0, 0, 0, 130)
        isLoading = true
    }


    private fun observePokemonPhotoType() {
        lifecycleScope.launch {
            viewModel.pokemonPhotoTypeFlow.first { photoType ->
                when (photoType.photoType) {
                    PokemonPhotoTypes.HOME       -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.HOME)
                        pokemonPhotoType = PokemonPhotoTypes.HOME.name.lowercase()
                        true
                    }

                    PokemonPhotoTypes.OFFICIAL   -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.OFFICIAL)
                        pokemonPhotoType = PokemonPhotoTypes.OFFICIAL.name.lowercase()
                        true
                    }

                    PokemonPhotoTypes.DREAMWORLD -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.DREAMWORLD)
                        pokemonPhotoType = PokemonPhotoTypes.DREAMWORLD.name.lowercase()
                        true
                    }

                    PokemonPhotoTypes.XYANI      -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.XYANI)
                        pokemonPhotoType = PokemonPhotoTypes.XYANI.name.lowercase()
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
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.OFFICIAL)
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.OFFICIAL)
                        val myAdapter = recyclerView.adapter
                        recyclerView.swapAdapter(myAdapter, true)
                    }

                    "Dreamworld" -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.DREAMWORLD)
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.DREAMWORLD)
                        val myAdapter = recyclerView.adapter
                        recyclerView.adapter = myAdapter
                    }

                    "Xyani"      -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.XYANI)
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.XYANI)
                        val myAdapter = recyclerView.adapter
                        recyclerView.swapAdapter(myAdapter, true)
                    }

                    "Home"       -> {
                        pokeAdapter.changePokemonPhoto(PokemonPhotoTypes.HOME)
                        viewModel.onPokemonPhotoTypeSelected(PokemonPhotoTypes.HOME)
                        val myAdapter = recyclerView.adapter
                        recyclerView.adapter = myAdapter
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
                    "nAsc",
                    sortOrderType == SortOrder.BY_NAME_ASCENDING.name.lowercase()
                ),
                PowerMenuItem(
                    "nDsc",
                    sortOrderType == SortOrder.BY_NAME_DESCENDING.name.lowercase()
                ),
                PowerMenuItem(
                    "iAsc",
                    sortOrderType == SortOrder.BY_ID_ASCENDING.name.lowercase()
                ),
                PowerMenuItem(
                    "iDsc",
                    sortOrderType == SortOrder.BY_ID_DESCENDING.name.lowercase()
                )
            )
            .setOnMenuItemClickListener { position, item ->
                pokeSortyByNamePowerMenu.selectedPosition = position
                when (item.title) {
                    "nAsc" -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_NAME_ASCENDING)
                        val myAdapter =
                            PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
                        myAdapter.pokemons =
                            if (listOfPokemons != null) listOfPokemons!! else emptyList()
                        recyclerView.swapAdapter(myAdapter, true)
                    }

                    "nDsc" -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_NAME_DESCENDING)
                        val myAdapter =
                            PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
                        myAdapter.pokemons =
                            if (listOfPokemons != null) listOfPokemons!! else emptyList()
                        recyclerView.adapter = myAdapter
                    }

                    "iAsc" -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_ID_ASCENDING)
                        val myAdapter =
                            PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
                        myAdapter.pokemons =
                            if (listOfPokemons != null) listOfPokemons!! else emptyList()
                        recyclerView.adapter = myAdapter
                    }

                    "iDsc" -> {
                        viewModel.onSortOrderChanged(SortOrder.BY_ID_DESCENDING)
                        val myAdapter =
                            PokeAdapter(::adapterOnItemClickedListener, ::favoritePokemon, this)
                        myAdapter.pokemons =
                            if (listOfPokemons != null) listOfPokemons!! else emptyList()
                        recyclerView.adapter = myAdapter
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

