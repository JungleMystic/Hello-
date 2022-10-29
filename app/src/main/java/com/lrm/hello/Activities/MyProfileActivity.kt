package com.lrm.hello.Activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.lrm.hello.Model.UserDetails
import com.lrm.hello.R
import com.lrm.hello.databinding.ActivityMyProfileBinding
import java.text.SimpleDateFormat
import java.util.*

class MyProfileActivity : AppCompatActivity() {

    val lastseenDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    val lastseenTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

    private lateinit var user: FirebaseUser
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var imageUri: Uri
    private lateinit var storage: FirebaseStorage

    private lateinit var binding: ActivityMyProfileBinding


    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it!!

        binding.myProfilePic.setImageURI(imageUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference("user").child(user.uid)

        storage = FirebaseStorage.getInstance()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser = snapshot.getValue(UserDetails::class.java)

                binding.profileName.setText(currentUser!!.name)

                if (currentUser.profilePic == "") {
                    binding.myProfilePic.setImageResource(R.drawable.profile_icon)
                } else {
                    Glide.with(this@MyProfileActivity).load(currentUser.profilePic)
                        .into(binding.myProfilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

        })

        binding.signOutButton.setOnClickListener {
            showConfirmationDialog()
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        binding.myProfilePic.setOnClickListener {
            selectImage.launch("image/*")
            binding.saveButton.visibility = View.VISIBLE
            binding.editNameButton.visibility = View.GONE
            binding.profileName.isEnabled = true
        }

        binding.saveButton.setOnClickListener {
            uploadImage()
            binding.progressBar.visibility = View.VISIBLE
            binding.profileName.isEnabled = false
        }

        binding.editNameButton.setOnClickListener {
            binding.profileName.isEnabled = true
            binding.editNameButton.visibility = View.GONE
            binding.saveNameButton.visibility = View.VISIBLE
        }

        binding.saveNameButton.setOnClickListener {
            binding.profileName.isEnabled = false
            binding.editNameButton.visibility = View.VISIBLE
            binding.saveNameButton.visibility = View.GONE

            val hashMap: HashMap<String, String> = HashMap()
            hashMap.put("name", binding.profileName.text.toString())

            databaseRef.updateChildren(hashMap as Map<String, Any>)

            Toast.makeText(applicationContext, "Profile name updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.sign_out_text))
            .setMessage(getString(R.string.signout_question))
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                auth.signOut()

                val hashMap: HashMap<String, String> = HashMap()
                hashMap.put("fcmToken", "")
                hashMap.put("onlineStatus", "")
                databaseRef.updateChildren(hashMap as Map<String, Any>)

                val intent = Intent(this@MyProfileActivity, SignInActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finish()
                Toast.makeText(this, "Signed Out...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun uploadImage() {

        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(user.uid)
            .child("profile.jpg")

        storageRef.putFile(imageUri!!)
            .addOnCompleteListener{
                storageRef.downloadUrl.addOnSuccessListener {
                    storeData(it)
                    Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.editNameButton.visibility = View.VISIBLE
                    binding.saveButton.visibility = View.GONE

                } .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun storeData(imageUrl: Uri?) {

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("name", binding.profileName.text.toString())
        hashMap.put("profilePic", imageUrl.toString())

        databaseRef.updateChildren(hashMap as Map<String, Any>)

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