package com.lrm.hello.Interface

import com.lrm.hello.Constants.Constants.Companion.CONTENT_TYPE
import com.lrm.hello.Constants.Constants.Companion.SERVER_KEY
import com.lrm.hello.Model.PushNotificationData
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Authorization: key=$SERVER_KEY","Content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification:PushNotificationData
    ): Response<ResponseBody>
}