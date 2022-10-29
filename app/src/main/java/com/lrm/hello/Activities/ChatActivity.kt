package com.lrm.hello.Activities

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.lrm.hello.Adapters.ChatAdapter
import com.lrm.hello.ApiUitlities
import com.lrm.hello.Model.Chat
import com.lrm.hello.Model.NotificationData
import com.lrm.hello.Model.PushNotification
import com.lrm.hello.Model.UserDetails
import com.lrm.hello.R
import com.lrm.hello.ScrollToBottom
import com.lrm.hello.databinding.ActivityChatBinding
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {

    val lastseenDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    val lastseenTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: ArrayList<Chat>
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef2: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var manager: LinearLayoutManager
    private lateinit var storage: FirebaseStorage
    private lateinit var imageUri: Uri

    /*
    private val selectedImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        imageUri = it!!
    }
    */

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

        storage = FirebaseStorage.getInstance()

        val intent = getIntent()
        //val userName = intent.getStringExtra("name")
        //val userProfilePic = intent.getStringExtra("profilePic")
        val userId = intent.getStringExtra("uid")

        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("user").child(userId!!)
        databaseRef2 = FirebaseDatabase.getInstance().getReference("user").child(user.uid)

        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserDetails::class.java)

                binding.chatProfileName.text = user!!.name

                if (user.profilePic == "") {
                    binding.chatUserProfilePic.setImageResource(R.drawable.profile_icon)
                } else {
                    Glide.with(this@ChatActivity).load(user.profilePic).into(binding.chatUserProfilePic)
                }

                if (user.onlineStatus == "Online") {
                    binding.onlineStatus.text = user.onlineStatus
                } else if (user.onlineStatus == "Offline") {
                    binding.onlineStatus.text = "last seen ${user.lastseenDate} at ${user.lastseenTime}"
                }

                if (user.typingStatus == "typing...") {
                    binding.typingStatus.text = user.typingStatus
                } else {
                    binding.typingStatus.text = ""
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


        /*binding.sendAttachment.setOnClickListener {
            selectedImage.launch("image/*")
        }*/

         */


        val handler = Handler()
        binding.messageBox.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {

                val hashMap: HashMap<String, String> = HashMap()
                hashMap.put("typingStatus", "typing...")
                databaseRef2.updateChildren(hashMap as Map<String, Any>)

                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                val hashMap: HashMap<String, String> = HashMap()
                hashMap.put("typingStatus", "")
                databaseRef2.updateChildren(hashMap as Map<String, Any>)
            }
        })

        binding.sendButton.setOnClickListener {

            /*val storageRef = FirebaseStorage.getInstance().getReference("chats")
                .child(user.uid)
                .child("imageMessage")

            storageRef.putFile(imageUri).addOnCompleteListener {
                storageRef.downloadUrl.addOnSuccessListener { image ->
                    val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

                    val hashMap: HashMap<String, String> = HashMap()
                    hashMap.put("imageUrl", image.toString())
                    reference.child("chat").push().setValue(hashMap)

                } .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
            } .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }

             */

            val message: String = binding.messageBox.text.toString()

            /*val imageMessage: String = imageUri.toString()

            if (imageMessage == "") {
                message = binding.messageBox.text.toString()
            } else {
                message = "photo"
            }

             */

            if (message.isNotEmpty()) {
                sendMessage(user.uid, userId, message)
                binding.messageBox.setText("")

            } else {
                Toast.makeText(applicationContext, "Your message is empty.", Toast.LENGTH_SHORT).show()
                binding.messageBox.setText("")
            }
        }

        readMessage(user.uid, userId)
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {

        val currentDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val currentTime: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("senderId", senderId)
        hashMap.put("receiverId", receiverId)
        hashMap.put("message", message)
        hashMap.put("currentDate", currentDate)
        hashMap.put("currentTime", currentTime)

        reference.child("chat").push().setValue(hashMap)

        sendNotification(message, receiverId)
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val databaseRef2: DatabaseReference = FirebaseDatabase.getInstance().getReference("chat")

        databaseRef2.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                chatList.clear()

                for (dataSnapshot: DataSnapshot in snapshot.children) {

                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.senderId.equals(senderId) && chat.receiverId.equals(receiverId) ||
                        chat.senderId.equals(receiverId) && chat.receiverId.equals(senderId)
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

    fun sendNotification(message: String, receiverId: String) {

        var senderName: String = ""

        FirebaseDatabase.getInstance().getReference("user").child(user.uid)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val senderUser = snapshot.getValue(UserDetails::class.java)
                        senderName = senderUser!!.name
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        FirebaseDatabase.getInstance().getReference("user")
            .child(receiverId).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val data = snapshot.getValue(UserDetails::class.java)

                        val notificationData = PushNotification(NotificationData(senderName, message), data!!.fcmToken)

                        ApiUitlities.getInstance().sendNotification(notificationData).enqueue(
                            object: retrofit2.Callback<PushNotification> {
                                override fun onResponse(
                                    call: Call<PushNotification>,
                                    response: Response<PushNotification>
                                ) {
                                }

                                override fun onFailure(call: Call<PushNotification>, t: Throwable) {
                                    Toast.makeText(this@ChatActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    override fun onResume() {
        super.onResume()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("onlineStatus", "Online")
        databaseRef2.updateChildren(hashMap as Map<String, Any>)
    }

    override fun onPause() {
        super.onPause()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap.put("onlineStatus", "Offline")
        hashMap.put("lastseenDate", lastseenDate)
        hashMap.put("lastseenTime", lastseenTime)

        databaseRef2.updateChildren(hashMap as Map<String, Any>)
    }
}