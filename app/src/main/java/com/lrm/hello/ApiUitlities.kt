package com.lrm.hello

import com.lrm.hello.Constants.Constants
import com.lrm.hello.Interface.NotificationApi
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