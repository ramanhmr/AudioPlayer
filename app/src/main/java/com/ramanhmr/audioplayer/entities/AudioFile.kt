package com.ramanhmr.audioplayer.entities

import android.net.Uri

data class AudioFile(
    val id: Long,
    val uri: Uri,
    val title: String?,
    val album: String?,
    val artist: String?,
    val duration: Int
)