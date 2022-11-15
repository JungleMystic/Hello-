package com.lrm.hello.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.lrm.hello.R
import com.lrm.hello.databinding.FragmentProfileBinding
import com.lrm.hello.model.UserDetails
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    var senderName: String? = null
    var senderProfilePic: String? = null
    var senderId: String? = null
    var receiverId: String? = null
    var requestStatus: String? = null
    var requestId: String? = null

    var currentUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child(currentUser.uid)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToFindFriendsFragment()
            this.findNavController().navigate(action)
        }

        val navArgs: ProfileFragmentArgs by navArgs()

        receiverId = navArgs.profileUid

        val databaseRef2: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child(receiverId!!)
        databaseRef2.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receiver = snapshot.getValue(UserDetails::class.java)

                binding.userAddFriendProfileName.setText(receiver!!.name)

                if(receiver.profilePic == "") {
                    Glide.with(requireContext()).load(R.drawable.profile_icon).into(binding.userAddFriendProfilePic)
                } else {
                    Glide.with(requireContext()).load(receiver.profilePic).into(binding.userAddFriendProfilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser = snapshot.getValue(UserDetails::class.java)

                senderName = currentUser!!.name
                senderProfilePic = currentUser.profilePic
                senderId = currentUser.uid
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })

        binding.addFriendButton.setOnClickListener {

            requestStatus = "Pending"
            requestId = "$senderId+$receiverId"

            val currentDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val currentTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

            val hashMap: HashMap<String, String> = HashMap()
            hashMap.put("senderName", senderName!!)
            hashMap.put("senderProfilePic", senderProfilePic!!)
            hashMap.put("senderId", senderId!!)
            hashMap.put("receiverId", receiverId!!)
            hashMap.put("requestStatus", requestStatus!!)
            hashMap.put("requestId", requestId!!)
            hashMap.put("requestDate", currentDate)
            hashMap.put("requestTime", currentTime)

            reference.child("friend_requests").child(receiverId!!).child(requestId!!).setValue(hashMap).addOnCompleteListener() {
                if (it.isSuccessful) {
                    binding.addFriendButton.visibility = View.GONE

                    Toast.makeText(context, "Friend Request sent...", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}