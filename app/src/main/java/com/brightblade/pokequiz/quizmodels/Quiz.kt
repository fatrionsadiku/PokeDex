package com.brightblade.pokequiz.quizmodels

import com.google.firebase.firestore.PropertyName

data class Quiz(
    var id : String = "",
    var title : String = "",
    var quizDescription : String = "",
    var questions : MutableList<Map<String,String>> = mutableListOf()
)

data class Question(
    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",
    @get:PropertyName("option1")
    @set:PropertyName("option1")
    var option1: String = "",
    @get:PropertyName("option2")
    @set:PropertyName("option2")
    var option2: String = "",
    @get:PropertyName("option3")
    @set:PropertyName("option3")
    var option3: String = "",
    @get:PropertyName("right_answer")
    @set:PropertyName("right_answer")
    var rightAnswer : String = ""
) {
    companion object {
        fun from(map: Map<String, String>) = object {
            val title by map
            val option1 by map
            val option2 by map
            val option3 by map
            val right_answer by map

            val data = Question(
                title = title,
                option1 = option1,
                option2 = option2,
                option3 = option3,
                rightAnswer = right_answer
            )
        }.data
    }
}
