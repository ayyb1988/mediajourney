package com.mediajourney.myplayer.audio;

import android.app.Activity;
import android.content.ComponentName;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.mediajourney.myplayer.R;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExoSimpleAudioPlayerActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "ExoSimpleAudioPlayerActivity";
    private MediaBrowserCompat mediaBrowser;
    private MediaBrowserCompat.ConnectionCallback mConnectionCallbacks = new MyConnectionCallback();
    private MediaControllerCompat.Callback mMediaControllerCallback;
    private MediaBrowserCompat.SubscriptionCallback mSubscriptionCallback;

    private TextView titleView, artistView;
    private Button playButton;
    private ImageView iconView;
    private SeekBar mSeekbar;
    private TextView startTextView, endTextView;
    private Button prevView, nextView, speedView, fadeinoutView;
    private Button cacheView, audiofocusView, backgroundplayView, effectView;

    private MediaControllerCompat mediaController;
    private boolean durationSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_audio);
        titleView = findViewById(R.id.title);
        artistView = findViewById(R.id.artist);
        playButton = findViewById(R.id.play);
        iconView = findViewById(R.id.icon);
        mSeekbar = findViewById(R.id.seekbar);
        startTextView = findViewById(R.id.startText);
        endTextView = findViewById(R.id.endText);
        prevView = findViewById(R.id.prev);
        nextView = findViewById(R.id.next);
        speedView = findViewById(R.id.speed);
        fadeinoutView = findViewById(R.id.fadeinout);

        cacheView = findViewById(R.id.cache);
        audiofocusView = findViewById(R.id.audiofocus);
        backgroundplayView = findViewById(R.id.backgroundplay);
        effectView = findViewById(R.id.effect);

        playButton.setOnClickListener(this);
        prevView.setOnClickListener(this);
        nextView.setOnClickListener(this);
        speedView.setOnClickListener(this);
        fadeinoutView.setOnClickListener(this);

        cacheView.setOnClickListener(this);
        audiofocusView.setOnClickListener(this);
        backgroundplayView.setOnClickListener(this);
        effectView.setOnClickListener(this);


        //mConnectionCallbacks 是C-S连接的callback
        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MusicService.class),
                mConnectionCallbacks, null);

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                int max = seekBar.getMax();
                Log.i(TAG, "onStopTrackingTouch: progress=" + progress + " max=" + max);
                if (mediaController != null) {
                    mediaController.getTransportControls().seekTo(progress);
                }

            }
        });
    }

    //MediaBrowser执行两个重要功能：它连接到MediaBrowserService，连接后，将为您的UI创建MediaController。


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
        //发出C-S连接请求 创建MusicService，收到onGetRoot回调值不为空说明建立连接成功--》然后触发MyConnectionCallback的回调onConnected
        mediaBrowser.connect();
