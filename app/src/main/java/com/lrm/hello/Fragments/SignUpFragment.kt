package com.lrm.hello.Fragments

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
import com.lrm.hello.Activities.MainActivity
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

        auth = FirebaseAuth.getInstance()

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
                            addUserToDatabase(name, inputEmail)
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

    private fun addUserToDatabase(name: String, inputEmail: String) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
            if (!it.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = it.result

            val hashMap: HashMap<String, String> = HashMap()
            hashMap.put("name", name)
            hashMap.put("inputEmail", inputEmail)
            hashMap.put("uid", auth.currentUser?.uid!!)
            hashMap.put("profilePic", "")
            hashMap.put("fcmToken", token!!)
            hashMap.put("onlineStatus", token!!)
            hashMap.put("lastseenDate", "")
            hashMap.put("lastseenTime", "")
            hashMap.put("typingStatus", "")

            databaseRef = FirebaseDatabase.getInstance().getReference("user").child(auth.currentUser?.uid!!)

            databaseRef.setValue(hashMap).addOnCompleteListener() {
                if (it.isSuccessful) {
                    val intent = Intent(this@SignUpFragment.requireContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    Toast.makeText(context, "Account has been created", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
