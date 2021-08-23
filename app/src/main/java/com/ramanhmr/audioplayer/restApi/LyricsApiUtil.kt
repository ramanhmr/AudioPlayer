package com.ramanhmr.audioplayer.restApi

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LyricsApiUtil {
    private const val BASE_URL = "https://api.musixmatch.com/ws/1.1/"

    private fun getRetrofit() = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().registerTypeAdapter(
                    Body::class.java,
                    BodyDeserializer()
                ).create()
            )
        )
        .client(
            OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        )
        .build()

    fun getLyricsApi(): LyricsApi = getRetrofit().create(LyricsApi::class.java)
}