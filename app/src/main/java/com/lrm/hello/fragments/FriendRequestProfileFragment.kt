package com.lrm.hello.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.lrm.hello.R
import com.lrm.hello.databinding.FragmentFriendRequestProfileBinding
import com.lrm.hello.model.UserDetails
import java.util.*

class FriendRequestProfileFragment : Fragment() {

    private lateinit var binding: FragmentFriendRequestProfileBinding

    var senderName: String? = null
    var senderProfilePic: String? = null
    var senderId: String? = null
    var receiverId: String? = null
    var receiverName: String? = null
    var receiverProfilePic: String? = null
    var requestId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendRequestProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backButton.setOnClickListener {
            val action = FriendRequestProfileFragmentDirections.actionFriendRequestProfileFragmentToFriendRequestsFragment()
            this.findNavController().navigate(action)
        }

        val navArgs: FriendRequestProfileFragmentArgs by navArgs()

        senderId = navArgs.profileUid

        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child(senderId!!)

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sender = snapshot.getValue(UserDetails::class.java)

                if(sender!!.profilePic == "") {
                    Glide.with(requireContext()).load(R.drawable.profile_icon).into(binding.frProfileProfilePic)
                } else {
                    Glide.with(requireContext()).load(sender.profilePic).into(binding.frProfileProfilePic)
                }

                senderProfilePic = sender.profilePic


                binding.frProfileProfileName.setText(sender.name)
                senderName = sender.name
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        val currentUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
        receiverId = currentUser.uid

        val reference7: DatabaseReference = FirebaseDatabase.getInstance().getReference("user").child(receiverId!!)
        reference7.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receiver = snapshot.getValue(UserDetails::class.java)

                receiverName = receiver!!.name
                receiverProfilePic = receiver.profilePic
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })


        requestId = "$senderId+$receiverId"

        binding.rejectButton.setOnClickListener {

            val reference2: DatabaseReference = FirebaseDatabase.getInstance().getReference("friend_requests").child(receiverId!!).child(requestId!!)

                val hashMap: HashMap<String, String> = HashMap()
                hashMap.put("requestStatus", "Rejected")

                reference2.updateChildren(hashMap as Map<String, Any>)

            binding.apply {
                rejectButton.visibility = View.GONE
                acceptButton.visibility = View.GONE
            }
        }

        binding.acceptButton.setOnClickListener {

            val reference3: DatabaseReference = FirebaseDatabase.getInstance().getReference("friend_requests").child(receiverId!!).child(requestId!!)

            val hashMap: HashMap<String, String> = HashMap()
            hashMap.put("requestStatus", "Accepted")

            reference3.updateChildren(hashMap as Map<String, Any>)

            val friendId = UUID.randomUUID().toString()

            val reference4: DatabaseReference = FirebaseDatabase.getInstance().getReference()

            val hashMap2: HashMap<String, String> = HashMap()
            hashMap2.put("friendUid", receiverId!!)
            hashMap2.put("friendName", receiverName!!)
            hashMap2.put("friendProfilePic", receiverProfilePic!!)

            reference4.child("friends").child(senderId!!).child(friendId).setValue(hashMap2)

            val reference5: DatabaseReference = FirebaseDatabase.getInstance().getReference()

            val hashMap3: HashMap<String, String> = HashMap()
            hashMap3.put("friendUid", senderId!!)
            hashMap3.put("friendName", senderName!!)
            hashMap3.put("friendProfilePic", senderProfilePic!!)
            reference5.child("friends").child(receiverId!!).child(friendId).setValue(hashMap3)

            binding.apply {
                rejectButton.visibility = View.GONE
                acceptButton.visibility = View.GONE
            }
        }
    }
}