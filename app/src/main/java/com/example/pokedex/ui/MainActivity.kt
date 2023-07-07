package com.example.pokedex.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import coil.decode.GifDecoder
import coil.load
import com.example.pokedex.R
import com.example.pokedex.databinding.ActivityMainBinding
import com.example.pokedex.ui.homefragment.HomeViewModel
import com.example.pokedex.utils.NetworkConnection
import com.example.pokedex.utils.customviews.CustomBottomMenuItem
import com.skydoves.androidbottombar.OnMenuItemSelectedListener
import com.skydoves.androidbottombar.animations.BadgeAnimation
import com.skydoves.androidbottombar.forms.badgeForm
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHost =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment)
        WindowCompat.setDecorFitsSystemWindows(this@MainActivity.window, false)


        NetworkConnection(this).observe(this) { isConnectedToInternet ->
            when (isConnectedToInternet) {
                true  -> {
//                    val homeFragment =
//                        navHost.childFragmentManager.fragments.first() as HomeFragment
                    binding.internetGroup.visibility = View.VISIBLE
                    binding.noInternetGroup.visibility = View.GONE
                    binding.root.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.pokedetailsbg, null)
                }

                false -> {
                    if (viewModel.doesCachedPokemonDatabaseHaveItems.value == false) {
                        binding.internetGroup.visibility = View.GONE
                        binding.root.background =
                            ResourcesCompat.getDrawable(resources, R.color.white, null)
                        binding.noInternetGif.load(R.drawable.no_internet) {
                            decoderFactory { result, options, _ ->
                                GifDecoder(result.source, options)
                            }
                        }
                        binding.noInternetGroup.visibility = View.VISIBLE
                    }
                }
            }
        }

        binding.bottomNavView.addBottomMenuItems(
            mutableListOf(
                CustomBottomMenuItem(this, title = "Home")
                    .setIcon(R.drawable.baseline_home_24)
                    .build(),
                CustomBottomMenuItem(this, title = "Favorites")
                    .setBadgeTextSize(12f)
                    .setBadgeMargin(8)
                    .setBadgeTextColorRes(R.color.white)
                    .setBadgeColorRes(R.color.lit_blue)
                    .setBadgeAnimation(BadgeAnimation.FADE)
                    .setBadgeDuration(450)
                    .setIcon(R.drawable.baseline_favorite_24)
                    .build()
            )
        )

        binding.bottomNavView.apply {
            setOnBottomMenuInitializedListener {
                badgeForm(this@MainActivity) {
                    setBadgeTextSize(9f)
                    setBadgePaddingLeft(20)
                    setBadgePaddingRight(6)
                    setBadgeDuration(550)
                    setBadgeStyle(R.font.ryogothic)
                }
            }
        }

        navHost.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.pokeDetailsFragment2) {
                binding.bottomNavView.animate().translationY(200f).setDuration(200).withEndAction {
                    binding.bottomNavView.visibility = View.GONE
                }.start()
            } else
                binding.bottomNavView.animate().apply {
                    binding.bottomNavView.visibility = View.VISIBLE
                    translationY(0f)
                    duration = 200
                }.start()
        }
        binding.bottomNavView.onMenuItemSelectedListener =
            OnMenuItemSelectedListener { index, _, _ ->
                when (index) {
                    0 -> navHost.navController.navigate(
                        resId = R.id.homeFragment,
                        args = null,
                        navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_left)
                            .setExitAnim(R.anim.slide_out_right).build()
                    )

                    1 -> navHost.navController.navigate(
                        resId = R.id.favoritePokemons,
                        args = null,
                        navOptions = NavOptions.Builder().setEnterAnim(R.anim.slide_in_right)
                            .setExitAnim(R.anim.slide_out_left).build()
                    )
                }
            }
    }
}