package com.ramanhmr.audioplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.net.URI

@Dao
interface AudioStatsDao {

    @Query("SELECT * FROM audio_stats WHERE uri LIKE :uri")
    fun getStatsByURI(uri: URI): AudioStatsEntity?

    @Query("SELECT * FROM audio_stats WHERE title LIKE :title AND album LIKE :album AND artist LIKE :artist")
    fun getStatsByInfo(title: String, album: String, artist: String): AudioStatsEntity?

    @Insert
    fun addStats(item: AudioStatsEntity)
}