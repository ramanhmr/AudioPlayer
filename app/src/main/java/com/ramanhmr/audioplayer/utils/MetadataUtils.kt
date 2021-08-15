package com.ramanhmr.audioplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.ramanhmr.audioplayer.R

object MetadataUtils {
    const val ID = MediaMetadataCompat.METADATA_KEY_MEDIA_ID
    const val TITLE = MediaMetadataCompat.METADATA_KEY_TITLE
    const val ARTIST = MediaMetadataCompat.METADATA_KEY_ARTIST
    const val ALBUM = MediaMetadataCompat.METADATA_KEY_ALBUM
    const val URI = MediaMetadataCompat.METADATA_KEY_MEDIA_URI
    const val DURATION = MediaMetadataCompat.METADATA_KEY_DURATION

    fun getAlbumArt(uri: Uri, context: Context): Bitmap {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)
        val embedPicture = retriever.embeddedPicture
        return if (embedPicture != null) {
            BitmapFactory.decodeByteArray(embedPicture, 0, embedPicture.size)
        } else {
            ResourcesCompat.getDrawable(context.resources, R.drawable.default_album, null)!!
                .toBitmap()
        }
    }

    fun durationToString(duration: Long): String {
        val hours = duration / (1000 * 60 * 60)
        val minutes = (duration / (1000 * 60)) % 60
        val seconds = (duration / 1000) % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun durationToString(duration: Int): String {
        return durationToString(duration.toLong())
    }
}