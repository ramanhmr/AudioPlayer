package com.ramanhmr.audioplayer.database

import androidx.room.Entity

@Entity(tableName = "audio_stats", primaryKeys = ["title", "artist", "album"])
data class AudioStatsEntity(
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val rating0: Int,
    val rating1: Int,
    val rating2: Int,
    val rating3: Int,
    val rating4: Int,
    val rating5: Int,
    val rating6: Int,
    val rating7: Int,
    val rating8: Int,
    val rating9: Int
)