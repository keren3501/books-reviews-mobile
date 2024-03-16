package com.example.booksreviews.view

import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booksreviews.R
import com.example.booksreviews.databinding.ItemReviewBinding
import com.example.booksreviews.model.Review
import com.example.booksreviews.model.UserRepository

class ReviewsAdapter(
    private val currUserId: String,
    private val onDeleteClickListener: ((Int) -> Unit)?,
    private val onEditClickListener: ((Int) -> Unit)?,
    private val inFeed: Boolean
) : ListAdapter<Review, ReviewsAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReviewBinding.inflate(inflater, parent, false)
        return ReviewViewHolder(binding, currUserId, onDeleteClickListener, onEditClickListener, inFeed)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(private val binding: ItemReviewBinding,
                           private val currUserId: String,
                           private val onDeleteClickListener: ((Int) -> Unit)?,
                           private val onEditClickListener: ((Int) -> Unit)?,
                           private val inFeed: Boolean
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            // Bind data to views
            if (!inFeed) {
                binding.top.visibility = View.GONE
            }

            // Launch a coroutine to call the suspend function
            val username = UserRepository.getUsernameFromUserId(review.userId)
            if (username != null) {
                binding.username.text = username
                // Use the username as needed
                println("Username: $username")
            } else {
                binding.username.text = review.userId
                // Handle the case where no username was found
                println("Username not found")
            }

            binding.bookTitle.text = review.bookTitle
            binding.authorName.text = review.authorName
            binding.reviewText.text = review.reviewText

            Glide.with(binding.root.context)
                .load(Environment.getExternalStorageDirectory().absolutePath + "/${review.bookTitle}_${review.authorName}.png")
                .error(R.drawable.no_cover)
                .override(100, 158)
                .into(binding.coverImage)

            if (onDeleteClickListener != null && currUserId == review.userId) {
                binding.iconDelete.visibility = View.VISIBLE
                // Set up click listener for delete icon
                binding.iconDelete.setOnClickListener {
                    onDeleteClickListener.invoke(adapterPosition)
                }
            }

            if (onEditClickListener != null && currUserId == review.userId) {
                binding.iconEdit.visibility = View.VISIBLE
                // Set up click listener for edit icon
                binding.iconEdit.setOnClickListener {
                    onEditClickListener.invoke(adapterPosition)
                }
            }
        }
    }

    private class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
            return oldItem == newItem
        }
    }
}
