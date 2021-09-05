package com.ramanhmr.audioplayer.repositories

import com.ramanhmr.audioplayer.interfaces.LyricsSource
import com.ramanhmr.audioplayer.restApi.LyricsApi
import com.ramanhmr.audioplayer.restApi.LyricsResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LyricsRepository(private val lyricsApi: LyricsApi) : LyricsSource {

    override suspend fun getLyrics(title: String, artist: String): String {
        val lyrics = withContext(Dispatchers.IO) {
            lyricsApi.getLyricsResponse(title, artist).toLyricsString()
        }
        return lyrics ?: "No lyrics for track $title - $artist found"
    }

    private fun LyricsResponseModel.toLyricsString(): String? {
        return if (this.message.body != null) {
            this.message.body.lyrics.lyrics_body
        } else null
    }
}