package com.ramanhmr.audioplayer.entities

import android.net.Uri

data class AudioFile(
    val uri: Uri,
    val title: String,
    val album: String,
    val artist: String,
    val duration: Int
) {
    fun durationToString(): String {
        val hours = duration / (1000 * 60 * 60)
        val minutes = (duration / (1000 * 60)) % 60
        val seconds = (duration / 1000) % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}