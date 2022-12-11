package com.example.pokedex.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.databinding.PokeLayoutBinding

class PokeAdapter(val context : Context) : RecyclerView.Adapter<PokeAdapter.ViewHolder>() {
    var pokemons: MutableList<Pokemon> = mutableListOf()

    class ViewHolder(val binding: PokeLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PokeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemons[position]
        with(holder) {
            binding.pokeName.text = pokemon.name
            Glide.with(context).load(pokemon.sprites.front_default).into(binding.pokemonPlaceHolder)
        }
    }

    override fun getItemCount(): Int = pokemons.size
}