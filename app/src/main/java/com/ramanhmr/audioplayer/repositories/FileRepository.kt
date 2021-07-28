package com.ramanhmr.audioplayer.repositories

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.ramanhmr.audioplayer.entities.AudioFile

class FileRepository(private val context: Context) {

    fun getAllFiles(): List<AudioFile> {

        Log.i("KEK", "Getting files")

        val files = mutableListOf<AudioFile>()

        val mainUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DURATION
                )
                val cursor = context.contentResolver
                    .query(mainUri, projection, null, null, null)
                cursor?.use {
                    val idCol = it.getColumnIndex(MediaStore.Audio.Media._ID)
                    val titleCol = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val albumCol = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                    val artistCol = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    val durationCol = it.getColumnIndex(MediaStore.Audio.Media.DURATION)

                    while (it.moveToNext()) {
                        val id = it.getLong(idCol)
                        val title = it.getString(titleCol)
                        val album = it.getString(albumCol)
                        val artist = it.getString(artistCol)
                        val duration = it.getInt(durationCol)

                        val fileUri = ContentUris.withAppendedId(mainUri, id)

                        files += AudioFile(fileUri, title, album, artist, duration)
                    }
                }
            }
            else -> {
                val projection = arrayOf(
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Media.DURATION
                )
                val cursor = context.contentResolver
                    .query(mainUri, projection, null, null, null)
                if (cursor != null) {
                    val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    val albumCol = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
                    val artistCol = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
                    val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idCol)
                        val title = cursor.getString(titleCol)
                        val album = cursor.getString(albumCol)
                        val artist = cursor.getString(artistCol)
                        val duration = cursor.getInt(durationCol)

                        val fileUri = ContentUris.withAppendedId(mainUri, id)

                        files += AudioFile(fileUri, title, album, artist, duration)
                    }
                }
//                cursor?.use {
//                    val idCol = it.getColumnIndex(MediaStore.Audio.Media._ID)
//                    val titleCol = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
//                    val albumCol = it.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
//                    val artistCol = it.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
//                    val durationCol = it.getColumnIndex(MediaStore.Audio.Media.DURATION)
//
//                    while (it.moveToNext()) {
//                        val id = it.getLong(idCol)
//                        val title = it.getString(titleCol)
//                        val album = it.getString(albumCol)
//                        val artist = it.getString(artistCol)
//                        val duration = it.getInt(durationCol)
//
//                        val fileUri = ContentUris.withAppendedId(mainUri, id)
//
//                        files += AudioFile(fileUri, title, album, artist, duration)
//                    }
//                }

                cursor?.close()
            }
        }



        Log.i("KEK", "Got ${files.size} files")
        return files
    }
}