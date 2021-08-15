package com.ramanhmr.audioplayer.ui

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ramanhmr.audioplayer.databinding.ItemAudioBinding
import com.ramanhmr.audioplayer.entities.AudioItem
import com.ramanhmr.audioplayer.services.PlayerService
import com.ramanhmr.audioplayer.utils.MetadataUtils
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class TrackAdapter(private val activity: MainActivity) :
    ListAdapter<AudioItem, TrackAdapter.AudioViewHolder>(AudioItem.DiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder =
        AudioViewHolder(
            ItemAudioBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), activity
        )

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AudioViewHolder(
        private val binding: ItemAudioBinding,
        private val activity: MainActivity
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(audioItem: AudioItem) {
            with(binding) {
                tvTitle.text = audioItem.file.title
                tvArtist.text = audioItem.file.artist
                tvDuration.text = MetadataUtils.durationToString(audioItem.file.duration)

                root.setOnClickListener {
                    val bundle = Bundle().apply {
                        putInt(PlayerService.SHUFFLE_BUNDLE_KEY, activity.shuffleMode)
                    }
                    activity.mediaController.transportControls.playFromUri(
                        audioItem.file.uri,
                        bundle
                    )
                    activity.setControlsPause()
                    activity.showControls(audioItem.file.uri)

                    val metadata = MediaMetadataCompat.Builder()
                        .putString(MetadataUtils.ID, audioItem.file.id.toString())
                        .putString(MetadataUtils.TITLE, audioItem.file.title)
                        .putString(MetadataUtils.ARTIST, audioItem.file.artist)
                        .putString(MetadataUtils.ALBUM, audioItem.file.album)
                        .putString(MetadataUtils.URI, audioItem.file.uri.toString())
                        .putLong(MetadataUtils.DURATION, audioItem.file.duration.toLong())
                        .build()
                    activity.showInfo(metadata)
                }
            }
        }
    }
}

