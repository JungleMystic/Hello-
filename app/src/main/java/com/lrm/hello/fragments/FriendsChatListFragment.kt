package com.lrm.hello.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.lrm.hello.R
import com.lrm.hello.adapters.FriendsChatListAdapter
import com.lrm.hello.databinding.FragmentFriendsChatListBinding
import com.lrm.hello.model.FriendsList
import com.lrm.hello.model.UserDetails

class FriendsChatListFragment : Fragment() {

    private lateinit var binding: FragmentFriendsChatListBinding
    private lateinit var user: FirebaseUser
    var auth: FirebaseAuth = Firebase.auth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef2: DatabaseReference
    private lateinit var frChatListrecyclerView: RecyclerView
    private lateinit var userList: ArrayList<UserDetails>
    private lateinit var friendsList: ArrayList<FriendsList>
    private lateinit var frChatListadapter: FriendsChatListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendsChatListBinding.inflate(inflater, container, false)

        binding.toolbar.inflateMenu(R.menu.menu)
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.friendRequests) {
                val action = FriendsChatListFragmentDirections.actionFriendsChatListFragmentToFriendRequestsFragment()
                this.findNavController().navigate(action)
            } else if (it.itemId == R.id.signOut) {
                //auth.signOut()
                Toast.makeText(requireContext(),"Signed Out...", Toast.LENGTH_SHORT).show()
            }
            true
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = FirebaseAuth.getInstance().currentUser!!

        binding.mainMyProfilePic.setOnClickListener {
            val action = FriendsChatListFragmentDirections.actionFriendsChatListFragmentToUserProfileFragment()
            this.findNavController().navigate(action)
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("user").child(user.uid)

        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentUser = snapshot.getValue(UserDetails::class.java)

                if (currentUser!!.profilePic == "") {
                    binding.mainMyProfilePic.setImageResource(R.drawable.profile_icon)
                } else {
                    Glide.with(this@FriendsChatListFragment).load(currentUser.profilePic)
                        .into(binding.mainMyProfilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }

        })

        binding.searchFriendsFAB.setOnClickListener{
            val action = FriendsChatListFragmentDirections.actionFriendsChatListFragmentToFindFriendsFragment()
            this.findNavController().navigate(action)
        }

        friendsList = ArrayList()
        userList = ArrayList()
        frChatListadapter = FriendsChatListAdapter(requireContext(), friendsList)

        frChatListrecyclerView = binding.recyclerview
        frChatListrecyclerView.layoutManager = LinearLayoutManager(requireContext())
        frChatListrecyclerView.adapter = frChatListadapter


        frChatListadapter.setOnItemClickListener(object: FriendsChatListAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                val user = friendsList[position]

                val friendUid: String = user.friendUid

                val action = FriendsChatListFragmentDirections.actionFriendsChatListFragmentToChatFragment(friendUid)
                findNavController().navigate(action)
            }
        })

        databaseRef2 = FirebaseDatabase.getInstance().getReference("friends").child(user.uid)
        databaseRef2.addValueEventListener(object: ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {

                friendsList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {

                    val friends = dataSnapshot.getValue(FriendsList::class.java)

                    friendsList.add(friends!!)
                }
                frChatListadapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}