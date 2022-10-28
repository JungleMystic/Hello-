package com.lrm.hello.Model

data class Chat(
    var messageId: String = "",
    var senderId:String = "",
    var receiverId:String = "",
    var message:String = "",
    var imageUrl:String = "",
    var currentDate: String = "",
    var currentTime: String = ""
)