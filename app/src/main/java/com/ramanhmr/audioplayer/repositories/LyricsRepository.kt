package com.ramanhmr.audioplayer.repositories

import com.ramanhmr.audioplayer.restApi.LyricsApi
import com.ramanhmr.audioplayer.restApi.LyricsResponseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricsRepository(private val lyricsApi: LyricsApi) {

    suspend fun getLyrics(title: String, artist: String) = withContext(Dispatchers.IO) {
        lyricsApi.getLyricsResponse(title, artist).toLyricsString()
    }

    private fun LyricsResponseEntity.toLyricsString(): String {
        return if (this.message.body != null) {
            this.message.body.lyrics.lyrics_body
        } else {
            "No lyrics found"
        }
    }
}