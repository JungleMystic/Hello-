package com.lrm.hello.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.lrm.hello.adapters.FriendRequestsAdapater
import com.lrm.hello.databinding.FragmentFriendRequestsBinding
import com.lrm.hello.model.FriendRequests

class FriendRequestsFragment : Fragment() {

    private lateinit var binding: FragmentFriendRequestsBinding
    private lateinit var frRecyclerView: RecyclerView
    private lateinit var frAdapter: FriendRequestsAdapater
    private lateinit var frList: ArrayList<FriendRequests>
    private lateinit var databaseRef: DatabaseReference
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendRequestsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        frList = ArrayList()
        frAdapter = FriendRequestsAdapater(requireContext(), frList)

        frRecyclerView = binding.recyclerview
        frRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        frRecyclerView.adapter = frAdapter

        frAdapter.setOnItemClickListener(object: FriendRequestsAdapater.onItemClickListener {
            override fun onItemClick(position: Int) {
                val user = frList[position]

                val senderUid: String = user.senderId

                val action = FriendRequestsFragmentDirections.actionFriendRequestsFragmentToFriendRequestProfileFragment(senderUid)
                findNavController().navigate(action)
            }

        })

        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference("friend_requests").child(user.uid)

        databaseRef.addValueEventListener(object: ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                frList.clear()

                for (postSnapshot in snapshot.children) {

                    val friendRequestList = postSnapshot.getValue(FriendRequests::class.java)

                    frList.add(friendRequestList!!)
                }
                frAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}