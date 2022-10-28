package com.lrm.hello.Adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.lrm.hello.Model.Chat
import com.lrm.hello.R
import com.lrm.hello.databinding.DeleteMessagesBinding

class ChatAdapter(val context: Context, val chatList: ArrayList<Chat>):
    RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    val RECEIVED_MESSAGE = 0
    val SENT_MESSAGE = 1

    private lateinit var user: FirebaseUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if(viewType == RECEIVED_MESSAGE) {
            val view: View = LayoutInflater.from(parent.context).inflate(R.layout.received_message, parent, false)
            return ViewHolder(view)

        } else {
            val view: View = LayoutInflater.from(context).inflate(R.layout.sent_message, parent, false)
            return ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val currentMessage = chatList[position]
        holder.messageText.text = currentMessage.message

        if(currentMessage.message.equals("photo")) {
            holder.imageHolder.visibility = View.VISIBLE
            Glide.with(context).load(currentMessage.imageUrl).placeholder(R.drawable.image_placeholder).into(holder.imageHolder)
        }

        holder.itemView.setOnLongClickListener {
            val view = LayoutInflater.from(context).inflate(R.layout.delete_messages, null)
            val binding: DeleteMessagesBinding = DeleteMessagesBinding.bind(view)

            val dialog = AlertDialog.Builder(context)
                .setView(binding.root)
                .create()

            /*binding.deleteForEveryone.setOnClickListener {
                currentMessage.message = "This message is deleted."
                FirebaseDatabase.getInstance().getReference("chat").child(user.uid).setValue(currentMessage)
            }

             */

            binding.cancelDelete.setOnClickListener { dialog.dismiss() }

            dialog.show()

            false
        }

        holder.currentDate.text = currentMessage.currentDate
        holder.currentTime.text = currentMessage.currentTime
    }

    override fun getItemViewType(position: Int): Int {
        user = FirebaseAuth.getInstance().currentUser!!

        if (chatList[position].senderId  == user.uid) {

            return SENT_MESSAGE
        } else {
            return RECEIVED_MESSAGE
        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.message_text)
        val currentDate: TextView = view.findViewById(R.id.currentDate)
        val currentTime: TextView = view.findViewById(R.id.currentTime)
        val imageHolder: ImageView = view.findViewById(R.id.image_holder)
    }
}