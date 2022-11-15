package com.lrm.hello.model

data class UserDetails(
    var name: String = "",
    var email: String = "",
    var uid: String = "",
    var profilePic: String = "",
    var fcmToken: String = "",
    var typingStatus: String = "",
    var onlineStatus: String = "",
    var lastseenDate: String = "",
    var lastseenTime: String = ""
)