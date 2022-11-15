package com.lrm.hello.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.lrm.hello.activities.MainActivity
import com.lrm.hello.databinding.FragmentSignInBinding

class SignInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

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

        auth = FirebaseAuth.getInstance()

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

                        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
                            if (!it.isSuccessful) {
                                return@OnCompleteListener
                            }

                            // Get new FCM registration token
                            val token = it.result

                            databaseRef = FirebaseDatabase.getInstance().getReference("user").child(auth.currentUser?.uid!!)

                            databaseRef.child("fcmToken").setValue(token)

                        })

                        val intent = Intent(this@SignInFragment.requireContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
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