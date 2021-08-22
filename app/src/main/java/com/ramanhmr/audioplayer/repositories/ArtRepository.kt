package com.ramanhmr.audioplayer.repositories

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.ramanhmr.audioplayer.services.PlayerService
import com.ramanhmr.audioplayer.utils.LastItemsQueue
import com.ramanhmr.audioplayer.utils.MetadataUtils

object ArtRepository {
    private const val MAX_ITEMS = PlayerService.MAX_LAST_PLAYED + 1
    private val uriBitmaps = LastItemsQueue<Pair<Uri, Bitmap>>(MAX_ITEMS)

    fun getAlbumArt(uri: Uri, context: Context): Bitmap {
        uriBitmaps.forEach {
            if (it.first == uri) return it.second
        }
        val art = MetadataUtils.getAlbumArt(uri, context)
        uriBitmaps.add(Pair(uri, art))
        return art
    }
}