package com.lrm.hello.Activities

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lrm.hello.Model.UserDetails
import com.lrm.hello.R
import com.lrm.hello.databinding.ActivityMyProfileBinding
import java.io.IOException
import java.util.*

class MyProfileActivity : AppCompatActivity() {

    private lateinit var user: FirebaseUser
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var filePath: Uri
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private lateinit var binding: ActivityMyProfileBinding

    private val PICK_IMAGE_REQUEST: Int = 2020

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference("user").child(user.uid)

        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

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
            chooseImage()
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
                startActivity(Intent(this@MyProfileActivity, SignInActivity::class.java))
                finish()
                Toast.makeText(this, "Signed Out...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun chooseImage() {
        val intent: Intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && requestCode != null) {
            filePath = data?.data!!

            try {
                var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                binding.myProfilePic.setImageBitmap(bitmap)
                binding.saveButton.visibility = View.VISIBLE
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage() {

        if (filePath != null) {

            var ref: StorageReference = storageRef.child("image/" + UUID.randomUUID().toString())
            ref.putFile(filePath)
                .addOnSuccessListener {

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap.put("name", binding.profileName.text.toString())
                    hashMap.put("profilePic", filePath.toString())

                    databaseRef.updateChildren(hashMap as Map<String, Any>)

                    Toast.makeText(applicationContext, "Uploaded", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.editNameButton.visibility = View.VISIBLE
                    binding.saveButton.visibility = View.GONE
                }
                .addOnFailureListener {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        applicationContext,
                        "Failed to upload. Please try again...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}