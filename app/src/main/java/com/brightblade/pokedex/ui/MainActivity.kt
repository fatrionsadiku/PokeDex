package com.brightblade.pokedex.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.ActivityMainBinding
import com.brightblade.pokedex.ui.homefragment.HomeFragment
import dagger.hilt.android.AndroidEntryPoint
import nl.joery.animatedbottombar.AnimatedBottomBar

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private var tabReselectedListener: OnHomeButtonReselected? = null
    val binding get() = _binding!!
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
        setUpBottomNavBar()

    }

    private fun setUpBottomNavBar() {
        navHost =
            (supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())
            // Apply the insets as a margin to the view. Here the system is setting
            // only the bottom, left, and right dimensions, but apply whichever insets are
            // appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)
            // Return CONSUMED if you don't want want the window insets to keep being
            // passed down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        navHost.navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.pokeDetailsFragment2) {
                binding.bottomNavView.animate().translationY(200f).setDuration(200)
                    .withEndAction {
                        binding.bottomNavView.visibility = View.GONE
                    }.start()
            } else
                binding.bottomNavView.animate().apply {
                    binding.bottomNavView.visibility = View.VISIBLE
                    translationY(0f)
                        duration = 200
                    }.start()
            }
            binding.bottomNavView.setOnTabSelectListener(object :
                AnimatedBottomBar.OnTabSelectListener {
                override fun onTabSelected(
                    lastIndex: Int,
                    lastTab: AnimatedBottomBar.Tab?,
                    newIndex: Int,
                    newTab: AnimatedBottomBar.Tab,
                ) {
                    when (newIndex) {
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

                // An optional method that will be fired whenever an already selected tab has been selected again.
                override fun onTabReselected(index: Int, tab: AnimatedBottomBar.Tab) {
                    when (index) {
                        0 -> {
                            tabReselectedListener =
                                navHost.childFragmentManager.fragments[0] as? HomeFragment
                            tabReselectedListener?.onHomeButtonReSelected()
                        }
                    }
                }
            })
    }

    interface OnHomeButtonReselected {
        fun onHomeButtonReSelected()
    }
}
