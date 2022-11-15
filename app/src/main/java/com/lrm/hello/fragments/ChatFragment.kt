package com.lrm.hello.fragments

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.lrm.hello.ApiUitlities
import com.lrm.hello.R
import com.lrm.hello.ScrollToBottom
import com.lrm.hello.adapters.ChatAdapter
import com.lrm.hello.databinding.FragmentChatBinding
import com.lrm.hello.model.Chat
import com.lrm.hello.model.NotificationData
import com.lrm.hello.model.PushNotification
import com.lrm.hello.model.UserDetails
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: ArrayList<Chat>
    private lateinit var databaseRef: DatabaseReference
    private lateinit var databaseRef2: DatabaseReference
    private lateinit var user: FirebaseUser
    private lateinit var manager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatList = ArrayList()
        chatRecyclerView = binding.chatRecyclerview
        manager = LinearLayoutManager(requireContext())
        manager.stackFromEnd = true
        chatRecyclerView.layoutManager = manager

        user = FirebaseAuth.getInstance().currentUser!!

        val navArgs: ChatFragmentArgs by navArgs()
        val friendUid = navArgs.friendUid

        binding.backButton.setOnClickListener {
            val action = ChatFragmentDirections.actionChatFragmentToFriendsChatListFragment()
            this.findNavController().navigate(action)
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("user").child(friendUid)
        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val friend = snapshot.getValue(UserDetails::class.java)

                binding.chatProfileName.text = friend!!.name

                if (friend.profilePic == "") {
                    binding.chatUserProfilePic.setImageResource(R.drawable.profile_icon)
                } else {
                    Glide.with(requireContext()).load(friend.profilePic).into(binding.chatUserProfilePic)
                }

                if (friend.onlineStatus == "Online") {
                    binding.onlineStatus.text = friend.onlineStatus
                } else if (friend.onlineStatus == "Offline") {
                    binding.onlineStatus.text = "last seen ${friend.lastseenDate} at ${friend.lastseenTime}"
                }

                if (friend.typingStatus == "typing...") {
                    binding.typingStatus.text = friend.typingStatus
                } else {
                    binding.typingStatus.text = ""
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        databaseRef2 = FirebaseDatabase.getInstance().getReference("user").child(user.uid)

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
            val message: String = binding.messageBox.text.toString()

            if (message.isNotEmpty()) {
                sendMessage(user.uid, friendUid, message)
                binding.messageBox.setText("")

            } else {
                Toast.makeText(context, "Your message is empty.", Toast.LENGTH_SHORT).show()
                binding.messageBox.setText("")
            }
        }
        readMessage(user.uid, friendUid)
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
        val databaseRef3: DatabaseReference = FirebaseDatabase.getInstance().getReference("chat")

        databaseRef3.addValueEventListener(object: ValueEventListener {
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

                chatAdapter = ChatAdapter(requireContext(), chatList)
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
                                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


}