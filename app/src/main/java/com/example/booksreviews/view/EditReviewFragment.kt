package com.example.booksreviews.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.booksreviews.R
import com.example.booksreviews.databinding.FragmentEditReviewBinding
import com.example.booksreviews.model.Review
import com.example.booksreviews.model.ReviewsRepository
import com.example.booksreviews.viewmodel.ReviewsViewModel
import com.example.booksreviews.viewmodel.UserViewModel
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

private const val PICK_IMAGE_REQUEST: Int = 4

class EditReviewFragment : Fragment() {

    private lateinit var binding: FragmentEditReviewBinding
    private lateinit var reviewsViewModel: ReviewsViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var newReview: Review
    private var coverUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditReviewBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelProvider = ViewModelProvider(requireActivity())
        reviewsViewModel = viewModelProvider[ReviewsViewModel::class.java]
        userViewModel = viewModelProvider[UserViewModel::class.java]

        newReview = Review()

        binding.coverImage.setOnClickListener { openGallery() }

        // if its edit mode
        if (reviewsViewModel.currEditedReviewIndex != -1) {
            newReview = Review(reviewsViewModel.reviewsLiveData.value?.get(reviewsViewModel.currEditedReviewIndex)!!)

            binding.etReviewText.setText(newReview.reviewText)
            binding.etBookTitle.setText(newReview.bookTitle)
            binding.etAuthorName.setText(newReview.authorName)
            Glide.with(this)
                .load(newReview.bookCoverUrl)
                .override(100, 150)
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.coverImage)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_post_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate to the HomeFragment when the back button is pressed
                findNavController().popBackStack()
                true
            }
            R.id.action_save -> {
                val reviewText = binding.etReviewText.text.toString()
                val bookTitle = binding.etBookTitle.text.toString()
                val authorName = binding.etAuthorName.text.toString()
                // Check if both text and photo are provided
                if (reviewText.isNotEmpty() && bookTitle.isNotEmpty() && authorName.isNotEmpty()) {
                    newReview.reviewText = reviewText
                    newReview.bookTitle = bookTitle
                    newReview.authorName = authorName

                    if (reviewsViewModel.currEditedReviewIndex !=-1) {
                        editReview()
                    }
                    else {
                        newReview.userId = userViewModel.user.uid
                        uploadReview()
                    }
                } else {
                    // Show an error message or toast indicating that both text and photo are required
                }

                true
            }
            // Add more menu items as needed
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editReview() {
        // Upload cover image to Firebase Storage
        if (coverUri != null) {
        val storageRef = FirebaseStorage.getInstance().reference.child("covers/${coverUri!!}")
        val uploadTask = storageRef.putFile(coverUri!!)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // Image uploaded successfully, get the download URL
            val downloadUrl = taskSnapshot.storage.downloadUrl.toString()
            newReview.bookCoverUrl = downloadUrl

            // Save book review details along with the cover image URL in Firebase Realtime Database
            ReviewsRepository.editBookReview(newReview.id, newReview)
            reviewsViewModel.finishEditing()
            findNavController().popBackStack()
        }.addOnFailureListener { exception ->
            // Handle any errors during upload
            Log.e("Edit Review", "Error uploading cover image: $exception")
        }
        }
        else {
            ReviewsRepository.editBookReview(newReview.id, newReview)
            reviewsViewModel.finishEditing()
            findNavController().popBackStack()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            // Handle the selected image as needed
            // For example, update the profile image in your ViewModel
            if (selectedImage != null) {
                coverUri = selectedImage

                Glide.with(this)
                    .load(selectedImage)
                    .error(R.drawable.ic_launcher_foreground)
                    .override(100, 150)
                    .into(binding.coverImage)
            }
        }
    }

    private fun uploadReview() {
        newReview.timestamp = System.currentTimeMillis()

        if (coverUri != null) {
            val imageId = UUID.randomUUID()
            // Upload cover image to Firebase Storage
            val storageRef =
                FirebaseStorage.getInstance().reference.child("covers/${imageId}")
            val uploadTask = storageRef.putFile(coverUri!!)

            uploadTask.addOnSuccessListener { taskSnapshot ->
                // Image uploaded successfully, get the download URL
                val downloadUrl = taskSnapshot.task.result.uploadSessionUri.toString()
                newReview.bookCoverUrl = downloadUrl

                // Save book review details along with the cover image URL in Firebase Realtime Database
                ReviewsRepository.addBookReview(newReview)
                findNavController().popBackStack()
            }.addOnFailureListener { exception ->
                // Handle any errors during upload
                Log.e("Edit Review", "Error uploading cover image: $exception")
            }
        }
        else {
            ReviewsRepository.addBookReview(newReview)
            findNavController().popBackStack()
        }
    }
}
