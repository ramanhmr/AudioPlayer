package com.ramanhmr.audioplayer.restApi

import com.google.gson.annotations.SerializedName

data class LyricsResponseModel(
    @SerializedName("message") val message: Message
)

data class Message(
    @SerializedName("header") val header: Header,
    @SerializedName("body") val body: Body?
)

data class Header(
    @SerializedName("status_code") val status_code: Int,
    @SerializedName("execute_time") val execute_time: Double
)

data class Body(
    @SerializedName("lyrics") val lyrics: Lyrics,
)

data class Lyrics(
    @SerializedName("lyrics_id") val lyrics_id: Int,
    @SerializedName("explicit") val explicit: Int,
    @SerializedName("lyrics_body") val lyrics_body: String,
    @SerializedName("script_tracking_url") val script_tracking_url: String,
    @SerializedName("pixel_tracking_url") val pixel_tracking_url: String,
    @SerializedName("lyrics_copyright") val lyrics_copyright: String,
    @SerializedName("updated_time") val updated_time: String,
)