//        subscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop: ");
        mediaBrowser.disconnect();
    }


    private void subscribe() {
        String mediaId = mediaBrowser.getRoot();
        mediaBrowser.unsubscribe(mediaId);
        if (mSubscriptionCallback == null) {
            //mediaBrowser 和mServiceBinderImpl建立联系
            mSubscriptionCallback = new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    super.onChildrenLoaded(parentId, children);
                    Log.i(TAG, "onChildrenLoaded: parentId=" + parentId + " children=" + children);

                    if (children != null && children.size() > 0) {
                        updateShowMediaInfo(children.get(0).getDescription());
                    }
                }


                @Override
                public void onError(@NonNull String parentId) {
                    super.onError(parentId);
                    Log.i(TAG, "onError: parentId=" + parentId);
                }
            };
        }
        mediaBrowser.subscribe(mediaId, mSubscriptionCallback);
    }

    private void updateShowMediaInfo(MediaDescriptionCompat description) {
        if (description == null) return;

        titleView.setText(description.getTitle());
        artistView.setText(description.getSubtitle());

        Glide.with(ExoSimpleAudioPlayerActivity.this).load(description.getIconUri().toString()).into(iconView);
        Uri mediaUri = description.getMediaUri();
        Uri iconUri = description.getIconUri();
        Log.i(TAG, "onChildrenLoaded: title=" + description.getTitle() + " subtitle=" + description.getSubtitle()
                + " mediaUri=" + mediaUri + " iconUri=" + iconUri);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.play) {
            PlaybackStateCompat playbackState = mediaController.getPlaybackState();
            int state = playbackState.getState();
            Log.i(TAG, "onClick: state=" + state);
            //通过 mediaController.getTransportControls 触发MediaSessionCompat.Callback回调--》进行播放控制
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                mediaController.getTransportControls().pause();
            } else {
                mediaController.getTransportControls().play();
            }
        } else if (id == R.id.prev) {
            if (mediaController != null) {
                mediaController.getTransportControls().skipToPrevious();
            }
        } else if (id == R.id.next) {
            if (mediaController != null) {
                mediaController.getTransportControls().skipToNext();
            }
        } else if (id == R.id.speed) {
            if (mediaController != null) {
                float speed = getSpeed();
                speedView.setText("倍速 " + speed);
                mediaController.getTransportControls().setPlaybackSpeed(speed);
            }
        } else if (id == R.id.fadeinout) {
            if (mediaController != null) {
                mediaController.getTransportControls().skipToNext();
            }
        } else if (id == R.id.cache) {

        } else if (id == R.id.audiofocus) {

        } else if (id == R.id.backgroundplay) {

        } else if (id == R.id.effect) {

        }

    }

    float[] speedArray = new float[]{0.5f, 1f, 1.5f, 2f};
    int curSpeedIndex = 1;

    private float getSpeed() {
        if (curSpeedIndex > 3) {
            curSpeedIndex = 0;
        }
        return speedArray[curSpeedIndex++];
    }

    public class MyMediaControllerCallback extends MediaControllerCompat.Callback {


        //这里的回调，只有用户触发的才会有相应的回调。
        //播放结束 这里没有
        //ExoPlayer getDuration : https://stackoverflow.com/questions/35298125/exoplayer-getduration
        // Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            Log.i(TAG, "onPlaybackStateChanged: state=" + state.getState());
            if (PlaybackStateCompat.STATE_PLAYING == state.getState()) {
                playButton.setText("暂停");
            } else {
                playButton.setText("播放");
            }
            updatePlaybackState(state);

            MediaMetadataCompat metadata = mediaController.getMetadata();
            updateDuration(metadata);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            durationSet = false;
            Log.i(TAG, "onMetadataChanged: metadata=" + metadata.toString());
            updateDuration(metadata);

        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
            Log.i(TAG, "onSessionDestroyed: ");
        }

        @Override
        public void onSessionReady() {
            super.onSessionReady();
            Log.i(TAG, "onSessionReady: ");
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
            Log.i(TAG, "onQueueChanged: ");
        }

        @Override
        public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
            Log.i(TAG, "onAudioInfoChanged: ");
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
            Log.i(TAG, "onSessionEvent: ");
        }

    }

    public class MyConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            super.onConnected();
            Log.i(TAG, "onConnected: MyConnectionCallback");

            //MediaBrowser和MediaBrowerService建立连接之后会回调该方法
            MediaSessionCompat.Token sessionToken = mediaBrowser.getSessionToken();

            //建立连接之后再创建MediaController
            mediaController = new MediaControllerCompat(ExoSimpleAudioPlayerActivity.this, sessionToken);

            MediaControllerCompat.setMediaController(ExoSimpleAudioPlayerActivity.this, mediaController);

            subscribe();
            //MediaController发送命令
            buildTransportControls();
            if (mMediaControllerCallback == null) {
                //这个callback 是Controller的callback，即用户触发了播放、暂停，后发生状态变化的回调。
                //像播放结束、自动切歌，则无法收到该回调（那该如何处理呐？）
                mMediaControllerCallback = new MyMediaControllerCallback();
            }

            mediaController.registerCallback(mMediaControllerCallback);

            PlaybackStateCompat state = mediaController.getPlaybackState();
            updatePlaybackState(state);
            updateProgress();
            if (state != null && (state.getState() == PlaybackStateCompat.STATE_PLAYING ||
                    state.getState() == PlaybackStateCompat.STATE_BUFFERING)) {
                scheduleSeekbarUpdate();
            }

            //通过mediaController获取MediaMetadataCompat
            MediaMetadataCompat metadata = mediaController.getMetadata();
            updateDuration(metadata);
        }


        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
        }
    }

    private void updateDuration(MediaMetadataCompat metadata) {
        if (metadata != null && !durationSet) {

            int duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            Log.i(TAG, "updateDuration: duration=" + duration);
            if (duration > 0) {
                mSeekbar.setMax(duration);
                endTextView.setText(DateUtils.formatElapsedTime(duration));
                durationSet = true;
            }
        }
    }

    private final ScheduledExecutorService mExecutorService =
            Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;
    private final Handler mHandler = new Handler();
    private PlaybackStateCompat mLastPlaybackState;

    private void updateProgress() {
        if (mLastPlaybackState != null) {
            MediaMetadataCompat metadata = mediaController.getMetadata();
            updateDuration(metadata);

            Log.i(TAG, "updateProgress: mLastPlaybackState.getPosition()=" + mLastPlaybackState.getPosition()
                    + " getPlaybackState=" + mLastPlaybackState.getPlaybackState() + " getState=" + mLastPlaybackState.getState()
                    + " getPlaybackSpeed=" + mLastPlaybackState.getPlaybackSpeed());
            int curPos = (int) mLastPlaybackState.getPosition();
            if (mLastPlaybackState.getState() == PlaybackStateCompat.STATE_PLAYING) {
                // Calculate the elapsed time between the last position update and now and unless
                // paused, we can assume (delta * speed) + current position is approximately the
                // latest position. This ensure that we do not repeatedly call the getPlaybackState()
                // on MediaControllerCompat.
                long timeDelta = SystemClock.elapsedRealtime() -
                        mLastPlaybackState.getLastPositionUpdateTime();
                curPos += (int) timeDelta * mLastPlaybackState.getPlaybackSpeed();
            }
            curPos = curPos / 1000;
            mSeekbar.setProgress(curPos);
            startTextView.setText(DateUtils.formatElapsedTime(curPos));
        }
    }

    private final Runnable mUpdateProgressTask = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    private void scheduleSeekbarUpdate() {
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(mUpdateProgressTask);
                        }
                    }, 100,
                    1000, TimeUnit.MILLISECONDS);
        }
    }

    private void updatePlaybackState(PlaybackStateCompat state) {
        mLastPlaybackState = state;
        Log.i(TAG, "updatePlaybackState: state=" + state.getState() + " position=" + mLastPlaybackState.getPosition() + " mLastPlaybackState=" + mLastPlaybackState);

        long activeQueueItemId = state.getActiveQueueItemId();
        MediaMetadataCompat metadata = mediaController.getMetadata();
        MediaDescriptionCompat description = null;
        if (metadata != null) {
            description = metadata.getDescription();
            if (description != null) {
                CharSequence title = description.getTitle();
                String mediaId = description.getMediaId();
                CharSequence subtitle = description.getSubtitle();
                Uri mediaUri = description.getMediaUri();
                Uri iconUri = description.getIconUri();
                CharSequence description1 = description.getDescription();
                Bundle extras = description.getExtras();

                Log.i(TAG, "updatePlaybackState: title=" + title + " mediaId=" + mediaId
                        + " subtitle=" + subtitle + " mediaUri=" + mediaUri + " iconUri=" + iconUri
                        + " decription1=" + description1 + " extras=" + extras
                        + " activeQueueItemId=" + activeQueueItemId + " state.getState()=" + state.getState());
            }
        }

        if (state.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS || state.getState() == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT) {
            updateShowMediaInfo(description);
        }

        scheduleSeekbarUpdate();

    }

    private void buildTransportControls() {

    }
}
