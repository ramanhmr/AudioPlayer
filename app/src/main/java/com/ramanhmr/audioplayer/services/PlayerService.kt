package com.ramanhmr.audioplayer.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.ArrayMap
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.ramanhmr.audioplayer.R
import com.ramanhmr.audioplayer.daos.FileDao
import com.ramanhmr.audioplayer.entities.AudioFile
import com.ramanhmr.audioplayer.ui.MainActivity
import com.ramanhmr.audioplayer.utils.LastItemsQueue
import org.koin.android.ext.android.inject
import kotlin.random.Random

class PlayerService : MediaBrowserServiceCompat(), MediaPlayer.OnCompletionListener {
    private val fileDao: FileDao by inject()
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionCallback: MediaSessionCompat.Callback
    private lateinit var playbackState: PlaybackStateCompat
    private lateinit var playbackStateBuilder: PlaybackStateCompat.Builder
    private var audioList = arrayListOf<AudioFile>()
    private val mediaPlayer = MediaPlayer()
    private val audioMap = ArrayMap<Int, AudioFile>()
    private lateinit var lastPlayed: LastItemsQueue<AudioFile>
    private var shuffleMode = RANDOM
    private var inPrevious = false

    override fun onCreate() {
        super.onCreate()

        getAllMedia()

        mediaSessionCallback = object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()

                mediaPlayer.start()

                if (mediaPlayer.isPlaying) {
                    playbackState =
                        playbackStateBuilder
                            .setState(PlaybackStateCompat.STATE_PLAYING, 0L, 1F)
                            .setActions(PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_STOP)
                            .build()
                    mediaSession.setPlaybackState(playbackState)
                }
                showNotification()
                // TODO: 01-Aug-21
            }

            override fun onPause() {
                super.onPause()

                mediaPlayer.pause()

                if (!mediaPlayer.isPlaying) {
                    playbackState =
                        playbackStateBuilder
                            .setState(PlaybackStateCompat.STATE_PAUSED, 0L, 1F)
                            .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_STOP)
                            .build()
                    mediaSession.setPlaybackState(playbackState)
                }
                // TODO: 01-Aug-21
            }

            override fun onStop() {
                super.onStop()

                mediaPlayer.stop()
                mediaPlayer.release()
                stopSelf()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()

                if (inPrevious) {
                    if (lastPlayed.hasNext()) {
                        val next = lastPlayed.next()!!
                        playUri(next.uri)
                        audioMap[CURRENT] = next
                    } else {
                        inPrevious = false
                        onSkipToNext()
                    }
                } else {
                    when (shuffleMode) {
                        RANDOM -> {
                            setRandomNext()

                            with(mediaPlayer) {
                                stop()
                                reset()
                                setDataSource(baseContext, audioMap[NEXT]!!.uri)
                                prepare()
                            }
                            onPlay()
                            lastPlayed.add(audioMap[CURRENT]!!)
                            audioMap[CURRENT] = audioMap[NEXT]
                        }
                        BY_SCORES -> {
                        }
                    }
                }


                // TODO: 01-Aug-21
            }

            override fun onSkipToPrevious() {
                if (lastPlayed.hasPrevious()) {
                    inPrevious = true
                    val previous = lastPlayed.previous()!!
                    playUri(previous.uri)
                    audioMap[CURRENT] = previous
                }
                super.onSkipToPrevious()
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                super.onPlayFromMediaId(mediaId, extras)

                onPlayFromUri(mediaId!!.toUri(), extras)
            }

            override fun onPlayFromUri(uri: Uri, extras: Bundle?) {
                super.onPlayFromUri(uri, extras)

                if (extras != null) {
                    shuffleMode = extras.getInt(SHUFFLE_BUNDLE_KEY)
                }
                lastPlayed.deleteHeadToCurrent()

                if (audioMap[CURRENT] == null) {
                    playUri(uri)
                    audioMap[CURRENT] = fileDao.getFileByUri(uri)
                } else if (audioMap[CURRENT]!!.uri != uri) {
                    lastPlayed.add(audioMap[CURRENT]!!)
                    playUri(uri)
                    audioMap[CURRENT] = fileDao.getFileByUri(uri)
                }
            }

            private fun playUri(uri: Uri) {
                with(mediaPlayer) {
                    stop()
                    reset()
                    setDataSource(baseContext, uri)
                    prepare()
                }
                onPlay()
            }
        }

        playbackStateBuilder = PlaybackStateCompat.Builder()
        playbackState =
            playbackStateBuilder.setActions(
                PlaybackStateCompat.ACTION_PLAY
                        or PlaybackStateCompat.ACTION_PAUSE
                        or PlaybackStateCompat.ACTION_STOP
            )
                .build()
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {
            setPlaybackState(playbackState)
            setCallback(mediaSessionCallback)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken

        lastPlayed = LastItemsQueue(MAX_LAST_PLAYED)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mediaSessionCallback.onSkipToNext()
    }

    private fun getAllMedia() {
        audioList.clear()
        audioList.addAll(fileDao.getAllFiles())
    }

    private fun setRandomNext() {
        var nextIndex = Random.Default.nextInt(audioList.size)
        while (audioList[nextIndex].uri == audioMap[CURRENT]!!.uri) {
            nextIndex = (nextIndex + 1) % audioList.size
        }
        audioMap[NEXT] = audioList[nextIndex]
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot = BrowserRoot(ROOT, null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()

        var mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
        audioList.forEach { audioItem ->
            mediaItems.add(
                MediaBrowserCompat.MediaItem(
                    MediaDescriptionCompat.Builder()
                        .setMediaId(audioItem.uri.toString())
                        .setTitle(audioItem.title)
                        .setSubtitle(audioItem.artist)
                        .setMediaUri(audioItem.uri)
                        .build(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            )
        }

        result.sendResult(mediaItems)
    }

    private fun showNotification() {
        val notification = getNotification()
        startForeground(SERVICE_ID, notification)
    }

    private fun getNotification(): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0)
            )
            setSilent(true)

            // TODO: 01-Aug-21
            setLargeIcon(
                ResourcesCompat.getDrawable(resources, R.drawable.defauld_album, null)?.toBitmap()
            )
            setContentTitle("Placeholder title")
            setContentText("Placeholder artist - Placeholder album")
            setSmallIcon(R.drawable.defauld_album)

            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    applicationContext,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            addAction(
                NotificationCompat.Action(
                    R.drawable.pause,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@PlayerService,
                        PlaybackStateCompat.ACTION_PAUSE
                    )
                )
            )
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.setContentIntent(pendingIntent)

        return notificationBuilder.build()
    }

    companion object {
        const val ROOT = "/"

        private const val MAX_LAST_PLAYED = 10

        private const val LOG_TAG = "THIS_PLAYER"
        private const val SERVICE_ID = 315465
        private const val NOTIFICATION_CHANNEL = "com.rammanhmr.audioplayer"
        private const val CHANNEL_ID = "CHANNEL_ID"

        //audioMap keys
        private const val CURRENT = 1
        private const val NEXT = 2
        private const val ALTERNATIVE = 3

        //shuffle modes
        const val SHUFFLE_BUNDLE_KEY = "SHUFFLE_KEY"
        const val RANDOM = 1
        const val BY_SCORES = 2
    }
}