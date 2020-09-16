package com.webbfontaine.typeracer.network

import com.google.gson.GsonBuilder
import com.webbfontaine.typeracer.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiServiceProvider {


    companion object {
        private val loggingInterceptor = HttpLoggingInterceptor()

        val apiService by lazy {
            createRetrofit(BuildConfig.BASE_URL).create(ApiService::class.java)
        }

        private fun createRetrofit(serverURL: String): Retrofit {

            val httpClientBuilder =
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
                httpClientBuilder.addInterceptor(loggingInterceptor)
            }


            return Retrofit.Builder()
                .baseUrl(serverURL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .client(httpClientBuilder.build()).build()
        }
    }
}