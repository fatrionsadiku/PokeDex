package com.brightblade.pokedex.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.ActivityMainBinding
import com.brightblade.pokedex.ui.homefragment.HomeViewModel
import com.brightblade.utils.customviews.CustomBottomMenuItem
import com.skydoves.androidbottombar.OnMenuItemSelectedListener
import com.skydoves.androidbottombar.animations.BadgeAnimation
import com.skydoves.androidbottombar.forms.badgeForm
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var navHost: NavHostFragment
    override fun onResume() {
        super.onResume()
        Log.d("ActivityState", navHost.navController.currentDestination?.label.toString())
        if (navHost.navController.currentDestination?.label == "PokeDetailsFragment") {
            binding.bottomNavView.animate().translationY(200f).setDuration(1).withEndAction {
                binding.bottomNavView.visibility = View.GONE
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHost =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment)
        WindowCompat.setDecorFitsSystemWindows(this@MainActivity.window, false)

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