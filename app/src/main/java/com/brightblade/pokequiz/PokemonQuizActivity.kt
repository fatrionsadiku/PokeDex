package com.brightblade.pokequiz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.brightblade.pokedex.databinding.ActivityQuizBinding

class PokemonQuizActivity : AppCompatActivity() {

    lateinit var binding : ActivityQuizBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(this.window, false)
    }
}