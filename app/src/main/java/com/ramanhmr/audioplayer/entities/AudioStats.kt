package com.ramanhmr.audioplayer.entities

import android.net.Uri

data class AudioStats(
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val ratings: ArrayList<Int>
)