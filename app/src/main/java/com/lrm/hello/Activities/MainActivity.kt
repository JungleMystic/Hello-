package com.lrm.hello.Activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
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
    private lateinit var databaseRef2: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.mainMyProfilePic.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyProfileActivity::class.java))
        }

        userList = ArrayList()
        adapter = NameListAdapter(this, userList)

        recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        auth = Firebase.auth
        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference("user")
        databaseRef2 = FirebaseDatabase.getInstance().getReference("user").child(user.uid)

        databaseRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

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

        databaseRef2.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser = snapshot.getValue(UserDetails::class.java)

                if (currentUser!!.profilePic == "") {
                    binding.mainMyProfilePic.setImageResource(R.drawable.profile_icon)
                } else {
                    Glide.with(this@MainActivity).load(currentUser.profilePic)
                        .into(binding.mainMyProfilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

        })
    }
}