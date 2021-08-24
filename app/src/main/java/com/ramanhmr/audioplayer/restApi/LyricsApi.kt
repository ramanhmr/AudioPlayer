package com.ramanhmr.audioplayer.restApi

import com.ramanhmr.audioplayer.BuildConfig
import retrofit2.http.GET
import retrofit2.http.Query

interface LyricsApi {

    @GET("matcher.lyrics.get?format=json&callback=callback&apikey=${BuildConfig.LYRICS_API_KEY}")
    suspend fun getLyricsResponse(
        @Query("q_track") track: String,
        @Query("q_artist") artist: String
    ): LyricsResponseModel
}