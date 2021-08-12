package com.ramanhmr.audioplayer.entities

import androidx.recyclerview.widget.DiffUtil

data class AudioItem(
    val file: AudioFile,
    val stats: AudioStats
) {
    class DiffUtilCallback : DiffUtil.ItemCallback<AudioItem>() {
        override fun areItemsTheSame(oldItem: AudioItem, newItem: AudioItem): Boolean =
            oldItem.file.uri == newItem.file.uri

        override fun areContentsTheSame(oldItem: AudioItem, newItem: AudioItem): Boolean =
            oldItem.file.title == newItem.file.title
                    && oldItem.file.artist == newItem.file.artist
                    && oldItem.file.album == newItem.file.album
                    && oldItem.stats.uri == newItem.stats.uri
    }
}