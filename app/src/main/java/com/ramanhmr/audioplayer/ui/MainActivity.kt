package com.ramanhmr.audioplayer.ui

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ramanhmr.audioplayer.R
import com.ramanhmr.audioplayer.databinding.ActivityMainBinding
import com.ramanhmr.audioplayer.services.PlayerService
import com.ramanhmr.audioplayer.ui.fragments.InfoFragment
import com.ramanhmr.audioplayer.ui.fragments.ListFragment
import com.ramanhmr.audioplayer.utils.MetadataUtils
import com.ramanhmr.audioplayer.viewmodels.MainViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaController: MediaControllerCompat
    private lateinit var subscriptionCallback: MediaBrowserCompat.SubscriptionCallback
    private lateinit var callback: MediaControllerCompat.Callback
    private var showingInfo = false
    var shuffleMode = PlayerService.RANDOM
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionCheck()

        callback = object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)

                when (state?.state) {
                    PlaybackStateCompat.STATE_PAUSED -> setControlsPlay()
                    PlaybackStateCompat.STATE_PLAYING -> setControlsPause()
                    PlaybackStateCompat.STATE_STOPPED -> hideControls()
                }
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                super.onMetadataChanged(metadata)
                val uri =
                    Uri.parse(metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
                binding.ivArt.setImageBitmap(MetadataUtils.getAlbumArt(uri, baseContext))
                if (showingInfo) {
                    showInfo(metadata)
                }
            }
        }
        subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {}

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, PlayerService::class.java),
            object : MediaBrowserCompat.ConnectionCallback() {
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
                        mediaController.registerCallback(callback)

                        // TODO: 01-Aug-21
//                        onConnected(mediaControllerCompat)
                    }

                }
            },
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
        }


        supportFragmentManager.beginTransaction()
            .add(
                binding.fcList.id,
                ListFragment::class.java,
                null,
                LIST_FRAGMENT_TAG
            ).commit()
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
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(callback)
        mediaBrowser.disconnect()
    }

    private fun permissionCheck() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionRequest()
        } else {
            viewModel.updateAudio()
        }
    }

    private fun permissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.updateAudio()
            } else {
                // TODO: 28-Jul-21 ask again politely
                permissionRequest()
            }
        }
    }

    fun showControls(uri: Uri? = null) {
        binding.controls.visibility = View.VISIBLE
        if (uri != null) {
            binding.ivArt.setImageBitmap(MetadataUtils.getAlbumArt(uri, this@MainActivity))
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
                LIST_FRAGMENT_TAG
            ).commit()
        showingInfo = false
    }

    fun showInfo(metadata: MediaMetadataCompat) {
        supportFragmentManager.beginTransaction()
            .replace(
                binding.fcList.id,
                InfoFragment.newInstance(metadata),
                INFO_FRAGMENT_TAG
            )
            .apply {
                if (!showingInfo) {
                    this.addToBackStack(BACKSTACK)
                }
            }
            .commit()
        showingInfo = true
    }

    fun removeInfo() {
        val fragment = supportFragmentManager.findFragmentByTag(INFO_FRAGMENT_TAG)
        if (fragment != null) {
            supportFragmentManager.popBackStack()
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        }
        showingInfo = false
    }

    companion object {
        private const val REQUEST_CODE = 3001
        private const val BACKSTACK = "Backstack"

        private const val LIST_FRAGMENT_TAG = "ListFragment"
        private const val INFO_FRAGMENT_TAG = "InfoFragment"
    }
}