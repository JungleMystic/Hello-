package com.lrm.hello.model

data class PushNotification (
    val data: NotificationData,
    val to: String? = ""
)