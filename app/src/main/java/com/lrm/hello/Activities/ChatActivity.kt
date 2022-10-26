package com.lrm.hello.Activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.gson.Gson
import com.lrm.hello.Adapters.ChatAdapter
import com.lrm.hello.Model.Chat
import com.lrm.hello.Model.NotificationData
import com.lrm.hello.Model.PushNotification
import com.lrm.hello.Model.UserDetails
import com.lrm.hello.R
import com.lrm.hello.RetrofitInstance
import com.lrm.hello.ScrollToBottom
import com.lrm.hello.databinding.ActivityChatBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: ArrayList<Chat>
    private lateinit var databaseRef: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var manager: LinearLayoutManager


    var topic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatList = ArrayList()

        chatRecyclerView = binding.chatRecyclerview
        manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        chatRecyclerView.layoutManager = manager

        user = FirebaseAuth.getInstance().currentUser!!

        databaseRef = FirebaseDatabase.getInstance().getReference()

        val intent = getIntent()
        val userName = intent.getStringExtra("name")
        val userProfilePic = intent.getStringExtra("profilePic")
        val userId = intent.getStringExtra("uid")

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        user = FirebaseAuth.getInstance().currentUser!!
        databaseRef = FirebaseDatabase.getInstance().getReference("user").child(userId!!)

        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserDetails::class.java)

                binding.profileName.text = user!!.name

                if (user.profilePic == "") {
                    binding.chatUserProfilePic.setImageResource(R.drawable.profile_icon)
                } else {
                    Glide.with(this@ChatActivity).load(user.profilePic).into(binding.chatUserProfilePic)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        binding.sendButton.setOnClickListener {
            val message: String = binding.messageBox.text.toString()

            if (message.isNotEmpty()) {
                sendMessage(user.uid, userId, message)
                binding.messageBox.setText("")

                topic = "/topics/$userId"
                PushNotification(NotificationData(userName!!,message), topic).also {
                    sendNotification(it)
                }

            } else {
                Toast.makeText(applicationContext, "Your message is empty.", Toast.LENGTH_SHORT).show()
                binding.messageBox.setText("")
            }
        }

        readMessage(user.uid, userId)
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {

        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)

        reference.child("chat").push().setValue(hashMap)
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val databaseRef2: DatabaseReference = FirebaseDatabase.getInstance().getReference("chat")

        databaseRef2.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                chatList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {

                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(senderId) && chat!!.receiverId.equals(receiverId) ||
                        chat!!.senderId.equals(receiverId) && chat!!.receiverId.equals(senderId)
                    ) {
                        chatList.add(chat)
                    }
                }

                chatAdapter = ChatAdapter(this@ChatActivity, chatList)
                chatRecyclerView.adapter = chatAdapter
                chatAdapter.registerAdapterDataObserver(ScrollToBottom(chatRecyclerView, chatAdapter, manager))
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("TAG", response.errorBody()!!.string())
            }
        } catch (e: Exception) {
            Log.e("TAG", e.toString())
        }
    }
}