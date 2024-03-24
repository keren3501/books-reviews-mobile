package com.example.booksreviews.view;

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.booksreviews.R
import com.example.booksreviews.databinding.FragmentMyAccountBinding
import com.example.booksreviews.model.UserRepository
import com.example.booksreviews.viewmodel.ReviewsViewModel
import com.example.booksreviews.viewmodel.UserViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private const val REQUEST_CODE: Int = 100

class MyAccountFragment : Fragment() {

    private lateinit var binding: FragmentMyAccountBinding
    private lateinit var currImageUri: Uri
    private lateinit var currUsername: Editable
    private lateinit var userViewModel: UserViewModel
    private lateinit var reviewsViewModel: ReviewsViewModel
    private lateinit var currUserId: String
    private var currDisplayName: String? = null
    private var isEditing: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAccountBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.my_account_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate to the HomeFragment when the back button is pressed
                findNavController().popBackStack()
                true
            }
            R.id.action_refresh -> {
                reviewsViewModel.getAllBookReviews()
                true
            }
            // Add more menu items as needed
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModelProvider = ViewModelProvider(requireActivity())
        userViewModel = viewModelProvider[UserViewModel::class.java]
        reviewsViewModel = viewModelProvider[ReviewsViewModel::class.java]

        currUserId = userViewModel.userId!!
        currDisplayName = UserRepository.getUsernameFromUserId(currUserId)

        currUsername = Editable.Factory.getInstance().newEditable(currDisplayName)

        binding.profileImage.isClickable = false

        // Load user data and reviews
        loadUserData()
        loadUserReviews()

        binding.btnEditProfile.setOnClickListener {
            toggleEditMode()
        }

        binding.profileImage.setOnClickListener {
            openGalleryForImage()
        }

        binding.btnCancelProfile.setOnClickListener {
            cancelChanges()
        }

        reviewsViewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    private fun cancelChanges() {
        Glide.with(this)
            .load(File(Environment.getExternalStorageDirectory(), "users/${currUserId}.png"))
            .error(R.drawable.reader_icon)
            .into(binding.profileImage)
        binding.username.text = Editable.Factory.getInstance().newEditable(currDisplayName)
        finishEditing()
    }

    private fun openGalleryForImage() {
        if (isEditing) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            data?.data?.let { uri ->
                // Save the URI of the selected image
                Glide.with(this)
                    .load(uri)
                    .error(R.drawable.reader_icon)
                    .into(binding.profileImage)

                // Now you can save 'uri' for later use
                currImageUri = uri
            }
        }
    }

    private fun finishEditing() {
        isEditing = false
        binding.username.isEnabled = false
        binding.profileImage.isClickable = false
        binding.btnEditProfile.text = "Edit"
        binding.btnCancelProfile.visibility = View.GONE
    }

    private fun toggleEditMode() {
        if (isEditing) {
            // Save changes
            saveChanges()
        } else {
            // Enable editing mode
            enableEditMode()
        }
    }

    private fun enableEditMode() {
        isEditing = true
        binding.username.isEnabled = true
        binding.profileImage.isClickable = true
        binding.btnEditProfile.text = "Save"
        binding.btnCancelProfile.visibility = View.VISIBLE
    }

    private fun saveChanges() {
        // Set username as display name
        UserRepository.updateUserInFirestore(userViewModel.userId!!, binding.username.text.toString(), currImageUri)

        if (::currImageUri.isInitialized) {
            context?.let {
                saveUriToFile(
                    it,
                    currImageUri,
                    "users/" + userViewModel.userId + ".png"
                )
            }
        }

        finishEditing()
    }

    fun saveUriToFile(context: Context, uri: Uri, filename: String): File? {
        try {
            // Create a file object with the desired name
            val file = File(Environment.getExternalStorageDirectory(), filename)

            // Open an input stream from the URI
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Open an output stream to the file
            val outputStream = FileOutputStream(file)

            // Copy content from input stream to output stream
            inputStream.copyTo(outputStream)

            // Close streams
            inputStream.close()
            outputStream.close()

            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun loadUserData() {
        Glide.with(this)
            .load(File(Environment.getExternalStorageDirectory(), "users/${currUserId}.png"))
            .error(R.drawable.reader_icon)
            .into(binding.profileImage)

        // Load user's profile image and username
        binding.username.text = Editable.Factory.getInstance().newEditable(currDisplayName)
    }

    private fun loadUserReviews() {
        // Load user's reviews into RecyclerView
        val reviewsAdapter = ReviewsAdapter(currUserId, null, null, false)
        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRecyclerView.adapter = reviewsAdapter

        // get only my reviews data from ViewModel
        reviewsAdapter.submitList(reviewsViewModel.getReviewsByUser(currUserId))

        reviewsViewModel.reviewsLiveData.observe(viewLifecycleOwner) {
            reviewsAdapter.submitList(reviewsViewModel.getReviewsByUser(currUserId))
        }
    }
}
