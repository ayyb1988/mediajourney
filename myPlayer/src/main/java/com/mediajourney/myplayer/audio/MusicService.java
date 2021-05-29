package com.mediajourney.myplayer.audio;

import android.os.Binder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import com.mediajourney.myplayer.audio.entity.MusicEntity;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.util.EventLogger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = "MusicService";
    private SimpleExoPlayer exoPlayer;
    private MediaSessionCompat mediaSession;

    /**
     * 当服务收到onCreate（）生命周期回调方法时，它应该执行以下步骤：
     * 1. 创建并初始化media session
     * 2. 设置media session回调
     * 3. 设置media session token
     */
    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        super.onCreate();
        //1. 创建并初始化MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), TAG);

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackStateCompat playbackState = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SEEK_TO)
                .build();
        mediaSession.setPlaybackState(playbackState);

        //2. 设置mediaSession回调
        mediaSession.setCallback(new MyMediaSessionCallBack());

        //3. 设置mediaSessionToken
        setSessionToken(mediaSession.getSessionToken());


        exoPlayer = new SimpleExoPlayer.Builder(getApplicationContext()).build();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        Log.i(TAG, "onGetRoot: clientPackageName=" + clientPackageName + " clientUid=" + clientUid + " pid=" + Binder.getCallingPid()
                + " uid=" + Binder.getCallingUid());
        //返回非空，表示连接成功
        return new BrowserRoot("media_root_id", null);
    }

    //获取音视频信息（这个更应该是在业务层处理事情）
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        Log.i(TAG, "onLoadChildren: parentId=" + parentId);
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if (TextUtils.equals("media_root_id", parentId)) {

        }
        ArrayList<MusicEntity> musicEntityList = getMusicEntityList();

        for (int i = 0; i < musicEntityList.size(); i++) {
            MusicEntity musicEntity = musicEntityList.get(i);

            MediaMetadataCompat metadataCompat = buildMediaMetadata(musicEntity);

            if (i == 0) {
                mediaSession.setMetadata(metadataCompat);
            }

            mediaItems.add(new MediaBrowserCompat.MediaItem(metadataCompat.getDescription(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

            exoPlayer.addMediaItem(MediaItem.fromUri(musicEntity.source));
        }
        //当设置多首歌曲组成队列时报错
        // IllegalStateException: sendResult() called when either sendResult() or sendError() had already been called for: media_root_id
        //原因，之前在for处理了，应该在设置好mediaItems列表后，统一设置result
        result.sendResult(mediaItems);
        Log.i(TAG, "onLoadChildren: addMediaItem");

        initExoPlayerListener();

        exoPlayer.prepare();
        Log.i(TAG, "onLoadChildren: prepare");
    }

    @NotNull
    private MediaMetadataCompat buildMediaMetadata(MusicEntity musicEntity) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, musicEntity.id)
                .putString("__SOURCE__", musicEntity.source)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, musicEntity.album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, musicEntity.artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, musicEntity.duration)
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, musicEntity.genre)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, musicEntity.image)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, musicEntity.title)
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, musicEntity.trackNumber)
                .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, musicEntity.totalTrackCount)
                .build();
    }

    private void initExoPlayerListener() {
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                long currentPosition = exoPlayer.getCurrentPosition();
                long duration = exoPlayer.getDuration();

                //状态改变（播放器内部发生状态变化的回调，
                // 包括
                // 1. 用户触发的  比如： 手动切歌曲、暂停、播放、seek等；
                // 2. 播放器内部触发 比如： 播放结束、自动切歌曲等）

                //该如何通知给ui业务层呐？？好些只能通过回调
                //那有该如何 --》查看源码得知通过setPlaybackState设置
                Log.i(TAG, "onPlaybackStateChanged: currentPosition=" + currentPosition + " duration=" + duration + " state=" + state);

                int playbackState;
                switch (state) {
                    default:
                    case Player.STATE_IDLE:
                        playbackState = PlaybackStateCompat.STATE_NONE;
                        break;
                    case Player.STATE_BUFFERING:
                        playbackState = PlaybackStateCompat.STATE_BUFFERING;
                        break;
                    case Player.STATE_READY:
                        if (exoPlayer.getPlayWhenReady()) {
                            playbackState = PlaybackStateCompat.STATE_PLAYING;
                        } else {
                            playbackState = PlaybackStateCompat.STATE_PAUSED;
                        }
                        break;
                    case Player.STATE_ENDED:
                        playbackState = PlaybackStateCompat.STATE_STOPPED;
                        break;
                }
                //播放器的状态变化，通过mediasession告诉在ui业务层注册的MediaControllerCompat.Callback进行回调

                setPlaybackState(playbackState);
            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.i(TAG, "onPlayerError: error=" + error.getMessage());
                setPlaybackState(PlaybackStateCompat.STATE_ERROR);
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                Log.i(TAG, "onIsPlayingChanged: isPlaying=" + isPlaying);
                if (isPlaying) {
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    setPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }

            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                Log.i(TAG, "onPositionDiscontinuity: reason=" + reason);

            }


        });

        exoPlayer.addAudioListener(new AudioListener() {
            @Override
            public void onVolumeChanged(float volume) {

            }

            @Override
            public void onAudioSessionIdChanged(int audioSessionId) {

            }

            @Override
            public void onAudioAttributesChanged(AudioAttributes audioAttributes) {

            }
        });
        exoPlayer.addAnalyticsListener(new EventLogger(new DefaultTrackSelector()));
    }

    private void setPlaybackState(int playbackState) {
        float speed = exoPlayer.getPlaybackParameters() == null ? 1f : exoPlayer.getPlaybackParameters().speed;

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder().setState(playbackState, exoPlayer.getCurrentPosition(), speed).build());
    }

    private ArrayList<MusicEntity> mediaEntityList = new ArrayList<MusicEntity>();

    @NotNull
    private ArrayList<MusicEntity> getMusicEntityList() {
        if (mediaEntityList != null && mediaEntityList.size() > 0) {
            return mediaEntityList;
        }
        if (mediaEntityList == null) {
            mediaEntityList = new ArrayList<MusicEntity>();
        }
        /**
         *{
         *       "id": "jazz_in_paris",
         *       "title": "Jazz in Paris",
         *       "album": "Jazz & Blues",
         *       "artist": "Media Right Productions",
         *       "genre": "Jazz & Blues",
         *       "source": "https://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3",
         *       "image": "https://storage.googleapis.com/automotive-media/album_art.jpg",
         *       "trackNumber": 1,
         *       "totalTrackCount": 6,
         *       "duration": 103,
         *       "site": "https://www.youtube.com/audiolibrary/music"
         *     }
         */

        MusicEntity musicEntity = new MusicEntity();
        musicEntity.id = "jazz_in_paris";
        musicEntity.title = "Jazz in Paris";
        musicEntity.album = "Jazz & Blues";
        musicEntity.artist = "Media Right Productions";
        musicEntity.genre = "Jazz & Blues";
        musicEntity.source = "https://storage.googleapis.com/automotive-media/Jazz_In_Paris.mp3";
        musicEntity.image = "https://storage.googleapis.com/automotive-media/album_art.jpg";
        musicEntity.trackNumber = 1;
        musicEntity.totalTrackCount = 6;
        musicEntity.duration = 103;
        musicEntity.site = "https://www.youtube.com/audiolibrary/music";

        mediaEntityList.add(musicEntity);


        /**
         * {
         *       "id": "wake_up_02",
         *       "title": "Geisha",
         *       "album": "Wake Up",
         *       "artist": "The Kyoto Connection",
         *       "genre": "Electronic",
         *       "source": "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/02_-_Geisha.mp3",
         *       "image": "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/art.jpg",
         *       "trackNumber": 2,
         *       "totalTrackCount": 13,
         *       "duration": 267,
         *       "site": "http://freemusicarchive.org/music/The_Kyoto_Connection/Wake_Up_1957/"
         *     }
         */

        MusicEntity musicEntity2 = new MusicEntity();
        musicEntity2.id = "wake_up_02";
        musicEntity2.title = "Geisha";
        musicEntity2.album = "Wake Up";
        musicEntity2.artist = "Media Right Productions";
        musicEntity2.genre = "Electronic";
        musicEntity2.source = "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/02_-_Geisha.mp3";
        musicEntity2.image = "https://storage.googleapis.com/uamp/The_Kyoto_Connection_-_Wake_Up/art.jpg";
        musicEntity2.trackNumber = 2;
        musicEntity2.totalTrackCount = 13;
        musicEntity2.duration = 267;
        musicEntity2.site = "http://freemusicarchive.org/music/The_Kyoto_Connection/Wake_Up_1957/";

        mediaEntityList.add(musicEntity2);

        return mediaEntityList;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 用于接收由MediaControl触发的改变，内部封装实现播放器和播放状态的改变
     */
    private class MyMediaSessionCallBack extends MediaSessionCompat.Callback {


        @Override
        public void onPlay() {
            super.onPlay();

            Log.i(TAG, "onPlay: ");
            exoPlayer.play();
        }

        @Override
        public void onPause() {
            super.onPause();

            Log.i(TAG, "onPause: ");
            exoPlayer.pause();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            Log.i(TAG, "onSeekTo: pos=" + pos);

            exoPlayer.seekTo(pos);
        }

        @Override
        public void onStop() {
            super.onStop();
            Log.i(TAG, "onStop: ");
            exoPlayer.stop();
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            super.onAddQueueItem(description);
            Log.i(TAG, "onAddQueueItem: description=" + description);
        }

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
            Log.i(TAG, "onCommand: cmd=" + command);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
            Log.i(TAG, "onPrepare: ");
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);
            Log.i(TAG, "onRemoveQueueItem: description=" + description);
        }

        @Override
        public void onSetPlaybackSpeed(float speed) {
            super.onSetPlaybackSpeed(speed);
            Log.i(TAG, "onSetPlaybackSpeed: speed=" + speed);
            PlaybackParameters playParams = new PlaybackParameters(speed);
            exoPlayer.setPlaybackParameters(playParams);
            //重新设置mediaSession.setPlaybackState 告知 监听者 speed变化
            setPlaybackState(exoPlayer.getPlaybackState());
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            Log.i(TAG, "onSkipToNext: ");
            exoPlayer.next();
            exoPlayer.setPlayWhenReady(true);
            setPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);

            mediaSession.setMetadata(getMediaMetadata(1));
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.i(TAG, "onSkipToPrevious: ");
            exoPlayer.previous();
            exoPlayer.setPlayWhenReady(true);
            setPlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
            mediaSession.setMetadata(getMediaMetadata(0));
        }

        @Override
        public void onSetRepeatMode(int repeatMode) {
            super.onSetRepeatMode(repeatMode);
            Log.i(TAG, "onSetRepeatMode: repeatMode=" + repeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            super.onSetShuffleMode(shuffleMode);
            Log.i(TAG, "onSetShuffleMode: shuffleMode=" + shuffleMode);
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            super.onSetRating(rating);
            Log.i(TAG, "onSetRating: rating=" + rating);
        }
    }

    @NotNull
    private MediaMetadataCompat getMediaMetadata(int i) {
        return buildMediaMetadata(getMusicEntityList().get(i));
    }
}
