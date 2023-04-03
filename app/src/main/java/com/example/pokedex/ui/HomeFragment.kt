package com.example.pokedex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel : HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getPaginatedPokemons(30)
        setUpPokeRecyclerView()
    }

    private fun setUpPokeRecyclerView(){
        val recyclerView = binding.recyclerView
        val adapter = PokeAdapter { pokeName, pokeURL ->
            val action = HomeFragmentDirections.actionHomeFragmentToPokeDetailsFragment2(pokeName,pokeURL)
            findNavController().navigate(action)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        viewModel.pokemons.observe(viewLifecycleOwner) {
            adapter.pokemons = it.toList()
        }
    }
}