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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.lrm.hello.adapters.FindFriendsAdapter
import com.lrm.hello.databinding.FragmentFindFriendsBinding
import com.lrm.hello.model.UserDetails

class FindFriendsFragment : Fragment() {

    private lateinit var binding: FragmentFindFriendsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var recyclerView: RecyclerView
    private lateinit var userList: ArrayList<UserDetails>
    private lateinit var adapter: FindFriendsAdapter
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFindFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userList = ArrayList()
        adapter = FindFriendsAdapter(requireContext(), userList)

        recyclerView = binding.recyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener(object: FindFriendsAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {

                val user = userList[position]

                val receiverUid: String = user.uid

                val action = FindFriendsFragmentDirections.actionFindFriendsFragmentToProfileFragment(receiverUid)
                findNavController().navigate(action)
            }
        })

        auth = Firebase.auth
        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference("user")

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
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}