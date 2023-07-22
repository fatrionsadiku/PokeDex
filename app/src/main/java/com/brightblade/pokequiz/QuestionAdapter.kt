package com.brightblade.pokequiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brightblade.pokedex.R
import com.brightblade.pokedex.databinding.ItemQuizQuestionBinding
import com.brightblade.pokequiz.quizmodels.Question

class QuestionAdapter(val itemClicker: (quizType: String) -> Unit, val question: Question) :
    RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    private var options: List<String> = listOf(
        question.rightAnswer,
    )

    inner class ViewHolder(val binding: ItemQuizQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    itemView.setBackgroundResource(R.drawable.option_item_selected_bg)
                } else {
                    itemView.setBackgroundResource(R.drawable.option_item_bg)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemQuizQuestionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.quizOption1.text = options[position]

    }

    override fun getItemCount(): Int = options.size
}
