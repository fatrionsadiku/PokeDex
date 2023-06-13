package com.example.pokedex.adapters

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.decode.GifDecoder
import coil.load
import com.airbnb.lottie.LottieAnimationView
import com.example.pokedex.R
import com.example.pokedex.data.models.FavoritePokemon
import com.example.pokedex.data.models.PokemonResult
import com.example.pokedex.databinding.PokeLayoutBinding
import com.example.pokedex.utils.capitalize
import com.skydoves.rainbow.Rainbow
import com.skydoves.rainbow.RainbowOrientation
import com.skydoves.rainbow.color
import com.skydoves.rainbow.contextColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavoritePokemonsAdapter(
    val itemClicker: (pokeName: String, pokeId: Int?) -> Unit,
    val favoritePokemon: (position: Int) -> Unit,
    val stateCheckedItemState: CheckedItemState
) : RecyclerView.Adapter<FavoritePokemonsAdapter.ViewHolder>() {
    var pokemons: List<FavoritePokemon>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }
    private val differ = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(val binding: PokeLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        //Will try to refactor code as soon as i find a workaround to loading images and getting dominant color at the same time
        @RequiresApi(Build.VERSION_CODES.O)
        fun bindData(pokemon: FavoritePokemon) {
            binding.pokeName.apply {
                text = pokemon.pokeName.capitalize()
                alpha = 0f
            }.animate().setDuration(500).alpha(1f)
            binding.pokemonPlaceHolder.load(getPokemonPicture(pokemon, "official")) {
                crossfade(500)
                listener(onError = { _, error ->
                    Log.d("SUBTAG", "Error -> ${error.throwable.message}")
                }, onSuccess = { _, _ ->
                    Log.d("SUBTAG", "Content image loaded")
                })
                target(onSuccess = { result ->
                    getDominantColor(result) { hexColor ->
                        Rainbow(binding.pokemonDojo).palette {
                            +contextColor(R.color.white)
                            +color(hexColor)
                        }.apply {
                            background(RainbowOrientation.BOTTOM_TOP, 14)
                        }
                    }
                })
            }
            binding.pokemonPlaceHolder.load(getPokemonPicture(pokemon, "xyani")) {
                crossfade(500)
                decoderFactory { result, options, _ ->
                    GifDecoder(result.source, options)
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
                "home" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
                "official" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png"
                "gif" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/$pokeId.gif"
                "xyani" -> "https://img.pokemondb.net/sprites/black-white/anim/normal/${pokemon.pokeName}.gif"
                else -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
            }
        }

        init {
            binding.apply {
                root.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        val currentPoke = pokemons[currentPosition]
                        val currentPokeId = getPokemonID(currentPoke)
                        itemClicker.invoke(currentPoke.pokeName, currentPokeId)
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
            PokeLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pokemon = pokemons[position]
        holder.bindData(pokemon)
    }

    override fun getItemCount(): Int = pokemons.size
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getDominantColor(drawable: Drawable, onFinish: (Int) -> Unit) {
    val bitMap = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
    Palette.from(bitMap).generate { palette ->
        palette?.dominantSwatch?.let { dominantColor ->
            val color = dominantColor.rgb
            onFinish(color)
        }
    }
}

private val diffCallback = object : DiffUtil.ItemCallback<FavoritePokemon>() {
    override fun areItemsTheSame(oldItem: FavoritePokemon, newItem: FavoritePokemon): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: FavoritePokemon, newItem: FavoritePokemon): Boolean {
        return oldItem.pokeName == newItem.pokeName
    }

}


