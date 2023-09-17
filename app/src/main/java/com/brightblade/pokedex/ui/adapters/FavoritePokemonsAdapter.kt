package com.brightblade.pokedex.ui.adapters

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.databinding.ItemPokemonBinding
import com.brightblade.utils.capitalize
import com.brightblade.utils.getDominantColor
import com.skydoves.rainbow.Rainbow
import com.skydoves.rainbow.RainbowOrientation
import com.skydoves.rainbow.color
import com.skydoves.rainbow.contextColor

class FavoritePokemonsAdapter(
    val itemClicker: (pokeName: String, pokeId: Int?, dominantColor: Int) -> Unit,
    val favoritePokemon: (position: Int) -> Unit,
    val stateCheckedItemState: CheckedItemState,
) : RecyclerView.Adapter<FavoritePokemonsAdapter.ViewHolder>() {
    var pokemons: List<FavoritePokemon>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }
    private val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var currentDominantColor = 0

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindData(pokemon: FavoritePokemon) {
            binding.pokeName.apply {
                text = pokemon.pokeName.capitalize()
                alpha = 0f
            }.animate().setDuration(500).alpha(1f)
            binding.pokeId.apply {
                val currentId = getPokemonID(pokemon)
                val formattedId = String.format("%04d", currentId)
                text = "Nᵒ $formattedId"
                alpha = 0f
            }.animate().setDuration(500).alpha(1f)
            binding.pokemonDojo.apply {
                alpha = 0.5f
            }.animate().setDuration(500).alpha(1f)
            binding.pokemonPlaceHolder.load(getPokemonPicture(pokemon, "official")) {
                listener { _, result ->
                    binding.pokemonPlaceHolder.load(result.drawable) { crossfade(500) }
                    result.drawable.getDominantColor { hexColor ->
                        currentDominantColor = hexColor
                        Rainbow(binding.pokemonDojo).palette {
                            +contextColor(R.color.white)
                            +color(hexColor)
                        }.apply {
                            background(RainbowOrientation.BOTTOM_TOP, 14)
                        }
                    }
                }
            }
            binding.favoriteButton.apply {
                stateCheckedItemState.doesSelectedItemExist(pokemon.pokeName) {
                    when (it) {
                        true -> {
                            binding.favoriteButton.progress = 1f
                        }

                        false -> {
                            binding.favoriteButton.progress = 0f
                        }
                    }
                }
            }
        }

        private fun getPokemonID(pokemon: FavoritePokemon) = pokemon.url?.replace(
            "https://pokeapi.co/api/v2/pokemon/", ""
        )?.replace("/", "")?.toInt()

        private fun getPokemonPicture(pokemon: FavoritePokemon, type: String): String {
            val pokeId = getPokemonID(pokemon)
            return when (type) {
                "dreamworld" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
                "home"       -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
                "official"   -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png"
                "gif"        -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/$pokeId.gif"
                "xyani"      -> "https://img.pokemondb.net/sprites/black-white/anim/normal/${pokemon.pokeName}.gif"
                else         -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
            }
        }

        init {
            binding.apply {
                root.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        val currentPoke = pokemons[currentPosition]
                        val currentPokeId = getPokemonID(currentPoke)
                        itemClicker.invoke(
                            currentPoke.pokeName,
                            currentPokeId,
                            currentDominantColor
                        )
                    }
                }
                favoriteButton.setOnClickListener { lottieView ->
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        val lottieAnimationView = lottieView as LottieAnimationView
                        favoritePokemon.invoke(currentPosition)
                        lottieAnimationView.progress =
                            if (lottieAnimationView.progress == 1f) 0f else 1f
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemPokemonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemons[position]
        holder.bindData(pokemon)
    }

    override fun getItemCount(): Int = pokemons.size
}

private val diffCallback = object : DiffUtil.ItemCallback<FavoritePokemon>() {
    override fun areItemsTheSame(oldItem: FavoritePokemon, newItem: FavoritePokemon): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FavoritePokemon, newItem: FavoritePokemon): Boolean {
        return oldItem.pokeName == newItem.pokeName
    }

}


