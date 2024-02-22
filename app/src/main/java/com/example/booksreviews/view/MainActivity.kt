package com.example.booksreviews.view;

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.booksreviews.R
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment, R.id.homeFragment, R.id.myAccountFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Inside your Application class or MainActivity's onCreate method
        FirebaseApp.initializeApp(this)
    }
}
