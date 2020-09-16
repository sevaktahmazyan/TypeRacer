package com.webbfontaine.typeracer.network

import com.google.gson.JsonArray
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("?format=json")
    fun getText(@Query("type") type: String?, @Query("paras") paragraphs: String?, @Query("sentences") sentences: Int?): Single<JsonArray>
}