package com.ramanhmr.audioplayer.restApi

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

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