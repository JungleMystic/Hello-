package com.lrm.hello.Model

data class PushNotification (
    val data: NotificationData,
    val to: String? = ""
)