package com.lrm.hello.`interface`

import com.lrm.hello.model.PushNotification
import com.lrm.hello.constants.Constants.Companion.CONTENT_TYPE
import com.lrm.hello.constants.Constants.Companion.SERVER_KEY
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