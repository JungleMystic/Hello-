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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.lrm.hello.MainActivity
import com.lrm.hello.UserDetails
import com.lrm.hello.databinding.FragmentSignUpBinding

class SignUpFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth



        binding.signUpButton.setOnClickListener {
            performSignUp()
        }
    }

    fun performSignUp() {

        val name = binding.nameET.text.toString()
        val inputEmail = binding.emailET.text.toString()
        val inputPassword = binding.passwordET.text.toString()
        val confirmPassword = binding.confirmPassET.text.toString()

        if(name.isNotEmpty() && inputEmail.isNotEmpty() && inputPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if(inputPassword == confirmPassword) {
                auth.createUserWithEmailAndPassword(inputEmail, inputPassword)
                    .addOnCompleteListener { task ->
                        if(task.isSuccessful) {
                            addUserToDatabase()
                            val intent = Intent(this@SignUpFragment.requireContext(), MainActivity::class.java)
                            startActivity(intent)
                            Toast.makeText(context, "Account has been created", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error occurred ${it.localizedMessage}",Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Password is not matching...", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please fill all the fields...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addUserToDatabase() {
        val name = binding.nameET.text.toString()
        val inputEmail = binding.emailET.text.toString()
        var uid = auth.currentUser?.uid!!

        databaseRef = FirebaseDatabase.getInstance().getReference()

        databaseRef.child("user").child(uid).setValue(UserDetails(name,inputEmail, uid))
    }
}
