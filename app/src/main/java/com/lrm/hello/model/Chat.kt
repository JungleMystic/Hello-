package com.lrm.hello.model

data class Chat (
    var senderId:String = "",
    var receiverId:String = "",
    var message:String = "",
    var messageType:String = "",
    var imageUri:String = "",
    var currentDate: String = "",
    var currentTime: String = ""
)