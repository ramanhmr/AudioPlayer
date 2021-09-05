package com.ramanhmr.audioplayer.repositories

import android.net.Uri
import android.provider.MediaStore
import com.ramanhmr.audioplayer.entities.AudioItem
import com.ramanhmr.audioplayer.entities.AudioStats
import com.ramanhmr.audioplayer.interfaces.FileSource

class AudioRepository(
    private val fileSource: FileSource,
    private val statRepository: StatRepository
) {

    suspend fun getAllItems(sortOrder: String? = MediaStore.Audio.Media.TITLE): List<AudioItem> =
        fileSource.getAllFiles(sortOrder)
            .map { AudioItem(it, AudioStats(it.uri, it.title, it.artist, it.album, arrayListOf())) }

    suspend fun getItemByUri(uri: Uri): AudioItem? {
        val file = fileSource.getFileByUri(uri)
        return if (file != null) {
            AudioItem(
                file,
                AudioStats(file.uri, file.title, file.artist, file.album, arrayListOf())
            )
        } else null
    }
}