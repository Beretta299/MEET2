package com.karasm.meet.server_connect

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

class RetroInstance {




    var INTERFACE: IRetroInterface?=null
    private val URL = "https://hellotherre.000webhostapp.com/"
    private var logging: HttpLoggingInterceptor?=null

    private constructor(){

        logging = HttpLoggingInterceptor()
        logging!!.level = HttpLoggingInterceptor.Level.BODY

        val httpClient = OkHttpClient.Builder()

        httpClient.addInterceptor(logging)

        val builder = Retrofit.Builder()
            .baseUrl(URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create()).client(httpClient.build())

        val retrofit = builder.build()

        INTERFACE = retrofit.create<IRetroInterface>(IRetroInterface::class.java!!)
    }

    companion object{
        private var instance: RetroInstance? = null
            fun getInstance(): RetroInstance {
                instance = RetroInstance()
                return instance!!
            }

        }

}