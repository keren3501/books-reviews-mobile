package com.example.booksreviews;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        return binding.root
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

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
    }

    private fun navigateToMyAccount() {
        findNavController().navigate(R.id.action_homeFragment_to_myAccountFragment)
    }
}
