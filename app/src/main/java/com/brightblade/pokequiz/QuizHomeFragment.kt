package com.brightblade.pokequiz

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.FragmentQuizHomeBinding
import com.brightblade.pokedex.ui.PokeSplashScreen
import com.brightblade.pokequiz.quizmodels.Quiz
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class QuizHomeFragment : Fragment(R.layout.fragment_quiz_home) {
    private val binding by viewBinding(FragmentQuizHomeBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        onBackPressed()
    }

    private fun setUpRecyclerView() {
        val quizAdapter = QuizAdapter() { quizType ->
            val action = QuizHomeFragmentDirections.actionHomeFragmentToQuizFragment(quizType)
            findNavController().navigate(action)
        }
        val listOfQuizzes = listOf(
            Quiz(
                "1",
                "Abilities",
                "Test your knowledge of Pokémon abilities!\n Identify the unique traits and powers possessed by different Pokémon in this quiz.\n Choose the correct ability from a list of options, including enticing but incorrect choices.\n Can you achieve a perfect score?"
            ),
            Quiz(
                "2",
                "Characteristics",
                "Explore the world of Pokémon biology!\n Match descriptions of Pokémon's physical attributes, habitats, and behaviors to the correct species.\n Put your knowledge to the test and prove yourself as a true Pokémon biologist."
            ),
            Quiz(
                "3",
                "Physical",
                "Sharpen your observational skills!\n Identify Pokémon based on their distinct physical appearances.\n Can you spot the Pokémon that matches the given description or appearance?\n Test your visual acuity and see how many you can correctly identify!"
            )
        )
        quizAdapter.quizzes = listOfQuizzes
        binding.quizRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizAdapter
        }
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            Intent(requireActivity(), PokeSplashScreen::class.java).also {
                val animations = ActivityOptions.makeCustomAnimation(requireContext(),android.R.anim.fade_in, android.R.anim.fade_out)
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it,animations.toBundle())
                requireActivity().finish()
            }
        }
    }
}