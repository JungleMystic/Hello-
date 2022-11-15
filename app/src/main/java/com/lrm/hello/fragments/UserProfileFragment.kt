package com.lrm.hello.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.lrm.hello.R
import com.lrm.hello.activities.SignInActivity
import com.lrm.hello.databinding.FragmentUserProfileBinding
import com.lrm.hello.model.UserDetails

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var user: FirebaseUser
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference

    private lateinit var imageUri: Uri
    private lateinit var storage: FirebaseStorage

    private val selectImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it!!

        binding.myProfilePic.setImageURI(imageUri)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                    Glide.with(context!!).load(currentUser.profilePic)
                        .into(binding.myProfilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        binding.signOutButton.setOnClickListener {
            showConfirmationDialog()
        }

        binding.backButton.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToFriendsChatListFragment()
            this.findNavController().navigate(action)
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

            Toast.makeText(context, "Profile name updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.sign_out_text))
            .setMessage(getString(R.string.signout_question))
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->

                val hashMap: HashMap<String, String> = HashMap()
                hashMap.put("fcmToken", "")
                hashMap.put("onlineStatus", "")
                databaseRef.updateChildren(hashMap as Map<String, Any>)

                auth.signOut()

                val intent = Intent(this.context, SignInActivity::class.java)
                startActivity(intent)
                Toast.makeText(context, "Signed Out...", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun uploadImage() {

        val storageRef = FirebaseStorage.getInstance().getReference("profile")
            .child(user.uid)
            .child("profile.jpg")

        storageRef.putFile(imageUri)
            .addOnCompleteListener{
                storageRef.downloadUrl.addOnSuccessListener {
                    storeData(it)
                    Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                    binding.editNameButton.visibility = View.VISIBLE
                    binding.saveButton.visibility = View.GONE

                } .addOnFailureListener {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun storeData(imageUrl: Uri?) {

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("name", binding.profileName.text.toString())
        hashMap.put("profilePic", imageUrl.toString())

        databaseRef.updateChildren(hashMap as Map<String, Any>)

    }
}