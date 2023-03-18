package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonApiResult
import com.example.pokedex.databinding.PokeLayoutBinding

class PokeAdapter(val itemClicker : (pokemonName : String) -> Unit) : RecyclerView.Adapter<PokeAdapter.ViewHolder>() {
    var pokemons : List<Pokemon?>
    get() = differ.currentList
    set(value) {
        differ.submitList(value)
    }
    private val differ = AsyncListDiffer(this, diffCallback)



    inner class ViewHolder(val binding: PokeLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION){
                        val pokeName = pokemons[currentPosition]?.name
                        itemClicker.invoke(pokeName!!)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PokeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemons[position]
        with(holder) {
            binding.pokeName.text = pokemon?.name
            Glide.with(binding.root.context).load(pokemon?.sprites?.pokeImageUrl).into(binding.pokemonPlaceHolder)
        }
    }

    override fun getItemCount(): Int = pokemons.size
}

private val diffCallback = object : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem == newItem
    }
}