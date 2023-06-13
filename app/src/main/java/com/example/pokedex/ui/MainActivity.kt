package com.example.pokedex.ui

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.pokedex.R
import com.example.pokedex.databinding.ActivityMainBinding
import com.example.pokedex.utils.customviews.CustomBottomMenuItem
import com.skydoves.androidbottombar.BottomMenuItem
import com.skydoves.androidbottombar.OnMenuItemSelectedListener
import com.skydoves.androidbottombar.animations.BadgeAnimation
import com.skydoves.androidbottombar.forms.badgeForm
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment).navController
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

        navController.addOnDestinationChangedListener { _, destination, _ ->
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
                    0 -> navController.navigate(R.id.homeFragment)
                    1 -> navController.navigate(R.id.favoritePokemons)
                }
            }
    }
}