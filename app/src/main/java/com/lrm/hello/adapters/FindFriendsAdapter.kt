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
import com.lrm.hello.model.UserDetails
import de.hdodenhof.circleimageview.CircleImageView

class FindFriendsAdapter(val context: Context, val userList: ArrayList<UserDetails>):
    RecyclerView.Adapter<FindFriendsAdapter.UserViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindFriendsAdapter.UserViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.rv_find_friends, parent, false)

        return UserViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = user.name

        Glide.with(context).load(user.profilePic).placeholder(R.drawable.profile_icon).into(holder.profilePic)

        if (user.onlineStatus == "Online") {
            holder.onlineStatusIcon.visibility = View.VISIBLE
        } else if (user.onlineStatus == "Offline") {
            holder.onlineStatusIcon.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(view: View, listener: onItemClickListener): RecyclerView.ViewHolder(view) {
        val nameText = view.findViewById<TextView>(R.id.name_text)
        val profilePic = view.findViewById<CircleImageView>(R.id.user_profilePic)
        val onlineStatusIcon = view.findViewById<ImageView>(R.id.onlineStatusIcon)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }
}