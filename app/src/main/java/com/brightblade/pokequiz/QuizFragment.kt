package com.brightblade.pokequiz

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.FragmentQuestionsBinding
import com.brightblade.pokequiz.quizmodels.Question
import com.google.firebase.firestore.FirebaseFirestore
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding

class QuizFragment : Fragment(R.layout.fragment_questions) {
    private val TAG = "QuizFragment"
    private val binding by viewBinding(FragmentQuestionsBinding::bind)
    private lateinit var fireStore: FirebaseFirestore
    private val quizFragmentArgs by navArgs<QuizFragmentArgs>()
    private lateinit var questionAdapter: QuestionAdapter
    private var questionsList: MutableList<Question> = mutableListOf()
    private var index = 1
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpFireStore(quizFragmentArgs.quizType)
    }

    //    Forced to use Firebase :)
    @Suppress("UNCHECKED_CAST")
    private fun setUpFireStore(quizTitle: String) {
        fireStore = FirebaseFirestore.getInstance()
        val collectionReference = fireStore.collection("quizzes").document("abilities")
        collectionReference.get().addOnSuccessListener { quiz ->
            val listOfQuestionMaps =
                quiz.data?.get("questions") as? List<Map<String, String>> ?: emptyList()
            listOfQuestionMaps.forEach {
                questionsList.add(Question.from(it))
            }
            Log.d(TAG, questionsList.toString())
        }
    }
}