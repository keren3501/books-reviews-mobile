package com.example.booksreviews.view;

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booksreviews.R
import com.example.booksreviews.databinding.FragmentHomeBinding
import com.example.booksreviews.viewmodel.ReviewsViewModel
import com.example.booksreviews.viewmodel.UserViewModel

class HomeFragment : Fragment() {

    // region Members

    private lateinit var binding: FragmentHomeBinding
    private lateinit var reviewsViewModel: ReviewsViewModel
    private lateinit var adapter: ReviewsAdapter

    // endregion

    // region LifeCycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        val viewModelProvider = ViewModelProvider(requireActivity())
        reviewsViewModel = viewModelProvider[ReviewsViewModel::class.java]

        // Set up RecyclerView and adapter
        adapter = ReviewsAdapter(
            viewModelProvider[UserViewModel::class.java].user.id,
            { review -> onDeleteReviewClicked(review) },
            { review -> onEditReviewClicked(review) },
            true)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observe changes in the reviews list
        reviewsViewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            // Update the adapter with the new list of reviews
            adapter.submitList(reviews)
            adapter.notifyDataSetChanged()

            showNoReviewsMessage(reviews.isEmpty())
        }

        binding.fabAddReview.setOnClickListener { navigateToEditReviewFragment() }

        adapter.submitList(reviewsViewModel.reviews.value)
        showNoReviewsMessage(reviewsViewModel.reviews.value!!.isEmpty())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                navigateToLogin()
                return true
            }
            R.id.action_my_account -> {
                navigateToMyAccount()
                return true
            }
            // Add more menu items as needed
            else -> return super.onOptionsItemSelected(item)
        }
    }

    // endregion

    // region Private Methods

    private fun onEditReviewClicked(reviewIndex: Int) {
        reviewsViewModel.currEditedReviewIndex = reviewIndex

        navigateToEditReviewFragment()
    }

    private fun onDeleteReviewClicked(reviewIndex: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete this review?")
            .setPositiveButton("Yes") { _, _ ->
                reviewsViewModel.deleteReviewAtIndex(reviewIndex)
            }
            .setNegativeButton("No") { _, _ ->
                // Do nothing, user canceled the delete operation
            }
            .show()
    }

    private fun showNoReviewsMessage(show: Boolean) {
        if (show) {
            binding.recyclerView.visibility = View.GONE
            binding.noReviewsMessage.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.noReviewsMessage.visibility = View.GONE
        }
    }

    // region Navigation

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    private fun navigateToMyAccount() {
        findNavController().navigate(R.id.action_homeFragment_to_myAccountFragment)
    }

    private fun navigateToEditReviewFragment() {
        findNavController().navigate(R.id.action_homeFragment_to_editReviewFragment)
    }

    // endregion

    // endregion

}
