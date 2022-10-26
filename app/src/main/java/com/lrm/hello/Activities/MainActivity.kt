package com.lrm.hello.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.lrm.hello.*
import com.lrm.hello.Adapters.NameListAdapter
import com.lrm.hello.Model.UserDetails
import com.lrm.hello.R
import com.lrm.hello.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: ArrayList<UserDetails>
    private lateinit var adapter: NameListAdapter
    private lateinit var databaseRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
            if (!it.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            FirebaseService.token = it.result

        })

        binding.mainMyProfilePic.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
        }

        binding.signOutButton.setOnClickListener {
            showConfirmationDialog()
        }

        userList = ArrayList()
        adapter = NameListAdapter(this, userList)

        recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        auth = Firebase.auth
        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference("user")

        FirebaseMessaging.getInstance().subscribeToTopic("/topics/${user.uid}")

        databaseRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                val currentUser = snapshot.getValue(UserDetails::class.java)

                if (currentUser!!.profilePic == "") {
                    binding.mainMyProfilePic.setImageResource(R.drawable.profile_icon)
                } else {
                    Glide.with(this@MainActivity).load(currentUser.profilePic)
                        .into(binding.mainMyProfilePic)
                }

                for (postSnapshot in snapshot.children) {
                    val users = postSnapshot.getValue(UserDetails::class.java)

                    if (!users!!.uid.equals(user.uid)) {
                        userList.add(users)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.sign_out_text))
            .setMessage(getString(R.string.signout_question))
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                auth.signOut()
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                finish()
                Toast.makeText(this, "Signed Out...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}