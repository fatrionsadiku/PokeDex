package com.brightblade.pokedex.ui.adapters

import android.graphics.Bitmap
import android.graphics.Color
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
import coil.decode.SvgDecoder
import coil.load
import coil.size.Precision
import coil.size.Scale
import com.airbnb.lottie.LottieAnimationView
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.PokemonResult
import com.brightblade.pokedex.databinding.ItemPokemonBinding
import com.brightblade.utils.Utility.listOfColors
import com.brightblade.utils.Utility.listOfSilhouettes
import com.brightblade.utils.capitalize
import com.skydoves.rainbow.Rainbow
import com.skydoves.rainbow.RainbowOrientation
import com.skydoves.rainbow.color
import com.skydoves.rainbow.contextColor

class PokeAdapter(
    val itemClicker: (pokeName: String, pokeId: Int, formattedId: String, dominantColor: Int) -> Unit,
    val favoritePokemon: (position: Int) -> Unit,
    val stateCheckedItemState: CheckedItemState,
    currentPhotoType: String? = null,
) : RecyclerView.Adapter<PokeAdapter.ViewHolder>() {
    var pokemons: List<PokemonResult>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }
    private val differ = AsyncListDiffer(this, diffCallback)
    private var _pokeImageUrl: String? = currentPhotoType

    inner class ViewHolder(val binding: ItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var currentDominantColor = 0

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindData(pokemon: PokemonResult) {
            binding.pokeName.apply {
                text = pokemon.name.capitalize()
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
            binding.pokemonPlaceHolder.load(
                if (_pokeImageUrl != null) getPokemonPicture(
                    pokemon,
                    _pokeImageUrl!!
                ) else getPokemonPicture(pokemon, "official")
            ) {
                placeholder(listOfSilhouettes.random())
                size(192)
                scale(Scale.FIT)
                precision(Precision.EXACT)
                listener { _, result ->
                    binding.pokemonPlaceHolder.load(result.drawable) {
                        crossfade(500)
                    }
                    if (_pokeImageUrl == "xyani") {
                        Rainbow(binding.pokemonDojo).palette {
                            +color(Color.parseColor(listOfColors.random()))
                            +contextColor(R.color.white)
                        }.apply {
                            background(RainbowOrientation.TOP_BOTTOM, 14)
                        }

                    } else {
                        getDominantColor(result.drawable) { hexColor ->
                            Log.d("PokeAdapter", hexColor.toString())
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
                if (_pokeImageUrl == "dreamworld") decoderFactory { result, options, _ ->
                    SvgDecoder(result.source, options)
                } else if (_pokeImageUrl == "xyani") decoderFactory { result, options, _ ->
                    GifDecoder(result.source, options)
                }
            }
            binding.favoriteButton.apply {
                stateCheckedItemState.doesSelectedItemExist(pokemon.name) { exists ->
                    progress = if (exists) 1f else 0f
                }
            }
        }

        init {
            binding.apply {
                root.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        val currentPoke = pokemons[currentPosition]
                        val currentPokeId = getPokemonID(currentPoke)
                        val formattedId = String.format("%04d", currentPokeId)
                        val currentPokemonName = currentPoke.name
                        Log.d("RecyclerView", "$currentPokemonName,$currentPokeId")
                        itemClicker.invoke(
                            currentPokemonName,
                            currentPokeId,
                            formattedId,
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

private val diffCallback = object : DiffUtil.ItemCallback<PokemonResult>() {
    override fun areItemsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: PokemonResult, newItem: PokemonResult): Boolean {
        return oldItem.name == newItem.name
    }
}

private fun getPokemonPicture(pokemon: PokemonResult, type: String): String {
    val pokeId = getPokemonID(pokemon)
    return when (type) {
        "dreamworld" -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/dream-world/$pokeId.svg"
        "home"       -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
        "official"   -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png"
        "gif"        -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/$pokeId.gif"
        "xyani"      -> "https://img.pokemondb.net/sprites/black-white/anim/normal/${pokemon.name}.gif"
        else         -> "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png"
    }
}

private fun getPokemonID(pokemon: PokemonResult) = pokemon.url.replace(
    "https://pokeapi.co/api/v2/pokemon/", ""
).replace("/", "").toInt()

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

interface CheckedItemState {
    fun doesSelectedItemExist(itemName: String, doesItemExist: (result: Boolean) -> Unit)
}
