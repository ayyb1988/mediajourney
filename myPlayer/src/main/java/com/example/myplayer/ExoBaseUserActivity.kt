package com.example.myplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.AudioListener
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.video.VideoListener


class ExoBaseUserActivity : AppCompatActivity() {

    lateinit var playerView: PlayerView
    lateinit var player: SimpleExoPlayer
    lateinit var playbackListener: PlaybackListener
    lateinit var analyticsListener: EventLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exoplayer_base)
        playerView = findViewById<PlayerView>(R.id.playerview)
        initExoPlayer();
    }

    private fun initExoPlayer() {
        //1. 创建播放器
        player = SimpleExoPlayer.Builder(this).build()
        printCurPlaybackState("init")  //  此时处于STATE_IDLE = 1;

        //2. 播放器和播放器容器绑定
        playerView.player = player

        //3. 设置数据源
        //音频
        val mediaItem = MediaItem.fromUri(" https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
        player.setMediaItem(mediaItem)

        //视频
        val mediaItemMP4 = MediaItem.fromUri(" https://media.vued.vanthink.cn/CJ7%20-%20Trailer.mp4")
        //使用addMediaItem可以实现多个音视频源组成的播放列表，播放完一个之后，自动切换下一个
        player.addMediaItem(mediaItemMP4)


        printCurPlaybackState("setMediaItem") //  此时处于STATE_IDLE = 1;

        //4.当Player处于STATE_READY状态时，进行播放
        player.playWhenReady = true

        //5. 设置listener
        playbackListener = PlaybackListener()
        player.addListener(playbackListener)
        player.addAudioListener(playbackListener)
        player.addVideoListener(playbackListener)

        //通过AnalyticsListener可以输出更多信息
        analyticsListener = EventLogger(DefaultTrackSelector())
        player.addAnalyticsListener(analyticsListener)

        //6. 调用prepare开始加载准备数据，该方法时异步方法，不会阻塞ui线程
        player.prepare()
        printCurPlaybackState("prepare") //  此时处于 STATE_BUFFERING = 2;

    }


    class PlaybackListener : Player.EventListener, AudioListener, VideoListener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val stateString: String
            stateString = when (playbackState) {
                ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -" //播放列表存在时播放最后一个播放完成才会回掉该方法
                else -> "UNKNOWN_STATE             -"
            }
            Log.d("ExoBaseUserActivity", "changed state to $stateString")
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            Log.d("ExoBaseUserActivity", "onPlayerError $error")
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.d("ExoBaseUserActivity", "onIsPlayingChanged isPlaying= $isPlaying")
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            Log.d("ExoBaseUserActivity", "onRepeatModeChanged repeatMode=$repeatMode")
        }

        /**
         * {@link com.google.android.exoplayer2.Player.DiscontinuityReason}
         */
        override fun onPositionDiscontinuity(reason: Int) {
            //当有播放队列切换音频/视频时 会回掉该API，其中reason为Player.DISCONTINUITY_REASON_PERIOD_TRANSITION
            Log.d("ExoBaseUserActivity", "onPositionDiscontinuity reason=$reason")
        }

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            Log.d("ExoBaseUserActivity", "onAudioSessionIdChanged--sessionId=" + audioSessionId)
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            Log.d("ExoBaseUserActivity", "onAudioAttributesChanged--audioAttributes=" + audioAttributes.toString())
        }

        override fun onVolumeChanged(volume: Float) {
            Log.d("ExoBaseUserActivity", "onVolumeChanged--volume=" + volume)
        }

        override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
            Log.d("ExoBaseUserActivity", "onSkipSilenceEnabledChanged--skipSilenceEnabled=" + skipSilenceEnabled)
        }

        override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
            Log.d("ExoBaseUserActivity", "onVideoSizeChanged--width=" + width + " height=" + height + " unappliedRotationDegrees=" + unappliedRotationDegrees + " pixelWidthHeightRatio=" + pixelWidthHeightRatio)
        }

        override fun onSurfaceSizeChanged(width: Int, height: Int) {
            Log.d("ExoBaseUserActivity", "onSurfaceSizeChanged--width=" + width + " height=" + height)
        }

        override fun onRenderedFirstFrame() {
            Log.d("ExoBaseUserActivity", "onRenderedFirstFrame")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.removeAnalyticsListener(analyticsListener)
        player.removeListener(playbackListener)
        player.removeAudioListener(playbackListener)
        player.removeVideoListener(playbackListener)

        player.release()
    }

    private fun printCurPlaybackState(s: String) {
        Log.d("ExoBaseUserActivity", "printCurPlaybackState: player.playbackState=" + player.playbackState + " stage=" + s)
    }
}