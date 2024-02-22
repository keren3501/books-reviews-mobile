package com.example.booksreviews.view;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksreviews.databinding.FragmentMyAccountBinding
import com.example.booksreviews.viewmodel.ReviewsViewModel
import com.example.booksreviews.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseUser

class MyAccountFragment : Fragment() {

    private lateinit var binding: FragmentMyAccountBinding
    private lateinit var userViewModel: UserViewModel
    private lateinit var reviewsViewModel: ReviewsViewModel
    private lateinit var currUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyAccountBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                // Navigate to the HomeFragment when the back button is pressed
                findNavController().popBackStack()
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

        currUser = userViewModel.user

        // Load user data and reviews
        loadUserData()
        loadUserReviews()

        binding.btnEditProfile.setOnClickListener {
            // Toggle between TextView and EditText for editing profile and name
            // Handle the logic accordingly
        }
    }

    private fun loadUserData() {
        // Load user's profile image and username
        binding.profileImage.setImageURI(currUser.photoUrl)
        binding.username.text = currUser.displayName
    }

    private fun loadUserReviews() {
        // Load user's reviews into RecyclerView
        val reviewsAdapter = ReviewsAdapter(userViewModel.user.uid, null, null, false)
        binding.reviewsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.reviewsRecyclerView.adapter = reviewsAdapter

        // Observe reviews data from ViewModel
        reviewsAdapter.submitList(reviewsViewModel.getReviewsByUser(currUser.uid))
    }
}
