package com.ramanhmr.audioplayer.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.ArrayMap
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.ramanhmr.audioplayer.R
import com.ramanhmr.audioplayer.daos.FileDao
import com.ramanhmr.audioplayer.entities.AudioFile
import com.ramanhmr.audioplayer.repositories.ArtRepository
import com.ramanhmr.audioplayer.ui.MainActivity
import com.ramanhmr.audioplayer.utils.LastItemsQueue
import com.ramanhmr.audioplayer.utils.MetadataUtils
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension
import kotlin.random.Random

@KoinApiExtension
class PlayerService : MediaBrowserServiceCompat(), MediaPlayer.OnCompletionListener {
    private val fileDao: FileDao by inject()
    private val artRepository: ArtRepository by inject()
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionCallback: MediaSessionCompat.Callback
    private lateinit var playbackState: PlaybackStateCompat
    private lateinit var playbackStateBuilder: PlaybackStateCompat.Builder
    private lateinit var metadataBuilder: MediaMetadataCompat.Builder
    private var initialized = false
    private var audioList = arrayListOf<AudioFile>()
    private val mediaPlayer: MediaPlayer by lazy {
        MediaPlayer().apply {
            setOnCompletionListener { mediaSessionCallback.onSkipToNext() }
        }.also {
            initialized = true
        }
    }
    private val audioMap = ArrayMap<Int, AudioFile>()
    private val lastPlayed = LastItemsQueue<AudioFile>(MAX_LAST_PLAYED)
    private var shuffleMode = RANDOM
    private var inPrevious = false
    private var playbackSpeed = 1F

    override fun onCreate() {
        super.onCreate()

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getAllMedia()
        }

