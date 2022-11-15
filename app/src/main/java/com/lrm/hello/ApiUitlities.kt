package com.lrm.hello

import com.lrm.hello.`interface`.NotificationApi
import com.lrm.hello.constants.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUitlities {

    fun getInstance (): NotificationApi {
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApi::class.java)
    }
}