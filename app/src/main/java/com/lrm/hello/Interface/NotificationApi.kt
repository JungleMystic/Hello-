package com.lrm.hello.Interface

import com.lrm.hello.Constants.Constants.Companion.CONTENT_TYPE
import com.lrm.hello.Constants.Constants.Companion.SERVER_KEY
import com.lrm.hello.Model.PushNotification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Authorization:key=$SERVER_KEY","Content-Type:$CONTENT_TYPE")
    @POST("fcm/send")
    fun sendNotification(
        @Body notification: PushNotification
    ): Call<PushNotification>
}