        notificationManager = getNotificationManager()
        mediaSessionCallback = getSessionCallbacks()
        playbackStateBuilder = PlaybackStateCompat.Builder()
        playbackState =
            playbackStateBuilder
                .setState(
                    PlaybackStateCompat.STATE_STOPPED,
                    0,
                    playbackSpeed
                ).setActions(PlaybackStateCompat.ACTION_PLAY)
                .build()
        metadataBuilder = MediaMetadataCompat.Builder()
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {
            setPlaybackState(playbackState)
            setCallback(mediaSessionCallback)
            isActive = true
        }
        sessionToken = mediaSession.sessionToken
    }

    private fun getSessionCallbacks() = object : MediaSessionCompat.Callback() {
        override fun onCustomAction(action: String, extras: Bundle?) {
            super.onCustomAction(action, extras)

            if (action == REQUEST_STATE && initialized) {
                playbackState =
                    playbackStateBuilder
                        .setState(
                            if (mediaPlayer.isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                            mediaPlayer.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_STOP
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                    or if (mediaPlayer.isPlaying) PlaybackStateCompat.ACTION_PAUSE else PlaybackStateCompat.ACTION_PLAY
                        )
                        .build()
                mediaSession.setPlaybackState(playbackState)
            }
        }

        override fun onPlay() {
            super.onPlay()

            mediaPlayer.start()

            if (mediaPlayer.isPlaying) {
                playbackState =
                    playbackStateBuilder
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            mediaPlayer.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PAUSE
                                    or PlaybackStateCompat.ACTION_STOP
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        )
                        .build()
                mediaSession.setPlaybackState(playbackState)
            }
            startForegroundService(Intent(applicationContext, PlayerService::class.java))
            showNotification(PAUSE)
            // TODO: 01-Aug-21
        }

        override fun onPause() {
            super.onPause()

            mediaPlayer.pause()

            if (!mediaPlayer.isPlaying) {
                playbackState =
                    playbackStateBuilder
                        .setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            mediaPlayer.currentPosition.toLong(),
                            playbackSpeed
                        )
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY
                                    or PlaybackStateCompat.ACTION_STOP
                                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        )
                        .build()
                mediaSession.setPlaybackState(playbackState)
            }
            showNotification(PLAY)
        }

        override fun onStop() {
            super.onStop()

            mediaPlayer.stop()
            mediaPlayer.release()
            playbackState =
                playbackStateBuilder
                    .setState(
                        PlaybackStateCompat.STATE_STOPPED,
                        0,
                        playbackSpeed
                    )
                    .setActions(
                        PlaybackStateCompat.ACTION_PLAY
                                or PlaybackStateCompat.ACTION_STOP
                                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                    .build()
            mediaSession.setPlaybackState(playbackState)
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
                        audioMap[CURRENT] = audioMap[NEXT]
                        onPlay()
                        lastPlayed.add(audioMap[CURRENT]!!)
                    }
                    BY_SCORES -> {
                    }
                }
            }
            updateMetadata()
        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()

            if (mediaPlayer.currentPosition < TIME_RESTART) {
                if (lastPlayed.hasPrevious()) {
                    inPrevious = true
                    audioMap[CURRENT] = lastPlayed.previous()
                    playUri(audioMap[CURRENT]!!.uri)
                    updateMetadata()
                }
            } else {
                mediaPlayer.pause()
                mediaPlayer.seekTo(0)
                onPlay()
            }
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)
            mediaPlayer.seekTo(pos, MediaPlayer.SEEK_CLOSEST)
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
                audioMap[CURRENT] = fileDao.getFileByUri(uri)
                playUri(uri)
                lastPlayed.add(audioMap[CURRENT]!!)
            } else if (audioMap[CURRENT]!!.uri != uri) {
                audioMap[CURRENT] = fileDao.getFileByUri(uri)
                playUri(uri)
                lastPlayed.add(audioMap[CURRENT]!!)
            }
            updateMetadata()
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

    private fun getNotificationManager() =
        NotificationManagerCompat.from(applicationContext).apply {
            createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    NOTIFICATION_CHANNEL,
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
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
        while (audioList[nextIndex].uri == audioMap[CURRENT]?.uri) {
            nextIndex = (nextIndex + 1) % audioList.size
        }
        audioMap[NEXT] = audioList[nextIndex]
    }

    private fun updateMetadata() {
        with(audioMap[CURRENT]!!) {
            val metadata = metadataBuilder.putString(MetadataUtils.ID, id.toString())
                .putString(MetadataUtils.TITLE, title)
                .putString(MetadataUtils.ARTIST, artist)
                .putString(MetadataUtils.ALBUM, album)
                .putString(MetadataUtils.URI, uri.toString())
                .putLong(MetadataUtils.DURATION, duration.toLong())
                .build()
            mediaSession.setMetadata(metadata)
        }
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
                        .setMediaId(audioItem.id.toString())
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

    private fun showNotification(buttonKey: Int) {
        val notification = getNotification(buttonKey)
        startForeground(SERVICE_ID, notification)
        notificationManager.notify(SERVICE_ID, notification)
    }

    private fun getNotification(buttonKey: Int): Notification {
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )
            setSilent(true)
            priority = NotificationCompat.PRIORITY_MAX
            setCategory(MEDIA_SESSION_SERVICE)

            setLargeIcon(artRepository.getAlbumArt(audioMap[CURRENT]!!.uri, this@PlayerService))
            setContentTitle("${audioMap[CURRENT]?.title}")
            setContentText("${audioMap[CURRENT]?.artist} - ${audioMap[CURRENT]?.album}")
            setSmallIcon(R.drawable.default_album)

            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    applicationContext,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            when (buttonKey) {
                PAUSE -> {
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
                PLAY -> {
                    addAction(
                        NotificationCompat.Action(
                            R.drawable.play,
                            "Play",
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this@PlayerService,
                                PlaybackStateCompat.ACTION_PLAY
                            )
                        )
                    )
                }
            }
            addAction(
                NotificationCompat.Action(
                    R.drawable.next,
                    "Next",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@PlayerService,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.setContentIntent(pendingIntent)

        return notificationBuilder.build()
    }

    companion object {
        const val ROOT = "/"
        const val REQUEST_STATE = "Request playback state"

        const val MAX_LAST_PLAYED = 10
        private const val TIME_RESTART = 5000L

        private const val LOG_TAG = "THIS_PLAYER"
        private const val SERVICE_ID = 315465
        private const val NOTIFICATION_CHANNEL = "com.rammanhmr.audioplayer"
        private const val CHANNEL_ID = "CHANNEL_ID"

        //notification state keys
        private const val PLAY = 1
        private const val PAUSE = 2

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