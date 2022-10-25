package com.lrm.hello.Activities

import android.os.Bundle
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
import com.lrm.hello.Model.PushNotificationData
import com.lrm.hello.Model.UserDetails
import com.lrm.hello.R
import com.lrm.hello.RetrofitInstance
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

    var topic = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatList = ArrayList()

        chatRecyclerView = binding.chatRecyclerview
        chatRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

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
            var message: String = binding.messageBox.text.toString()

            if (message.isNotEmpty()) {
                sendMessage(user.uid, userId, message)
                binding.messageBox.setText("")

                topic = "/topics/$userId"
                PushNotificationData(NotificationData(userName!!,message), topic).also {
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

        var reference: DatabaseReference? = FirebaseDatabase.getInstance().getReference()

        var hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)

        reference!!.child("chat").push().setValue(hashMap)
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
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun sendNotification(notification: PushNotificationData) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if (response.isSuccessful) {
                Toast.makeText(this@ChatActivity, "Response: ${Gson().toJson(response)}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@ChatActivity, response.errorBody().toString(), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this@ChatActivity, e.message, Toast.LENGTH_SHORT).show()
        }
    }
}