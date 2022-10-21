package com.lrm.hello.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lrm.hello.MainActivity
import com.lrm.hello.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    lateinit var auth: FirebaseAuth

    private var _binding: FragmentSignInBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignInBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth

        binding.signInButton.setOnClickListener {
            performSignIn()
        }
    }

    fun performSignIn() {
        val inputEmail = binding.emailET.text.toString()
        val inputPassword = binding.passwordET.text.toString()

        if(inputEmail.isNotEmpty() && inputPassword.isNotEmpty()) {
            auth.signInWithEmailAndPassword(inputEmail, inputPassword)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        val intent = Intent(this@SignInFragment.requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(context, "Signed in Successfully...", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Authentication failed...${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Please fill all the fields...", Toast.LENGTH_SHORT).show()
        }
    }
}