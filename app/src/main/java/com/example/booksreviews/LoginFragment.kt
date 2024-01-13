package com.example.booksreviews;

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    // region Members

    private lateinit var binding: FragmentLoginBinding
    private var isLoginMode: Boolean = true

    private val emailTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            validateEmail(s.toString())
            updateSubmitButtonState()
        }
    }

    private val passwordTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            validatePassword(s.toString())
            updateSubmitButtonState()
        }
    }

    // endregion

    // region Fragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnToggle.setOnClickListener { toggleForm() }
        binding.btnSubmit.setOnClickListener { submitForm() }
        binding.etEmail.addTextChangedListener(emailTextWatcher)
        binding.etPassword.addTextChangedListener(passwordTextWatcher)
    }

    override fun onDestroyView() {
        // Remove the TextWatcher to prevent memory leaks
        binding.etEmail.removeTextChangedListener(emailTextWatcher)
        binding.etPassword.removeTextChangedListener(passwordTextWatcher)
        super.onDestroyView()
    }

    // endregion

    // region Private Methods

    private fun toggleForm() {
        isLoginMode = !isLoginMode

        if (isLoginMode) {
            binding.welcomeMsg.text = "Welcome Back!"
            binding.btnSubmit.text = "Sign In"
            binding.btnToggle.text = "Don't have an account? Sign Up"
        } else {
            binding.welcomeMsg.text = "Create Account :)"
            binding.btnSubmit.text = "Sign Up"
            binding.btnToggle.text = "Already have an account? Sign In"
        }
    }

    private fun submitForm() {
        // כאן תוסיפי את הלוגיקה להתחברות או הרשמה, בהתאם לסטטוס הנוכחי (isLoginMode)
        if (isValidEmail(binding.etEmail.text.toString()) && isValidPassword(binding.etPassword.text.toString())) {
            // Perform login logic here
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        } else {
            // Show an error message or handle the invalid input case
        }
    }

    private fun validateEmail(email: String) {
        if (!isValidEmail(email)) {
            binding.emailLayout.error = "Invalid email address"
        } else {
            binding.emailLayout.error = null
        }
    }

    private fun validatePassword(password: String) {
        if (!isValidPassword(password)) {
            binding.passwordLayout.error = "Password must be at least 8 chars"
        } else {
            binding.passwordLayout.error = null
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && !email.contains(" ")
    }

    private fun isValidPassword(password: String): Boolean {
        // Add your password validation logic here (e.g., minimum length, specific characters)
        return password.length >= 8
    }

    private fun updateSubmitButtonState() {
        // Enable the submit button only if both email and password are valid
        binding.btnSubmit.isEnabled =
            isValidEmail(binding.etEmail.text.toString()) && isValidPassword(binding.etPassword.text.toString())
    }

    // endregion

}
