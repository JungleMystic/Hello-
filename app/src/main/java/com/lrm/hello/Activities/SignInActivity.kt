package com.lrm.hello.Activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lrm.hello.Fragments.SignInFragmentDirections
import com.lrm.hello.Fragments.SignUpFragmentDirections
import com.lrm.hello.R
import com.lrm.hello.databinding.ActivitySigninBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySigninBinding
    private lateinit var navController: NavController

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.signUpHere.setOnClickListener {
            val action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
            navController.navigate(action)

            binding.signUpLL.visibility = View.GONE
            binding.signInLL.visibility = View.VISIBLE
        }

        binding.signInHere.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment()
            navController.navigate(action)

            binding.signUpLL.visibility = View.VISIBLE
            binding.signInLL.visibility = View.GONE
        }

        // Retrieve NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment

        navController = navHostFragment.navController

    }

    override fun onStart() {
        super.onStart()

        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}