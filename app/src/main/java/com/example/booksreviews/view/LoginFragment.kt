package com.example.booksreviews.view;

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.R
import com.example.booksreviews.databinding.FragmentLoginBinding
import com.example.booksreviews.model.User
import com.example.booksreviews.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

private const val RC_GOOGLE_SIGN_IN = 9001 // You can use any integer value
class LoginFragment : Fragment() {

    // region Members

    private lateinit var binding: FragmentLoginBinding
    private var isLoginMode: Boolean = true
    private lateinit var googleSignInClient: GoogleSignInClient

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.btnToggle.setOnClickListener { toggleForm() }
        binding.btnSubmit.setOnClickListener { submitForm() }
        binding.etEmail.addTextChangedListener(emailTextWatcher)
        binding.etPassword.addTextChangedListener(passwordTextWatcher)
        binding.googleSignInButton.setOnClickListener { signInWithGoogle() }
    }

    override fun onDestroyView() {
        // Remove the TextWatcher to prevent memory leaks
        binding.etEmail.removeTextChangedListener(emailTextWatcher)
        binding.etPassword.removeTextChangedListener(passwordTextWatcher)
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            binding.progressBar.visibility = View.GONE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnCompleteListener(requireActivity()) {
                    task ->
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        // Google Sign-In failed, handle error
                        e.printStackTrace()
                    }
                }
        }
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
        binding.progressBar.visibility = View.VISIBLE

        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (isValidEmail(email) && isValidPassword(password)) {
            val progressBar = binding.progressBar
            progressBar.visibility = View.VISIBLE

            if (isLoginMode) {
                // Sign in with email and password
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            // Sign in success, user is signed in
                            val user = FirebaseAuth.getInstance().currentUser
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                            // Proceed with your app logic
                        } else {
                            // Sign in failed, display a message to the user.
                            // You can get the specific error using task.exception
                        }
                    }
            } else {
                // Register with email and password
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        progressBar.visibility = View.GONE
                        if (task.isSuccessful) {
                            // Registration success, user is signed in automatically
                            val user = FirebaseAuth.getInstance().currentUser
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                            // Proceed with your app logic
                        } else {
                            // Registration failed, display a message to the user.
                            // You can get the specific error using task.exception
                        }
                    }
            }
        } else {
            // Show an error message or handle the invalid input case
        }
    }

    private fun signInWithGoogle() {
        binding.progressBar.visibility = View.VISIBLE
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, user is signed in
                    val user = FirebaseAuth.getInstance().currentUser
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    // Proceed with your app logic
                } else {
                    // Sign in failed, display a message to the user.
                    // You can get the specific error using task.exception
                }
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
