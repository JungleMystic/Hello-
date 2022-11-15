package com.lrm.hello.model

data class FriendRequests (
    var senderName: String = "",
    var senderProfilePic: String = "",
    var senderId:String = "",
    var receiverId:String = "",
    var requestStatus: String = "",
    var requestId: String = "",
    var requestDate: String = "",
    var requestTime: String = ""
)