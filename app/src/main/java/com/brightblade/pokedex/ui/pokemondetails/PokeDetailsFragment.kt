package com.brightblade.pokedex.ui.pokemondetails

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.load
import coil.size.Precision
import coil.size.Scale
import com.airbnb.lottie.LottieAnimationView
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.models.FavoritePokemon
import com.brightblade.pokedex.databinding.FragmentPokemonDetailsBinding
import com.brightblade.pokedex.ui.adapters.FragmentAdapter
import com.brightblade.pokedex.ui.pokemondetails.pokeabilities.PokeAbilities
import com.brightblade.utils.ImageType
import com.brightblade.utils.Resource
import com.brightblade.utils.Utility
import com.brightblade.utils.Utility.HIGHEST_POKEMON_ID
import com.brightblade.utils.Utility.listOfPokemonBackgrounds
import com.brightblade.utils.capitalize
import com.brightblade.utils.getDominantColor
import com.brightblade.utils.requestPermission
import com.google.android.material.tabs.TabLayoutMediator
import com.skydoves.rainbow.Rainbow
import com.skydoves.rainbow.RainbowOrientation
import com.skydoves.rainbow.color
import com.skydoves.rainbow.contextColor
import com.yagmurerdogan.toasticlib.Toastic
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


@AndroidEntryPoint
class PokeDetailsFragment : Fragment(R.layout.fragment_pokemon_details) {
    private val readStoragePermissionResult: ActivityResultLauncher<String> by requestPermission(
        WRITE_EXTERNAL_STORAGE,
        granted = {
            storagePermissionMessage("Storage permissions have been granted")
        },
        denied = {
            storagePermissionMessage("Storage permissions have not been granted")
        })
    val binding by viewBinding(FragmentPokemonDetailsBinding::bind)
    private val pokemonArgs by navArgs<PokeDetailsFragmentArgs>()
    private var currentPokemonName = ""
    var currentPokemonId: Int = 0
    private var currentPhotoIndex = 0
    private var favoriteStatusToast: Toast? = null
    private val pokeViewModel: PokeDetailsSharedViewModel by activityViewModels()
    private val pokeDbViewModel: PokemonDatabaseViewModel by activityViewModels()
    private var currentViewPagerFragment: String = ""
    private var hasNavigatedWithButtons: Boolean = false
    private var currentPokemonLink: String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPokemonDetails(pokemonArgs.pokemonId)
        setUpPokeDetailsViewPager()
        onBackButtonPresed()
        saveDetailsOnClickListener()
        shareDetailsOnClickListener()
        nextPokemonOnClickListener()
        previousPokemonOnClickListener()
        favoritePokemonOnClickListener()
        onPokemonPhotoLongPressListener()
        changePictureOnClickListener()
        observeFavoriteState()
    }

    private fun onPokemonPhotoLongPressListener() {
        binding.pokemonPhoto.setOnLongClickListener {
            val pokemonPhoto = getScreenShotFromView(it, true)
            checkApiAndSavePhoto(pokemonPhoto, true)
            true
        }
    }

    private fun observeFavoriteState() {
        pokeDbViewModel.isPokemonFavoritedState.observe(viewLifecycleOwner) { isPokemonFavorited ->
            when (isPokemonFavorited) {
                true  -> binding.favoritePokemon.isChecked = true
                false -> binding.favoritePokemon.isChecked = false
            }
        }
    }

    private fun favoritePokemonOnClickListener() {
        binding.favoritePokemon.setOnClickListener {
            lifecycleScope.launch {
                when (pokeDbViewModel.doesPokemonExist(currentPokemonName)) {
                    true  -> {
                        pokeDbViewModel.unFavoritePokemon(
                            FavoritePokemon(
                                pokeName = currentPokemonName,
                                url = currentPokemonLink
                            )
                        )
                        if (favoriteStatusToast != null) {
                            favoriteStatusToast!!.cancel()
                        }
                        favoriteStatusToast = Toastic.toastic(
                            context = requireContext(),
                            message = "${currentPokemonName.capitalize()} removed from favorites",
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
                                pokeName = currentPokemonName,
                                url = currentPokemonLink
                            )
                        )
                        if (favoriteStatusToast != null) {
                            favoriteStatusToast!!.cancel()
                        }
                        favoriteStatusToast = Toastic.toastic(
                            context = requireContext(),
                            message = "${currentPokemonName.capitalize()} saved to favorites",
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
        }
    }

    private fun changePictureOnClickListener() {
        binding.changePicture.setOnClickListener {
            (it as LottieAnimationView).cancelAnimation()
            it.playAnimation()
            currentPhotoIndex = (currentPhotoIndex + 1) % 5
            setPokemonPicture(currentPhotoIndex, currentPokemonId)
        }
    }

    private fun previousPokemonOnClickListener() {
        binding.previousPokemonButton.setOnClickListener {
            checkIfPokeAbilitiesIsNotNull()
            if (currentPokemonId > 1) getPokemonDetails(pokeId = currentPokemonId - 1) else getPokemonDetails(
                pokeId = HIGHEST_POKEMON_ID
            )
            hasNavigatedWithButtons = true
            currentPhotoIndex = 0
        }
    }

    private fun nextPokemonOnClickListener() {
        binding.nextPokemonButton.setOnClickListener {
            checkIfPokeAbilitiesIsNotNull()
            if (currentPokemonId == HIGHEST_POKEMON_ID) getPokemonDetails(pokeId = 1) else getPokemonDetails(
                pokeId = currentPokemonId + 1
            )
            hasNavigatedWithButtons = true
            currentPhotoIndex = 0
        }
    }

    private fun checkIfPokeAbilitiesIsNotNull() {
        if ((childFragmentManager.findFragmentByTag("f2") as PokeAbilities?) != null) {
            (childFragmentManager.findFragmentByTag("f2") as PokeAbilities).apply {
                binding.apply {
                    pokeItemsHolder.removeAllViews()
                    pokeDetailsHolder.removeAllViews()
                }
            }
        }
    }

    private fun shareDetailsOnClickListener() {
        binding.shareDetails.setOnClickListener {
            showShareIntent(binding.root)
        }
    }

    private fun saveDetailsOnClickListener() {
        binding.saveDetails.setOnClickListener {
            val currentDetailsScreen = getScreenShotFromView(binding.root)
            checkApiAndSavePhoto(currentDetailsScreen, false)
        }
    }

    private fun checkApiAndSavePhoto(currentScreen: Bitmap?, isPokemonPhoto: Boolean) {
        if (currentScreen != null) {
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                saveMediaToStorage(currentScreen, isPokemonPhoto)
            } else {
                if (!checkPermission(WRITE_EXTERNAL_STORAGE)) {
                    readStoragePermissionResult.launch(WRITE_EXTERNAL_STORAGE)
                } else saveMediaToStorage(currentScreen, isPokemonPhoto)
            }
        }
    }

    fun getPokemonDetails(pokeId: Int) {
        pokeViewModel.getSinglePokemonByName(pokeId)
        pokeViewModel.singlePokemonResponse.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Error   -> Log.e("PokeDetailsFragment", "Error fetching pokemon")
                is Resource.Loading -> {
                    showProgressBar()
                }

                is Resource.Success -> {
                    currentPokemonId = pokeId
                    currentPokemonName = response.data?.name ?: ""
                    currentPokemonLink = "https://pokeapi.co/api/v2/pokemon/$pokeId/"
                    pokeViewModel.pokemonName.postValue(currentPokemonName)
                    lifecycleScope.launch {
                        pokeDbViewModel.doesPokemonExist(
                            currentPokemonName,
                            true
                        )
                    }
                    hideProgressBar()
                    fillPokemonDataOnScreen(response.data?.name ?: "", pokeId)
                    setFragmentResult(
                        "pokemon_id",
                        bundleOf(
                            Pair("navigation_state", hasNavigatedWithButtons),
                            Pair("pokemon_id", pokeId - 2)
                        )
                    )
                }
            }
        }
    }

    private fun fillPokemonDataOnScreen(pokeName: String, pokeId: Int) {
        binding.apply {
            setPokemonPicture(pokeId = pokeId)
            progressBar.isVisible = false
            pokemonName.text = pokeName.capitalize()
            pokemonId.apply {
                val formattedId = String.format("%04d", pokeId)
                text = "#$formattedId"
            }
            progressBar.isVisible = false
        }
    }

    private fun setPokemonPicture(index: Int = 0, pokeId: Int) {
        val imageUrls = listOf(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/home/$pokeId.png",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/dream-world/$pokeId.svg",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$pokeId.png",
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/$pokeId.gif"
        )
        val currentImage = imageUrls[index]
//        Gets the Image type from the given Url, it'll either return a .png,.svg or a .gif'
        val currentImageFormat =
            ImageType.valueOf(
                currentImage.substring(currentImage.length - 3, currentImage.length).uppercase()
            )
        binding.pokemonPhoto.apply {
            load(currentImage) {
                scale(Scale.FIT)
                precision(Precision.AUTOMATIC)
                crossfade(500)
                allowHardware(false)
                listener(
                    onStart = {},
                    onCancel = {},
                    onError = { _, _ ->
                        load(Utility.listOfSilhouettes.random())
                    },
                    onSuccess = { _, successResult ->
                        if (currentImageFormat != ImageType.GIF) {
                            successResult.drawable.getDominantColor { dominantColor ->
                                Rainbow(binding.root).palette {
                                    +contextColor(R.color.white)
                                    +color(dominantColor)
                                }.apply {
                                    background(RainbowOrientation.BOTTOM_TOP)
                                }
                            }
                        }
                    })
                decoderFactory { result, options, _ ->
                    when (currentImageFormat) {
                        ImageType.PNG -> ImageDecoderDecoder(result.source, options)
                        ImageType.SVG -> SvgDecoder(result.source, options)
                        ImageType.GIF -> GifDecoder(result.source, options)
                    }
                }
            }
        }
    }

    private fun setUpPokeDetailsViewPager() {
        binding.apply {
            val adapter = FragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            pokeInfosViewPager.adapter = adapter
            TabLayoutMediator(tabLayout, pokeInfosViewPager) { tab, position ->
                tab.apply {
                    when (position) {
                        0 -> text = "About"

                        1 -> text = "Base Stats"

                        2 -> text = "Abilities/Items"

                        3 -> text = "Evolution Tree"
                    }
                }
            }.attach()
            pokeInfosViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> currentViewPagerFragment = "About"

                        1 -> currentViewPagerFragment = "Base Stats"

                        2 -> currentViewPagerFragment = "Abilities/Items"

                        3 -> currentViewPagerFragment = "Evolution Tree"
                    }
                }
            })
            tabLayout.background = ColorDrawable(Color.WHITE)
        }
    }

    private fun getScreenShotFromView(v: View, isPokemonPhoto: Boolean = false): Bitmap? {
        var screenshot: Bitmap? = null
        val backgroundImage =
            BitmapFactory.decodeResource(resources, listOfPokemonBackgrounds.random())
        var canvas: Canvas? = null
        try {
            if (isPokemonPhoto) {
                // Capture the original view dimensions
                val originalWidth = v.measuredWidth
                val originalHeight = v.measuredHeight

                // Create a screenshot bitmap based on the background image dimensions
                screenshot = Bitmap.createBitmap(
                    backgroundImage.width,
                    backgroundImage.height,
                    Bitmap.Config.ARGB_8888
                )
                canvas = Canvas(screenshot)

                // Calculate the scaling factor (3x)
                val scaleFactor = 3f

                // Create the Pokemon photo with the doubled dimensions
                val pokemonPhoto = Bitmap.createBitmap(
                    (originalWidth * scaleFactor).toInt(),
                    (originalHeight * scaleFactor).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                val pokemonCanvas = Canvas(pokemonPhoto)

                // Scale and draw the contents of the provided view (v) onto the pokemonCanvas
                pokemonCanvas.scale(scaleFactor, scaleFactor)
                v.draw(pokemonCanvas)

                // Calculate the center position on the screenshot for the Pokemon photo
                val centerX = (screenshot.width - pokemonPhoto.width) / 2f
                val centerY = (screenshot.height - pokemonPhoto.height) / 2f

                // Draw the background image on the screenshot
                canvas.drawBitmap(backgroundImage, 0f, 0f, null)

                // Draw the Pokemon photo at the calculated center position
                canvas.drawBitmap(pokemonPhoto, centerX, centerY, null)
            } else {
                // If not capturing the Pokemon photo, just draw the view onto the screenshot
                screenshot =
                    Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
                canvas = Canvas(screenshot)
                v.draw(canvas)
            }
        } catch (e: Exception) {
            Log.e("GFG", "Failed to capture screenshot because:" + e.message)
        }
        return screenshot
    }


    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap, isPokemonPhoto: Boolean = false) {
        // Generating a file name
        val filename =
            "PokeDex_${currentPokemonName.capitalize()}_${currentViewPagerFragment}_${System.currentTimeMillis()}.jpg"
        // Output stream
        var fos: OutputStream? = null
        // For devices running android >= Q
        if (SDK_INT >= Build.VERSION_CODES.Q) {
            // getting the contentResolver
            requireContext().contentResolver?.also { resolver ->
                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {
                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                // Inserting the contentValues to
                // contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                // Opening an outputstream with the Uri that we got
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toastic.toastic(
                context = requireContext(),
                message = if (isPokemonPhoto) "${currentPokemonName.capitalize()}'s photo saved to Gallery"
                else "${currentPokemonName.capitalize()}'s $currentViewPagerFragment saved to Gallery",
                duration = Toastic.LENGTH_SHORT,
                type = Toastic.DEFAULT,
                isIconAnimated = true,
                customIcon = R.drawable.pokeball,
                font = R.font.ryogothic,
                textColor = Color.BLACK,
                customIconAnimation = R.anim.rotate_anim
            ).show()
        }
    }

    private fun showShareIntent(view: View) {
        val intent = Intent(Intent.ACTION_SEND).setType("image/*")
        val currentView = getScreenShotFromView(view)
        try {
            val cachePath = File(requireContext().cacheDir, "sharedpokemon")
            cachePath.mkdirs()
            val stream =
                FileOutputStream("$cachePath/image.png") // overwrites this image every time to save space
            currentView?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val imagePath = File(requireContext().cacheDir, "sharedpokemon")
        val newFile = File(imagePath, "image.png")
        val uri = FileProvider.getUriForFile(
            requireContext(),
            com.brightblade.pokedex.BuildConfig.APPLICATION_ID + ".provider",
            newFile
        )
        val shareMessage = when (currentViewPagerFragment) {
            "Base Stats"      -> "Hey, check out this awesome pokemon, it has some crazy stats!!!. Can you believe how strong it is?"
            "Abilities/Items" -> "Hey, check out this awesome pokemon, it's abilities are INSANE!!!. Can you believe how skilled it is?"
            "Evolution Tree"  -> "Hey, check out this awesome pokemon's evolution tree, such an amazing evolution."
            else              -> "Hey, check out this awesome pokemon"
        }
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        intent.putExtra(Intent.EXTRA_SUBJECT, shareMessage)
        startActivity(Intent.createChooser(intent, "Share pokemon via:"))
    }

    /**
     * Used to check storage permissions on devices running on Android SDK 29 and lower
     * @return Storage Permission State
     */
    private fun checkPermission(permissionName: String): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), permissionName)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun storagePermissionMessage(message: String) {
        Toastic.toastic(
            context = requireContext(),
            message = message,
            duration = Toastic.LENGTH_SHORT,
            type = Toastic.DEFAULT,
            isIconAnimated = true,
            customIcon = R.drawable.pokeball,
            font = R.font.ryogothic,
            textColor = Color.BLACK,
            customIconAnimation = R.anim.rotate_anim
        ).show()
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun onBackButtonPresed() {
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}