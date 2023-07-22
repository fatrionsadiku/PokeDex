package com.brightblade.pokequiz

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.decode.SvgDecoder
import coil.load
import com.brightblade.pokedex.R
import com.brightblade.pokequiz.quizmodels.Quiz
import com.brightblade.pokedex.databinding.ItemQuizTypeBinding
import com.brightblade.utils.Utility.listOfColors
import com.brightblade.utils.Utility.listOfIcons
import com.skydoves.rainbow.Rainbow
import com.skydoves.rainbow.RainbowOrientation
import com.skydoves.rainbow.color

class QuizAdapter(val itemClicker : (quizType : String) -> Unit) : RecyclerView.Adapter<QuizAdapter.ViewHolder>() {

    var quizzes: List<Quiz>
        get() = differ.currentList
        set(value) {
            differ.submitList(value)
        }
    private val differ = AsyncListDiffer(this, diffCallback)
    inner class ViewHolder(val binding : ItemQuizTypeBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(quiz: Quiz){
            binding.apply {
                quizTitle.text = quiz.title
                quizIcon.load(listOfIcons[adapterPosition]){
                    decoderFactory { result, options, imageLoader ->
                        SvgDecoder(result.source,options)
                    }
                }
                quizDescription.text = quiz.quizDescription
                Rainbow(quizContainer).palette {
                    +color(Color.parseColor(listOfColors.random()))
                    +color(binding.root.context.resources.getColor(R.color.white))
                }.apply {
                    background(RainbowOrientation.TOP_BOTTOM, 14)
                }
            }
        }
        init {
            binding.root.setOnClickListener {
                val currentPosition = adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    val selectedQuizName = quizzes[currentPosition].title
                    itemClicker(selectedQuizName.toLowerCase())
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizTypeBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(quizzes[position])
    }

    override fun getItemCount(): Int = quizzes.size
}

private val diffCallback = object : DiffUtil.ItemCallback<Quiz>() {
    override fun areItemsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Quiz, newItem: Quiz): Boolean {
        return oldItem.id == newItem.id
    }

}