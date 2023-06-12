package com.example.pokedex.ui

import android.os.Bundle
import android.view.Gravity
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.example.pokedex.R
import com.example.pokedex.databinding.ActivityMainBinding
import com.example.pokedex.viewmodels.HomeViewModel
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
        WindowCompat.setDecorFitsSystemWindows(this@MainActivity.window, false)
        binding.bottomNavView.addBottomMenuItems(
            mutableListOf(
                BottomMenuItem(this)
                    .setTitle("Home") // sets the content of the title.
                    .setTitleColorRes(R.color.black) // sets the color of the title using resource.
                    .setTitleActiveColorRes(R.color.white) // sets the color of the title when selected/active.
                    .setTitlePadding(6) // sets the padding of the title.
                    .setTitleSize(14f) // sets the size of the title.
                    .setTitleGravity(Gravity.CENTER) // sets gravity of the title.
                    .setIcon(R.drawable.baseline_home_24)
                    .setIconColorRes(R.color.white) // sets the [Drawable] of the icon using resource.
                    .setIconActiveColorRes(R.color.purple_700) // sets the color of the icon when selected/active.
                    .setIconSize(20)
                    .build(),
                BottomMenuItem(this)
                    .setTitle("Favorites") // sets the content of the title.
                    .setTitleColorRes(R.color.black) // sets the color of the title using resource.
                    .setTitleActiveColorRes(R.color.white) // sets the color of the title when selected/active.
                    .setTitlePadding(6) // sets the padding of the title.
                    .setTitleSize(14f) // sets the size of the title.
                    .setTitleGravity(Gravity.CENTER) // sets gravity of the title.
                    .setIcon(R.drawable.baseline_favorite_24)
                    .setIconColorRes(R.color.white) // sets the [Drawable] of the icon using resource.
                    .setIconActiveColorRes(R.color.purple_700)
                    .setIconSize(20)// sets the color of the icon when selected/active.
                    .setBadgeTextSize(12f)
                    .setBadgeMargin(8)// sets the size of the badge.
                    .setBadgeTextColorRes(R.color.white) // sets the text color of the badge using resource.
                    .setBadgeColorRes(R.color.lit_blue) // sets the color of the badge using resource.
                    .setBadgeAnimation(BadgeAnimation.FADE) // sets an animation of the badge.
                    .setBadgeDuration(450) // sets a duration of the badge.
                    .build()
            )
        )

        binding.bottomNavView.apply {
            setOnBottomMenuInitializedListener {
                badgeForm(this@MainActivity){
                    setBadgeTextSize(9f)
                    setBadgePaddingLeft(20)
                    setBadgePaddingRight(6)
                    setBadgeDuration(550)
                    setBadgeStyle(R.font.ryogothic)
                }
            }
        }
        binding.bottomNavView.onMenuItemSelectedListener =
            OnMenuItemSelectedListener { index, _, _ ->
                 val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
                 val navController = navHostFragment.navController
                when(index){
                    0 -> navController.navigate(R.id.homeFragment)
                    1 -> navController.navigate(R.id.favoritePokemons)
                }
            }

//        viewModel.totalNumberOfFavs.observe(this) {
//            binding.bottomNavView.showBadge(1, "$it")
//
//        }
    }
}