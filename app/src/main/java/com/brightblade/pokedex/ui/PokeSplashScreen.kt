package com.brightblade.pokedex.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.brightblade.pokedex.databinding.ActivitySplashScreenBinding
import com.brightblade.pokequiz.PokemonQuizActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PokeSplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(this.window, false)

        binding.apply {
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
        binding.pikaBook.setOnClickListener {
            Intent(this@PokeSplashScreen, MainActivity::class.java).also {
                startActivity(it)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
        binding.quizBook.setOnClickListener {
            Intent(this@PokeSplashScreen, PokemonQuizActivity::class.java).also {
                startActivity(it)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }
    }
}

