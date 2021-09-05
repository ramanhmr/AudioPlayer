package com.ramanhmr.audioplayer.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AudioStatsEntity::class], version = 1)
abstract class AudioStatsDatabase : RoomDatabase() {
    abstract fun audioStatsDao(): AudioStatsDao
}