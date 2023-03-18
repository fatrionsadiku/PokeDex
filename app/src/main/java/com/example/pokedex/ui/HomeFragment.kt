package com.example.pokedex.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pokedex.adapters.PokeAdapter
import com.example.pokedex.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    private val viewModel by lazy {
        ViewModelProvider(this)[HomeViewModel::class.java]
    }
    private val recyclerView by lazy {
        binding.recyclerView
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getPaginatedPokemons(30)
        setUpPokeRecyclerView()

    }

    private fun setUpPokeRecyclerView(){
        val adapter = PokeAdapter() {
            Toast.makeText(requireContext(), "The pokemon clicked's name is $it", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = adapter
        val padding = (resources.displayMetrics.density * 22).toInt()
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        viewModel.pokemon.observe(viewLifecycleOwner) {
            adapter.pokemons = it
        }
    }
}