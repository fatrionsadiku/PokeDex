package com.example.pokedex.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pokedex.data.models.Pokemon
import com.example.pokedex.data.models.PokemonApiResult
import com.example.pokedex.data.models.PokemonResult
import com.example.pokedex.data.models.PokemonsApiResult
import com.example.pokedex.databinding.PokeLayoutBinding

class PokeAdapter(val itemClicker : (pokeName : String, pokeUrl : String) -> Unit) : RecyclerView.Adapter<PokeAdapter.ViewHolder>() {
    var pokemons : List<PokemonResult>
    get() = differ.currentList
    set(value) {
        differ.submitList(value)
    }
    private val differ = AsyncListDiffer(this, diffCallback)



    inner class ViewHolder(val binding: PokeLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun getPokemonPicture(pokemon : PokemonResult, type : String) : String {
            val pokeId = pokemon.url.replace(
                "https://pokeapi.co/api/v2/pokemon/",
                ""
            ).replace("/", "").toInt()

            return when(type){
                "dreamworld" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
                "home" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
                "official" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png"
                "gif" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/$pokeId.gif"
                "xyani" -> "https://img.pokemondb.net/sprites/black-white/anim/normal/${pokemon.name}.gif"
                else -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
            }
        }
        init {
            binding.apply {
                root.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION){
                        val currentPoke = pokemons[currentPosition]
                        itemClicker.invoke(currentPoke.name,currentPoke.getPokemonPicture())
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
            binding.pokeName.text = pokemon.name
            Glide.with(binding.root.context).load(getPokemonPicture(pokemon,"xyani")).into(binding.pokemonPlaceHolder)
        }
    }

    override fun getItemCount(): Int = pokemons.size
}

private val diffCallback = object : DiffUtil.ItemCallback<PokemonResult>() {
    override fun areItemsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
        return oldItem.name == newItem.name
    }
}