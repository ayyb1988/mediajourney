package com.av.mediajourney.audiotrack;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.av.mediajourney.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AudioTrackStreamActivity extends Activity {

    private static final String TAG = "AudioTrackStream";
    @BindView(R.id.tv_play)
    TextView tvPlay;
    @BindView(R.id.tv_pause)
    TextView tvStop;

    private AudioTrack audioTrack;
    private int sampleRateInHz;
    private int channels;
    private int audioFormat;
    private int bufferSize;
    private int mode = -1;

    private boolean hasPcmFile = false;
    private File pcmFile;
    private Thread audioTrackThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track_layout);
        ButterKnife.bind(this);

        initAudioTrackParams();
    }

    private void initAudioTrackParams() {
        sampleRateInHz = 44100;
        channels = AudioFormat.CHANNEL_OUT_MONO;//错误的写成了CHANNEL_IN_MONO
        audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channels, audioFormat);

        pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "convert.wav");//"raw.pcm"
        if (pcmFile.exists()) {
            hasPcmFile = true;
        }
    }


    @OnClick({R.id.tv_play, R.id.tv_pause})
    public void onViewClicked(View view) {
        if (!hasPcmFile) {
            Toast.makeText(this, "PCM文件不存在，请先录制", Toast.LENGTH_SHORT).show();
            return;
        }
        switch (view.getId()) {
            case R.id.tv_play:
                play();
                break;
            case R.id.tv_pause:
                pausePlay();
                break;
        }
    }


    private int initAudioTrackWithMode(int mode, int bufferSize) {
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack.setPlaybackPositionUpdateListener(null);
            audioTrack = null;
        }

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channels, audioFormat, bufferSize, mode);
        if (audioTrack != null) {
            Log.i(TAG, "initAudioTrackWithMode: state="+audioTrack.getState()+" playState="+audioTrack.getPlayState());
            return audioTrack.getState();
        }
        return AudioTrack.STATE_UNINITIALIZED;
    }

    private void play() {
        releaseAudioTrack();

        int state = initAudioTrackWithMode(AudioTrack.MODE_STREAM, bufferSize);
        if (state == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "run: state is uninit");
            return;
        }

        audioTrackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(pcmFile);
                    byte[] buffer = new byte[bufferSize / 2];
                    int readCount;
                    Log.d(TAG, "run: ThreadId=" + Thread.currentThread() + " playState=" + audioTrack.getPlayState());
                    //stream模式，可以先调用play
                    audioTrack.play();
                    while (fileInputStream.available() > 0) {
                        readCount = fileInputStream.read(buffer);
                        if (readCount == AudioTrack.ERROR_BAD_VALUE || readCount == AudioTrack.ERROR_INVALID_OPERATION) {
                            continue;
                        }
                        if (audioTrack == null) {
                            return;
                        } else {
                            Log.i(TAG, "run: audioTrack.getState()" + audioTrack.getState() + " audioTrack.getPlayState()=" + audioTrack.getPlayState());
                        }
//                        audioTrack.getPlayState()
                        //一次一次的写入pcm数据到audioTrack.由于是在子线程中进行write，快速连续点击可能主线程触发了stop或者release，导致子线程write异常：IllegalStateException: Unable to retrieve AudioTrack pointer for write()
                        //所以加playstate的判断
                        if (readCount > 0 && audioTrack != null && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                            audioTrack.write(buffer, 0, readCount);
                        }
                    }

                } catch (IOException | IllegalStateException e) {
                    e.printStackTrace();
                    Log.e(TAG, "play: " + e.getMessage());
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "playWithStreamMode: end  ThreadID=" + Thread.currentThread());
                }
            }
        });
        audioTrackThread.start();
    }


    private void pausePlay() {
        if (audioTrack != null) {
            if (audioTrack.getState() > AudioTrack.STATE_UNINITIALIZED) {
                audioTrack.pause();
            }
            Log.d(TAG, "pausePlay: isPlaying false getPlayState= " + audioTrack.getPlayState());
        }
        if (audioTrackThread != null) {
            audioTrackThread.interrupt();
        }
    }

    private void releaseAudioTrack() {
        if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.stop();
            audioTrack.release();
            Log.d(TAG, "pausePlay: isPlaying false");
        }
        if (audioTrackThread != null) {
            audioTrackThread.interrupt();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        if (audioTrackThread != null) {
            audioTrackThread.interrupt();
            audioTrackThread = null;
        }
    }

}
