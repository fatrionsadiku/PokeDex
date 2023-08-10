package com.brightblade.pokedex.ui.pokemondetails

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.brightblade.pokedex.R
import com.brightblade.pokedex.data.persistent.HideDetails
import com.brightblade.pokedex.databinding.FragmentPokemonDetailsBinding
import com.brightblade.pokedex.ui.adapters.FragmentAdapter
import com.brightblade.pokedex.ui.pokemondetails.pokeabilities.PokeAbilities
import com.brightblade.utils.Resource
import com.brightblade.utils.capitalize
import com.google.android.material.tabs.TabLayoutMediator
import com.yagmurerdogan.toasticlib.Toastic
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


@AndroidEntryPoint
class PokeDetailsFragment : Fragment(R.layout.fragment_pokemon_details) {
    val binding by viewBinding(FragmentPokemonDetailsBinding::bind)
    private val pokemonArgs by navArgs<PokeDetailsFragmentArgs>()
    private var currentPokemonName = ""
    var currentPokemonId: Int = 0
    private var hideDetails = false
    private val pokeViewModel: PokeDetailsSharedViewModel by activityViewModels()
    private var currentViewPagerFragment: String = ""
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPokemonDetails(pokemonArgs.pokemonId)
        setUpPokeDetailsViewPager()
        setUpDetailsState()
        onBackButtonPresed()
        saveDetailsOnClickListener()
        shareDetailsOnClickListener()
        nextPokemonOnClickListener()
        previousPokemonOnClickListener()
    }

    private fun previousPokemonOnClickListener() {
        binding.previousPokemonButton.setOnClickListener {
            checkIfPokeAbilitiesIsNotNull()
            getPokemonDetails(pokeId = currentPokemonId - 1)
        }
    }

    private fun nextPokemonOnClickListener() {
        binding.nextPokemonButton.setOnClickListener {
            checkIfPokeAbilitiesIsNotNull()
            getPokemonDetails(pokeId = currentPokemonId + 1)

        }
    }

    private fun checkIfPokeAbilitiesIsNotNull() {
        if ((childFragmentManager.findFragmentByTag("f1") as PokeAbilities?) != null) {
            (childFragmentManager.findFragmentByTag("f1") as PokeAbilities).apply {
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
            if (currentDetailsScreen != null) {
                if (SDK_INT >= Build.VERSION_CODES.Q) {
                    saveMediaToStorage(currentDetailsScreen)
                } else {
                    if (!checkPermission()) {
                        verifyStoragePermissions()
                    } else saveMediaToStorage(currentDetailsScreen)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pokeViewModel.pokemonDescription.value = null
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
                    hideProgressBar()
                    fillPokemonDataOnScreen(response.data?.name ?: "", pokeId)
                }
            }
        }
        pokeViewModel.pokemonDescription.observe(viewLifecycleOwner) { descriptionResponse ->
            when (descriptionResponse) {
                is Resource.Error   -> {
                    binding.pokemonDescription.text =
                        "Whoops, this pokemon's bio seems to be missing\n we apologize for the inconvenience"
                }

                is Resource.Loading -> {
                    binding.pokemonDescription.text = ""
                    if (hideDetails) {
                        binding.pokeDescriptionLoadingAnimation.apply {
                            visibility = View.VISIBLE
                            playAnimation()
                        }
                    }
                }

                is Resource.Success -> {
                    lifecycleScope.launch {
                        delay(500)
                        binding.pokeDescriptionLoadingAnimation.apply {
                            visibility = View.INVISIBLE
                            cancelAnimation()
                        }
                        val stringBuilder = StringBuilder()
                        descriptionResponse.data?.forEach { pokeDescription ->
                            stringBuilder.append(
                                pokeDescription.replace("\n", " ").replace(".", ".\n")
                            )
                                .append("\n")
                        }
                        binding.pokemonDescription.text = stringBuilder.toString()
                    }


                }
            }
        }
    }

    private fun fillPokemonDataOnScreen(pokeName: String, pokeId: Int) {
        binding.apply {
            pokemonPhoto.load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$pokeId.png") {
                crossfade(500)
                allowHardware(false)
            }
            progressBar.isVisible = false
            pokemonName.text = pokeName.capitalize()
            progressBar.isVisible = false
        }
    }

    private fun setUpPokeDetailsViewPager() {
        binding.apply {
            val adapter = FragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
            pokeInfosViewPager.adapter = adapter
            TabLayoutMediator(tabLayout, pokeInfosViewPager) { tab, position ->
                tab.apply {
                    when (position) {
                        0 -> {
                            text = "Details"
                        }

                        1 -> {
                            text = "Abilities/Items"
                        }

                        2 -> {
                            text = "Evolution Tree"
                        }
                    }
                }
            }.attach()
            pokeInfosViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when (position) {
                        0 -> {
                            currentViewPagerFragment = "Details"
                        }

                        1 -> {
                            currentViewPagerFragment = "Abilities/Items"
                        }

                        2 -> {
                            currentViewPagerFragment = "Evolution Tree"
                        }
                    }
                }
            })
            tabLayout.background = ColorDrawable(Color.WHITE)
        }
    }

    private fun getScreenShotFromView(v: View): Bitmap? {
        // create a bitmap object
        var screenshot: Bitmap? = null
        try {
            // inflate screenshot object
            // with Bitmap.createBitmap it
            // requires three parameters
            // width and height of the view and
            // the background color
            screenshot =
                Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
            // Now draw this bitmap on a canvas
            val canvas = Canvas(screenshot)
            v.draw(canvas)
        } catch (e: Exception) {
            Log.e("GFG", "Failed to capture screenshot because:" + e.message)
        }
        // return the bitmap
        return screenshot
    }

    // this method saves the image to gallery
    private fun saveMediaToStorage(bitmap: Bitmap) {
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
                message = "${currentPokemonName.capitalize()}'s $currentViewPagerFragment saved to Gallery",
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
            "Details"         -> "Hey, check out this awesome pokemon, it has some crazy stats!!!. Can you believe how strong it is?"
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
    Checks and verifies the storage permissions required by the application.
    Iterates through a list of permissions and prompts the user to grant any missing permissions.
    Permissions required for storage access are declared in the PERMISSIONS_STORAGE array.
    The function checks if each permission is granted using ActivityCompat.checkSelfPermission().
    If any permission is not granted, it requests the permissions using ActivityCompat.requestPermissions().
     */
    private fun verifyStoragePermissions() {
        val PERMISSIONS_STORAGE = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
        )
        for (permission in PERMISSIONS_STORAGE) {
            val currentPermission = ActivityCompat.checkSelfPermission(
                requireActivity(),
                permission
            )
            if (currentPermission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    PERMISSIONS_STORAGE,
                    1
                )
            }
        }
    }

    /**
     * Used to check storage permissions on devices running on Android SDK 29 and lower
     * @return Storage Permission State
     */
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun setUpDetailsState() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (pokeViewModel.hideDetailsFlow.first().detailsState) {
                HideDetails.SHOW_ONLY_POKEMON -> {
                    binding.hideDetailsButton.progress = 1f
                    binding.topPartPokeDetails.visibility = View.INVISIBLE
                    binding.pokemonDescription.visibility = View.VISIBLE
                    hideDetails = true
                }

                HideDetails.SHOW_ALL_DETAILS  -> {
                    binding.hideDetailsButton.progress = 0f
                    binding.topPartPokeDetails.visibility = View.VISIBLE
                    binding.pokemonDescription.visibility = View.INVISIBLE
                    hideDetails = false
                }
            }
        }
        binding.hideDetailsButton.setOnClickListener {
            if (hideDetails) {
                pokeViewModel.onHideDetailsStateSelected(HideDetails.SHOW_ALL_DETAILS)
                binding.hideDetailsButton.apply {
                    speed = -1f
                    playAnimation()
                    progress = 1f
                }
                binding.pokemonDescription.apply {
                    alpha = 1f
                    animate().apply {
                        visibility = View.INVISIBLE
                        duration = 500
                        alpha(0f)
                    }.start()
                }
                binding.topPartPokeDetails.apply {
                    alpha = 0f
                    animate().apply {
                        visibility = View.VISIBLE
                        duration = 500
                        alpha(1f)
                    }.start()
                    hideDetails = !hideDetails
                }
            } else {
                pokeViewModel.onHideDetailsStateSelected(HideDetails.SHOW_ONLY_POKEMON)
                binding.hideDetailsButton.apply {
                    speed = 1f
                    playAnimation()
                    progress = 0f
                }
                binding.pokemonDescription.apply {
                    alpha = 0f
                    animate().apply {
                        visibility = View.VISIBLE
                        duration = 500
                        alpha(1f)
                    }.start()
                }
                binding.topPartPokeDetails.apply {
                    alpha = 1f
                    animate().apply {
                        visibility = View.INVISIBLE
                        duration = 500
                        alpha(0f)
                    }.start()
                    hideDetails = !hideDetails
                }
            }
        }
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