package com.example.booksreviews.view;

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.booksreviews.R
import com.example.booksreviews.databinding.FragmentLoginBinding
import com.example.booksreviews.model.UserRepository
import com.example.booksreviews.model.UserSharedPreferences
import com.example.booksreviews.viewmodel.UserViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

private const val RC_GOOGLE_SIGN_IN = 9001 // You can use any integer value
class LoginFragment : Fragment() {

    // region Members

    private val DEFAULT_AVATAR_RESOURCE_ID = R.drawable.baseline_person_outline_24
    private lateinit var binding: FragmentLoginBinding
    private var isLoginMode: Boolean = true
    private lateinit var userViewModel: UserViewModel
    private lateinit var googleSignInClient: GoogleSignInClient

    private val emailTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            updateSubmitButtonState()
        }
    }

    private val passwordTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
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

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
        userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]

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
            GoogleSignIn.getSignedInAccountFromIntent(data)
                .addOnCompleteListener(requireActivity()) {
                    task ->
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account.idToken!!)
                    } catch (e: ApiException) {
                        // Google Sign-In failed, handle error
                        val errorMessage = task.exception?.message ?: "Unknown error"
                        binding.errorTextView.text = errorMessage
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

    private fun onAuthCompleted(task: Task<AuthResult>, isLogin: Boolean) {
        binding.progressBar.visibility = View.GONE
        if (task.isSuccessful) {
            // Registration success, user is signed in automatically
            val user = FirebaseAuth.getInstance().currentUser!!
            userViewModel.userId = user.uid
            context?.let { UserSharedPreferences.saveUser(it, user.uid) }

            if (isLogin) {
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                // Extract username from email (before the @ symbol)
                val username = user.email?.substringBefore('@')

                // Set username as display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .setPhotoUri(getDefaultAvatarUri()) // Set default avatar image
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            // Registration and profile update successful
                            userViewModel.addUser(user)
                            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                        } else {
                            // Failed to update profile
                            // Handle the error
                            val errorMessage = profileTask.exception?.message ?: "Unknown error"
                            binding.errorTextView.text = errorMessage
                        }
                    }
            }
        }
        else {
            // Handle the error
            val errorMessage = task.exception?.message ?: "Unknown error"
            binding.errorTextView.text = errorMessage
        }
    }

    private fun submitForm() {
        binding.progressBar.visibility = View.VISIBLE

        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (isLoginMode) {
            // Sign in with email and password
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    onAuthCompleted(task, true)
                }
        } else {
            // Register with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    onAuthCompleted(task, false)
                }
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
                onAuthCompleted(task, false)
            }
    }

    private fun getDefaultAvatarUri(): Uri {
        // Convert drawable resource to Uri
        return Uri.parse("android.resource://${requireContext().packageName}/$DEFAULT_AVATAR_RESOURCE_ID")
    }

    private fun updateSubmitButtonState() {
        // Enable the submit button only if both email and password are valid
        binding.btnSubmit.isEnabled =
            binding.etEmail.text.toString().isNotEmpty() && binding.etPassword.text.toString().isNotEmpty()
    }

    // endregion

}
