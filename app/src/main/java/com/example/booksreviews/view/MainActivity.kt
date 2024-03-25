package com.example.booksreviews.view;

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.booksreviews.model.ImageCacheDatabase
import com.example.booksreviews.R
import com.example.booksreviews.model.ImageRepository
import com.example.booksreviews.model.UserSharedPreferences
import com.example.booksreviews.viewmodel.UserViewModel
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val REQUEST_MANAGE_ALL_FILES_ACCESS_PERMISSION: Int = 23

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ImageRepository.imageDao = ImageCacheDatabase.getDatabase(this).imageDao()
        CoroutineScope(Dispatchers.IO).launch {
            ImageRepository.getAllCachedImages()
        }

        checkManageAllFilesAccessPermission()
    }

    private fun checkManageAllFilesAccessPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            requestManageAllFilesAccessPermission()
        } else {
            setupNavigation()
        }
    }

    private fun requestManageAllFilesAccessPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        startActivityForResult(intent, REQUEST_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_MANAGE_ALL_FILES_ACCESS_PERMISSION) {
            // Check if the user granted the permission
            if (Environment.isExternalStorageManager()) {
                // The user granted the permission
                // Proceed with your action
                setupNavigation()
            } else {
                // The user denied the permission
                // Handle this case accordingly (e.g., show a message or disable functionality)
                requestManageAllFilesAccessPermission()
            }
        }
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.loginFragment, R.id.homeFragment, R.id.myAccountFragment))

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Create the directory if it doesn't exist
        val usersDir = File(Environment.getExternalStorageDirectory(), "users")
        if (!usersDir.exists()) {
            usersDir.mkdirs() // Creates all necessary parent directories
        }

        val coversDir = File(Environment.getExternalStorageDirectory(), "covers")
        if (!coversDir.exists()) {
            coversDir.mkdirs() // Creates all necessary parent directories
        }

        // Retrieve user data when the app starts
        val userId = UserSharedPreferences.getUser(this)
        if (userId != null) {
            val viewModelProvider = ViewModelProvider(this)
            val userViewModel = viewModelProvider[UserViewModel::class.java]
            userViewModel.userId = userId

            userViewModel.fetchUserData(userId)

            // User data exists, automatically log in the user
            // You can navigate to the home screen or perform any other necessary actions here
            navController.navigate(R.id.action_loginFragment_to_homeFragment)
        } else {
            // User data doesn't exist, the user needs to log in
        }
    }

}
