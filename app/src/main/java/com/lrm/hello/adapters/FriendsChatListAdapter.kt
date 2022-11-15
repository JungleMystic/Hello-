package com.lrm.hello.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lrm.hello.R
import com.lrm.hello.model.FriendsList
import de.hdodenhof.circleimageview.CircleImageView

class FriendsChatListAdapter(val context: Context, val friendsList: ArrayList<FriendsList>): RecyclerView.Adapter<FriendsChatListAdapter.UserViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsChatListAdapter.UserViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.rv_friends, parent, false)

        return UserViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val friends = friendsList[position]

        holder.nameText.text = friends.friendName

        if (friends.friendProfilePic == "") {
            Glide.with(context).load(R.drawable.profile_icon).into(holder.profilePic)
        } else {
            Glide.with(context).load(friends.friendProfilePic).placeholder(R.drawable.profile_icon).into(holder.profilePic)
        }

        /*if (friends.friendOnlineStatus == "Online") {
            holder.onlineStatusIcon.visibility = View.VISIBLE
        } else if (friends.friendOnlineStatus == "Offline") {
            holder.onlineStatusIcon.visibility = View.GONE
        }

         */
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    class UserViewHolder(view: View, listener: onItemClickListener): RecyclerView.ViewHolder(view) {
        val nameText = view.findViewById<TextView>(R.id.frList_name_text)
        val profilePic = view.findViewById<CircleImageView>(R.id.frList_profilePic)
        //val onlineStatusIcon = view.findViewById<ImageView>(R.id.onlineStatusIcon)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}