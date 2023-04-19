package com.example.pokedex.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.databinding.FragmentHomeBinding
import com.example.pokedex.utils.Resource
import com.example.pokedex.utils.Utility
import com.example.pokedex.viewmodels.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter : PokeAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getPaginatedPokemons(Utility.PAGE_SIZE)
        setUpPokeRecyclerView()
        setUpPokeFiltering()
    }

    private fun setUpPokeRecyclerView() = viewLifecycleOwner.lifecycleScope.launch {
        try {
            val recyclerView = binding.recyclerView
            adapter = PokeAdapter(::adapterOnItemClickedListener)
            fetchApiData()
            recyclerView.adapter = adapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        } catch (e: Exception) {
            Log.e("Error fetching poke", "setUpPokeRecyclerView: ${e.toString()}")
        }
    }

    private fun fetchApiData(){
        viewModel.pokemonResponse.observe(viewLifecycleOwner) { response ->
            when(response){
                is Resource.Error -> Log.e("HomeFragment", "Error fetching paginated pokemons")
                is Resource.Loading -> {
                    binding.apply {
                        binding.progressBar.isVisible = true
                        binding.recyclerView.isVisible = false
                    }
                }
                is Resource.Success -> {
                    adapter.pokemons = response.data!!
                    binding.progressBar.isVisible = false
                    binding.recyclerView.isVisible = true
                }
            }
        }
    }

    private fun setUpPokeFiltering() {
        binding.searchEditText.apply {
            addTextChangedListener { query ->
                viewModel.filterPokemonsByName(query,adapter)
            }
            setOnEditorActionListener { _, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    this.clearFocus()
                }
                false
            }
        }
    }
    private fun adapterOnItemClickedListener(pokeName: String){
        val action = HomeFragmentDirections.actionHomeFragmentToPokeDetailsFragment2(
            pokeName
        )
        findNavController().navigate(action)
    }
}