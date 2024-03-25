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
import com.example.booksreviews.viewmodel.UserViewModel
import java.io.File
import java.net.URL

class ReviewsAdapter(
    private val currUserId: String,
    private val onDeleteClickListener: ((Int) -> Unit)?,
    private val onEditClickListener: ((Int) -> Unit)?,
    private val inFeed: Boolean,
    private val userViewModel: UserViewModel
) : ListAdapter<Review, ReviewsAdapter.ReviewViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReviewBinding.inflate(inflater, parent, false)
        return ReviewViewHolder(binding, currUserId, onDeleteClickListener, onEditClickListener, inFeed, userViewModel)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ReviewViewHolder(private val binding: ItemReviewBinding,
                           private val currUserId: String,
                           private val onDeleteClickListener: ((Int) -> Unit)?,
                           private val onEditClickListener: ((Int) -> Unit)?,
                           private val inFeed: Boolean,
                           private val userViewModel: UserViewModel
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            // Bind data to views
            if (!inFeed) {
                binding.top.visibility = View.GONE
            }

            // Launch a coroutine to call the suspend function
            val userData = userViewModel.getCachedUserById(review.userId)

            var usernameStr = review.userId

            if (userData != null) {
                if (userData.username != null) {
                    usernameStr = userData.username
                }

                Glide.with(binding.root.context)
                    .load(File(Environment.getExternalStorageDirectory(), "users/${userData.id}.png"))
                    .error(R.drawable.reader_icon)
                    .override(40, 40)
                    .into(binding.userPhotoImageView)
            }

            binding.username.text = usernameStr
            binding.bookTitle.text = review.bookTitle
            binding.authorName.text = review.authorName
            binding.reviewText.text = review.reviewText

            Glide.with(binding.root.context)
                .load(Environment.getExternalStorageDirectory().absolutePath + "/covers/${review.bookTitle}_${review.authorName}.png")
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
