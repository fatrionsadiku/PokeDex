package com.example.pokedex.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pokedex.databinding.ActivitySplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokeSplashScreen : AppCompatActivity() {
    private lateinit var binding : ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            pokeLogo.alpha = 0f
            pokeSlogan.alpha = 0f
            pokeLogo.animate().setDuration(500).alpha(1f).withEndAction {
              pokeSlogan.animate().setDuration(500).alpha(1f).withEndAction {
                  Intent(this@PokeSplashScreen, MainActivity::class.java).also {
                      startActivity(it)
                      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                      finish()
                  }
              }
            }
        }
    }
}