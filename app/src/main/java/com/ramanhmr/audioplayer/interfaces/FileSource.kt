package com.ramanhmr.audioplayer.interfaces

import android.net.Uri
import com.ramanhmr.audioplayer.entities.AudioFile

interface FileSource {
    suspend fun getAllFiles(sortOrder: String?): List<AudioFile>

    suspend fun getFileByUri(uri: Uri): AudioFile?
}