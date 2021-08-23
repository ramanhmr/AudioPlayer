package com.ramanhmr.audioplayer.restApi

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class LyricsResponseEntity(
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

class BodyDeserializer : JsonDeserializer<Body> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Body? {
        if (json.isJsonObject) {
            val bodyObject = json.asJsonObject
            return if (bodyObject.has("lyrics")) {
                val lyricsObject = bodyObject.get("lyrics").asJsonObject
                Body(
                    Lyrics(
                        lyricsObject.get("lyrics_id").asInt,
                        lyricsObject.get("explicit").asInt,
                        lyricsObject.get("lyrics_body").asString,
                        lyricsObject.get("script_tracking_url").asString,
                        lyricsObject.get("pixel_tracking_url").asString,
                        lyricsObject.get("lyrics_copyright").asString,
                        lyricsObject.get("updated_time").asString
                    )
                )
            } else null
        } else return null
    }
}