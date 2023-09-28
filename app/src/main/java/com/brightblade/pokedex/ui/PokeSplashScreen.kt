package com.brightblade.pokedex.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.brightblade.pokedex.data.persistent.SplashScreenAnimation
import com.brightblade.pokedex.databinding.ActivitySplashScreenBinding
import com.brightblade.pokequiz.PokemonQuizActivity
import com.brightblade.utils.fadeIn
import com.brightblade.utils.fadeOut
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PokeSplashScreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private val viewModel: SplashScreenViewModel by viewModels()
    private var isOutsidePikaBook = false
    private var isOutsidePikaQuiz = false

    override fun onDestroy() {
        viewModel.onSplashScreenAnimationStateChange(SplashScreenAnimation.PLAYANIMATION)
        super.onDestroy()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(this.window, false)

        binding.apply {
            lifecycleScope.launch {
                when(viewModel.shouldSplashScreenAnimate.first().shouldAnimate){
                    SplashScreenAnimation.PLAYANIMATION -> {
                        pokeLogoGroup.visibility = View.VISIBLE
                        pokeLogo.alpha = 0f
                        pokeSlogan.alpha = 0f
                        pokeLogo.fadeIn().withEndAction {
                            pokeSlogan.fadeIn().withEndAction {
                                pokeLogo.fadeOut().start()
                                pokeSlogan.fadeOut().start()
                                appDestination.fadeIn().start()
                                pikaBook.fadeIn().start()
                                quizBook.fadeIn().start()
                            }
                        }
                    }

                    SplashScreenAnimation.SKIPANIMATION -> {
                        pokeLogoGroup.visibility = View.GONE
                        appDestination.fadeIn(duration = 1000).start()
                        pikaBook.fadeIn(duration = 1000).start()
                        quizBook.fadeIn(duration = 1000).start()
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
        binding.pikaBook.setOnTouchListener { pikaBookButton, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ObjectAnimator.ofFloat(pikaBookButton, View.ROTATION, 0f, 10f).setDuration(100L)
                        .start()
                    isOutsidePikaBook = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isInsideView(
                            event.rawX,
                            event.rawY,
                            pikaBookButton
                        ) && !isOutsidePikaBook
                    ) {
                        // User's finger moved outside of the button, trigger the rotation here.
                        ObjectAnimator.ofFloat(pikaBookButton, View.ROTATION, 10f, 0f)
                            .setDuration(100L).start()
                        isOutsidePikaBook = true
                    }
                }
            }
            false
        }

        binding.quizBook.setOnTouchListener { pikaQuizButton, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ObjectAnimator.ofFloat(pikaQuizButton, View.ROTATION, 0f, -10f)
                        .setDuration(100L).start()
                    isOutsidePikaQuiz = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (!isInsideView(
                            event.rawX,
                            event.rawY,
                            pikaQuizButton
                        ) && !isOutsidePikaQuiz
                    ) {
                        // User's finger moved outside of the button, trigger the rotation here.
                        ObjectAnimator.ofFloat(pikaQuizButton, View.ROTATION, -10f, 0f)
                            .setDuration(100L).start()
                        isOutsidePikaQuiz = true
                    }
                }
            }
            false
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

    private fun isInsideView(x: Float, y: Float, view: View): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val viewRect = Rect(
            location[0],
            location[1],
            location[0] + view.width,
            location[1] + view.height
        )
        return viewRect.contains(x.toInt(), y.toInt())
    }
}



