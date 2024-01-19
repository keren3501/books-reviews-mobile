package com.example.booksreviews.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.booksreviews.databinding.ItemReviewBinding
import com.example.booksreviews.model.Review

class ReviewsAdapter(
    private val currUserId: Int,
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
                           private val currUserId: Int,
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

            binding.username.text = review.userId.toString()
            binding.bookTitle.text = review.bookTitle
            binding.authorName.text = review.authorName
            binding.reviewText.text = review.reviewText

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

            // Set up logic for loading and displaying images (using Glide, Picasso, etc.)
            // For simplicity, we assume imagePath is a URL or local path
            // binding.ivReviewImage.load(review.imagePath)
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
