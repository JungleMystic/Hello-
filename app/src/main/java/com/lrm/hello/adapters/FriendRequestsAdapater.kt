package com.lrm.hello.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lrm.hello.R
import com.lrm.hello.model.FriendRequests
import de.hdodenhof.circleimageview.CircleImageView

class FriendRequestsAdapater(val context: Context, val friendRequestList: ArrayList<FriendRequests>):
    RecyclerView.Adapter<FriendRequestsAdapater.UserViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestsAdapater.UserViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.rv_friend_requests, parent, false)

        return UserViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = friendRequestList[position]
        holder.senderNameText.text = user.senderName

        Glide.with(context).load(user.senderProfilePic).placeholder(R.drawable.profile_icon).into(holder.senderProfilePic)

        holder.statusUpdate.text = user.requestStatus

        holder.requestedDate.text = user.requestDate
        holder.requestedTime.text = user.requestTime
    }

    override fun getItemCount(): Int {
        return friendRequestList.size
    }

    class UserViewHolder(view: View, listener: onItemClickListener): RecyclerView.ViewHolder(view) {

        val senderNameText = view.findViewById<TextView>(R.id.fr_nameText)
        val senderProfilePic = view.findViewById<CircleImageView>(R.id.fr_profilePic)
        val requestedDate = view.findViewById<TextView>(R.id.requestDate)
        val requestedTime = view.findViewById<TextView>(R.id.requestTime)
        val statusUpdate = view.findViewById<TextView>(R.id.statusUpdate)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}