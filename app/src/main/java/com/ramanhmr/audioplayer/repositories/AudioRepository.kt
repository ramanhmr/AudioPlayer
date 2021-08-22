package com.ramanhmr.audioplayer.repositories

import android.net.Uri
import android.provider.MediaStore
import com.ramanhmr.audioplayer.daos.FileDao
import com.ramanhmr.audioplayer.entities.AudioItem
import com.ramanhmr.audioplayer.entities.AudioStats

class AudioRepository(private val fileDao: FileDao, private val statRepository: StatRepository) {

    fun getAllItems(sortOrder: String? = MediaStore.Audio.Media.TITLE): List<AudioItem> =
        fileDao.getAllFiles(sortOrder)
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