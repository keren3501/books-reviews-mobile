package com.example.booksreviews;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener { navigateToLogin() }
        binding.btnHome.setOnClickListener { navigateToHome() }
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
    }
}
