package com.ramanhmr.audioplayer.interfaces

interface LyricsSource {
    suspend fun getLyrics(title: String, artist: String): String
}