package com.ramanhmr.audioplayer.ui

import android.content.ComponentName
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.ramanhmr.audioplayer.R
import com.ramanhmr.audioplayer.databinding.ActivityMainBinding
import com.ramanhmr.audioplayer.repositories.ArtRepository
import com.ramanhmr.audioplayer.services.PlayerService
import com.ramanhmr.audioplayer.ui.fragments.InfoFragment
import com.ramanhmr.audioplayer.ui.fragments.ListFragment
import com.ramanhmr.audioplayer.ui.fragments.LyricsFragment
import com.ramanhmr.audioplayer.utils.MetadataUtils
import com.ramanhmr.audioplayer.viewmodels.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private val artRepository: ArtRepository by inject()
    private lateinit var mediaBrowser: MediaBrowserCompat
    lateinit var mediaController: MediaControllerCompat
    private lateinit var subscriptionCallback: MediaBrowserCompat.SubscriptionCallback
    private lateinit var controllerCallback: MediaControllerCompat.Callback
    private var currentFragment = LIST_FRAGMENT
    var shuffleMode = PlayerService.RANDOM
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.updateAudio()

        controllerCallback = getControllerCallback()
        subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {
            // TODO: 16-Aug-21
        }

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, PlayerService::class.java),
            getConnectionCallback(),
            null
        )

        with(binding) {
            ivNext.setOnClickListener { mediaController.transportControls.skipToNext() }
            ivPrevious.setOnClickListener { mediaController.transportControls.skipToPrevious() }
            ivPlayPause.setOnClickListener {
                mediaController.transportControls.pause()
                checkControlsPausePlay()
            }
            ivArt.setOnClickListener { showInfo(mediaController.metadata) }
            ivShuffle.setOnClickListener { changeShuffleMod() }
        }

        supportFragmentManager.beginTransaction()
            .add(
                binding.fcList.id,
                ListFragment::class.java,
                null,
                LIST_FRAGMENT
            ).commit()
    }

    private fun changeShuffleMod() {
        when (shuffleMode) {
            PlayerService.RANDOM -> {
                mediaController.transportControls.sendCustomAction(
                    PlayerService.SET_SHUFFLE,
                    Bundle().apply { putInt(PlayerService.SHUFFLE_BUNDLE_KEY, PlayerService.ORDER) }
                )
                binding.ivShuffle.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.shuffle_order
                    )
                )
                shuffleMode = PlayerService.ORDER
            }
            PlayerService.ORDER -> {
                mediaController.transportControls.sendCustomAction(
                    PlayerService.SET_SHUFFLE,
                    Bundle().apply {
                        putInt(
                            PlayerService.SHUFFLE_BUNDLE_KEY,
                            PlayerService.RANDOM
                        )
                    }
                )
                binding.ivShuffle.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this,
                        R.drawable.shuffle_random
                    )
                )
                shuffleMode = PlayerService.RANDOM
            }
        }
    }

    private fun getConnectionCallback() = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            if (mediaBrowser.isConnected) {

                mediaBrowser.unsubscribe(PlayerService.ROOT)
                mediaBrowser.subscribe(PlayerService.ROOT, subscriptionCallback)

                mediaController = MediaControllerCompat(
                    this@MainActivity,
                    mediaBrowser.sessionToken
                )
                MediaControllerCompat.setMediaController(
                    this@MainActivity,
                    mediaController
                )
                mediaController.registerCallback(controllerCallback)

                checkIfPlaying()
            }
        }
    }

    private fun checkIfPlaying() {
        if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            showInfo(mediaController.metadata)
            viewModel.startProgressSetPosition(mediaController.playbackState.position.toInt())
            showControls(
                Uri.parse(
                    mediaController.metadata.getString(
                        MetadataUtils.URI
                    )
                )
            )
        }
    }

    private fun getControllerCallback() = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)

            when (state?.state) {
                PlaybackStateCompat.STATE_PAUSED -> {
                    setControlsPlay()
                    viewModel.stopProgressSetPosition(state.position.toInt())
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    setControlsPause()
                    viewModel.startProgressSetPosition(state.position.toInt())
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    hideControls()
                    viewModel.stopProgressSetPosition(state.position.toInt())
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat) {
            super.onMetadataChanged(metadata)
            val uri =
                Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
            binding.ivArt.setImageBitmap(artRepository.getAlbumArt(uri, baseContext))
            if (currentFragment == INFO_FRAGMENT) {
                showInfo(metadata)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    fun showControls(uri: Uri? = null) {
        checkControlsPausePlay()
        binding.controls.visibility = View.VISIBLE
        if (uri != null) {
            binding.ivArt.setImageBitmap(artRepository.getAlbumArt(uri, this@MainActivity))
        }
    }

    fun hideControls() {
        binding.controls.visibility = View.GONE
    }

    private fun checkControlsPausePlay() {
        if (mediaController.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            setControlsPause()
        } else {
            setControlsPlay()
        }
    }

    fun setControlsPlay() {
        with(binding) {
            ivPlayPause.setImageDrawable(
                AppCompatResources.getDrawable(
                    baseContext,
                    R.drawable.play
                )
            )
            ivPlayPause.setOnClickListener {
                mediaController.transportControls.play()
                setControlsPause()
            }
        }
    }

    fun setControlsPause() {
        with(binding) {
            ivPlayPause.setImageDrawable(
                AppCompatResources.getDrawable(baseContext, R.drawable.pause)
            )
            ivPlayPause.setOnClickListener {
                mediaController.transportControls.pause()
                setControlsPlay()
            }
        }
    }

    fun showList() {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.fcList.id,
                ListFragment::class.java,
                null,
                LIST_FRAGMENT
            ).commit()
        currentFragment = LIST_FRAGMENT
    }

    fun showInfo(metadata: MediaMetadataCompat) {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.fcList.id,
                InfoFragment.newInstance(metadata),
                INFO_FRAGMENT
            )
            .apply {
                if (currentFragment == LIST_FRAGMENT) {
                    this.addToBackStack(BACKSTACK)
                }
            }
            .commit()
        currentFragment = INFO_FRAGMENT
    }

    fun showLyrics(lyrics: String, metadata: MediaMetadataCompat) {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.fcList.id,
                LyricsFragment.newInstance(lyrics, metadata),
                LYRICS_FRAGMENT
            )
//            .addToBackStack(BACKSTACK)
            .commit()
        currentFragment = LYRICS_FRAGMENT
    }

    fun removeInfo() {
        val fragment = supportFragmentManager.findFragmentByTag(INFO_FRAGMENT)
        if (fragment != null) {
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
        currentFragment = LIST_FRAGMENT
    }

    fun removeLyrics() {
        val fragment = supportFragmentManager.findFragmentByTag(LYRICS_FRAGMENT)
        if (fragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    binding.fcList.id,
                    InfoFragment.newInstance(mediaController.metadata),
                    INFO_FRAGMENT
                )
                .commit()
            currentFragment = INFO_FRAGMENT
        }
    }

    companion object {
        private const val BACKSTACK = "Backstack"
        private const val LIST_FRAGMENT = "ListFragment"
        private const val INFO_FRAGMENT = "InfoFragment"
        private const val LYRICS_FRAGMENT = "LyricsFragment"
    }
}