package com.brightblade.pokequiz

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.FragmentQuizHomeBinding
import com.brightblade.pokequiz.quizmodels.Quiz
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class QuizHomeFragment : Fragment(R.layout.fragment_quiz_home) {
    private val binding by viewBinding(FragmentQuizHomeBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        val quizAdapter = QuizAdapter(){quizType ->
            val action = QuizHomeFragmentDirections.actionHomeFragmentToQuizFragment(quizType)
            findNavController().navigate(action)
        }
        val listOfQuizzes = listOf(
            Quiz(
                "1",
                "abilities",
                "Test your knowledge of Pokémon abilities!\n Identify the unique traits and powers possessed by different Pokémon in this quiz.\n Choose the correct ability from a list of options, including enticing but incorrect choices.\n Can you achieve a perfect score?"
            ),
            Quiz(
                "2",
                "characteristics",
                "Explore the world of Pokémon biology!\n Match descriptions of Pokémon's physical attributes, habitats, and behaviors to the correct species.\n Put your knowledge to the test and prove yourself as a true Pokémon biologist."
            ),
            Quiz(
                "3",
                "physical",
                "Sharpen your observational skills!\n Identify Pokémon based on their distinct physical appearances.\n Can you spot the Pokémon that matches the given description or appearance?\n Test your visual acuity and see how many you can correctly identify!"
            ),
            Quiz(
                "1",
                "PokeAbilities",
                "Test your knowledge of Pokémon abilities!\n Identify the unique traits and powers possessed by different Pokémon in this quiz.\n Choose the correct ability from a list of options, including enticing but incorrect choices.\n Can you achieve a perfect score?"
            ),
            Quiz(
                "2",
                "PokeBio",
                "Explore the world of Pokémon biology!\n Match descriptions of Pokémon's physical attributes, habitats, and behaviors to the correct species.\n Put your knowledge to the test and prove yourself as a true Pokémon biologist."
            ),
            Quiz(
                "3",
                "PokeApperance",
                "Sharpen your observational skills!\n Identify Pokémon based on their distinct physical appearances.\n Can you spot the Pokémon that matches the given description or appearance?\n Test your visual acuity and see how many you can correctly identify!"
            ),
            Quiz(
                "1",
                "PokeAbilities",
                "Test your knowledge of Pokémon abilities!\n Identify the unique traits and powers possessed by different Pokémon in this quiz.\n Choose the correct ability from a list of options, including enticing but incorrect choices.\n Can you achieve a perfect score?"
            ),
            Quiz(
                "2",
                "PokeBio",
                "Explore the world of Pokémon biology!\n Match descriptions of Pokémon's physical attributes, habitats, and behaviors to the correct species.\n Put your knowledge to the test and prove yourself as a true Pokémon biologist."
            ),
            Quiz(
                "3",
                "PokeApperance",
                "Sharpen your observational skills!\n Identify Pokémon based on their distinct physical appearances.\n Can you spot the Pokémon that matches the given description or appearance?\n Test your visual acuity and see how many you can correctly identify!"
            )
        )
        quizAdapter.quizzes = listOfQuizzes
        binding.quizRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizAdapter
        }
    }
}