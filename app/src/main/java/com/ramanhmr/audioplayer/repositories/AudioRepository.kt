package com.ramanhmr.audioplayer.repositories

import android.net.Uri
import com.ramanhmr.audioplayer.daos.FileDao
import com.ramanhmr.audioplayer.entities.AudioItem
import com.ramanhmr.audioplayer.entities.AudioStats

class AudioRepository(private val fileDao: FileDao, private val statRepository: StatRepository) {

    fun getAllItems(): List<AudioItem> = fileDao.getAllFiles()
        .map { AudioItem(it, AudioStats(it.uri, it.title, it.artist, it.album, arrayListOf())) }

    fun getItemByUri(uri: Uri): AudioItem? {
        val file = fileDao.getFileByUri(uri)
        return if (file != null) {
            AudioItem(
                file,
                AudioStats(file.uri, file.title, file.artist, file.album, arrayListOf())
            )
        } else null
    }
}