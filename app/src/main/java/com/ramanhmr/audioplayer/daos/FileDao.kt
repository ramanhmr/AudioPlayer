package com.ramanhmr.audioplayer.daos

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.ramanhmr.audioplayer.entities.AudioFile
import com.ramanhmr.audioplayer.interfaces.FileSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileDao(private val context: Context) : FileSource {

    override suspend fun getAllFiles(sortOrder: String?): List<AudioFile> {
        val files = mutableListOf<AudioFile>()

        withContext(Dispatchers.IO) {
            val baseUri = getMainUri()
            val cursor =
                getCursor(baseUri, sortOrder ?: MediaStore.Audio.Media.TITLE, getProjection())
            while (cursor?.moveToNext() == true) {
                files += cursor.getAudioFile(baseUri)
            }
            cursor?.close()
        }
        return files
    }

    override suspend fun getFileByUri(uri: Uri): AudioFile? {
        var file: AudioFile?
        withContext(Dispatchers.IO) {
            val cursor = getCursor(uri, null, getProjection())
            file = if (cursor?.moveToFirst() == true) cursor.getAudioFileSetUri(uri) else null
            cursor?.close()
        }
        return file
    }

    private fun getCursor(uri: Uri, sortOrder: String?, projection: Array<String>): Cursor? =
        context.contentResolver.query(uri, projection, null, null, sortOrder)

    private fun getMainUri(): Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    }

    //    Changing fields fetched form MediaStore requires adding them to AudioFile class, getProjection() and Cursor.getAudioFile() functions
    private fun getProjection(): Array<String> = arrayOf(ID, TITLE, ALBUM, ARTIST, DURATION)

    private fun Cursor.getAudioFile(baseUri: Uri): AudioFile {
        val id = this.getLong(0)
        val title = this.getString(1)
        val album = this.getString(2)
        val artist = this.getString(3)
        val duration = this.getInt(4)

        val fileUri = ContentUris.withAppendedId(baseUri, id)

        return AudioFile(id, fileUri, title, album, artist, duration)
    }

    private fun Cursor.getAudioFileSetUri(fileUri: Uri): AudioFile {
        val id = this.getLong(0)
        val title = this.getString(1)
        val album = this.getString(2)
        val artist = this.getString(3)
        val duration = this.getInt(4)

        return AudioFile(id, fileUri, title, album, artist, duration)
    }

    companion object {
        private const val ID = MediaStore.Audio.Media._ID
        private const val TITLE = MediaStore.Audio.Media.TITLE
        private val ALBUM =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MediaStore.Audio.Media.ALBUM else MediaStore.Audio.Albums.ALBUM
        private val ARTIST =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) MediaStore.Audio.Media.ARTIST else MediaStore.Audio.Artists.ARTIST
        private const val DURATION = MediaStore.Audio.Media.DURATION
    }
}