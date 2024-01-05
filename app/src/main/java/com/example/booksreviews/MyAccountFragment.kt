package com.example.booksreviews;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.databinding.FragmentMyAccountBinding

class MyAccountFragment : Fragment() {

    private lateinit var binding: FragmentMyAccountBinding

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
}
