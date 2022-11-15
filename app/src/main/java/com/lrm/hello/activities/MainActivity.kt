package com.lrm.hello.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lrm.hello.R
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var user: FirebaseUser
    private lateinit var databaseRef: DatabaseReference
    private lateinit var navController: NavController

    val lastseenDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    val lastseenTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Retrieve NavController from the NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment2) as NavHostFragment

        navController = navHostFragment.navController

        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference("user").child(user.uid)
    }

    override fun onResume() {
        super.onResume()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("onlineStatus", "Online")
        databaseRef.updateChildren(hashMap as Map<String, Any>)
    }

    override fun onPause() {
        super.onPause()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("onlineStatus", "Offline")
        hashMap.put("lastseenDate", lastseenDate)
        hashMap.put("lastseenTime", lastseenTime)

        databaseRef.updateChildren(hashMap as Map<String, Any>)
    }
}