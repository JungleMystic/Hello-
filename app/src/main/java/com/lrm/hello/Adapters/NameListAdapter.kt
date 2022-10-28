package com.lrm.hello.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lrm.hello.Activities.ChatActivity
import com.lrm.hello.Model.UserDetails
import com.lrm.hello.R
import de.hdodenhof.circleimageview.CircleImageView

class NameListAdapter(val context: Context, val userList: ArrayList<UserDetails>):
    RecyclerView.Adapter<NameListAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.rv_users, parent, false)

        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.nameText.text = user.name

        if (user.onlineStatus == "Online") {
            holder.onlineStatusIcon.visibility = View.VISIBLE
        } else if (user.onlineStatus == "Offline") {
            holder.onlineStatusIcon.visibility = View.GONE
        }

        Glide.with(context).load(user.profilePic).placeholder(R.drawable.profile_icon).into(holder.profilePic)

        holder.user_layout.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("uid", user.uid)
            intent.putExtra("profilePic", user.profilePic)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(view: View): RecyclerView.ViewHolder(view) {

        val nameText = view.findViewById<TextView>(R.id.name_text)
        val profilePic = view.findViewById<CircleImageView>(R.id.user_profilePic)
        val user_layout: ConstraintLayout = view.findViewById(R.id.name_list_layout)
        val onlineStatusIcon = view.findViewById<ImageView>(R.id.onlineStatusIcon)
    }
}