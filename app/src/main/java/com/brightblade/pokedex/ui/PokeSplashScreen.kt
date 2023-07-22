package com.brightblade.pokedex.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.brightblade.pokedex.data.persistent.SplashScreenAnimation
import com.brightblade.pokedex.databinding.ActivitySplashScreenBinding
import com.brightblade.pokequiz.PokemonQuizActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokeSplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val viewModel: SplashScreenViewModel by viewModels()
    private val TAG = "SplashScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(this.window, false)

        binding.apply {
            appDestination.setOnClickListener {
                viewModel.onSplashScreenAnimationStateChange(SplashScreenAnimation.PLAYANIMATION)
            }
           lifecycleScope.launch {
              when(viewModel.shouldSplashScreenAnimate.first().shouldAnimate){
                  SplashScreenAnimation.PLAYANIMATION -> {
                      pokeLogoGroup.visibility = View.VISIBLE
                      pokeLogo.alpha = 0f
                      pokeSlogan.alpha = 0f
                      pokeLogo.animate().setDuration(500).alpha(1f).withEndAction {
                          pokeSlogan.animate().setDuration(500).alpha(1f).withEndAction {
                              pokeLogo.animate().setDuration(300).alpha(0f).start()
                              pokeSlogan.animate().setDuration(300).alpha(0f).start()
                              appDestination.animate().setDuration(500).alpha(1f).start()
                              pikaBook.animate().setDuration(500).alpha(1f).start()
                              quizBook.animate().setDuration(500).alpha(1f).start()
                          }
                      }
                  }
                  SplashScreenAnimation.SKIPANIMATION -> {
                      pokeLogoGroup.visibility = View.GONE
                      appDestination.animate().setDuration(1000).alpha(1f).start()
                      pikaBook.animate().setDuration(1000).alpha(1f).start()
                      quizBook.animate().setDuration(1000).alpha(1f).start()
                  }
              }
           }
        }
        binding.pikaBook.setOnClickListener {
            viewModel.onSplashScreenAnimationStateChange(SplashScreenAnimation.SKIPANIMATION)
            Intent(this@PokeSplashScreen, MainActivity::class.java).also {
                startActivity(it)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
        binding.quizBook.setOnClickListener {
            viewModel.onSplashScreenAnimationStateChange(SplashScreenAnimation.SKIPANIMATION)
            Intent(this@PokeSplashScreen, PokemonQuizActivity::class.java).also {
                startActivity(it)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }
}



