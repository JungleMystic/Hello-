package com.lrm.hello

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.lrm.hello.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var user: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: ArrayList<UserDetails>
    private lateinit var adapter: NameListAdapter
    private lateinit var databaseRef: DatabaseReference



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signOutButton.setOnClickListener{
            showConfirmationDialog()
        }

        userList = ArrayList()
        adapter = NameListAdapter(this, userList)

        recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        user = Firebase.auth


        databaseRef = FirebaseDatabase.getInstance().getReference()

        databaseRef.child("user").addValueEventListener(object: ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for(postSnapshot in snapshot.children) {
                    val currentUser = postSnapshot.getValue(UserDetails::class.java)

                    if (user.currentUser?.uid != currentUser?.uid) {
                        userList.add(currentUser!!)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.sign_out_text))
            .setMessage(getString(R.string.signout_question))
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                user.signOut()
                startActivity(Intent(this@MainActivity, SignInActivity::class.java))
                finish()
                Toast.makeText(this,"Signed Out...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}