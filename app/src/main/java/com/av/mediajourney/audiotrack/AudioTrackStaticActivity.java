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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AudioTrackStaticActivity extends Activity {

    private static final String TAG = "AudioTrackStatic";
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
    private volatile boolean isPlaying;
    private Thread audioTrackThread;
    private boolean isReadying = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track_layout);
        ButterKnife.bind(this);

        initAudioTrackParams();
        initStaticBuff();
    }

    private void initAudioTrackParams() {
        sampleRateInHz = 44100;
        channels = AudioFormat.CHANNEL_OUT_MONO;//错误的写成了CHANNEL_IN_MONO
        audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channels, audioFormat);

        pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "raw.pcm");
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
                play(staicBuff);
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
            long size = 0;
            try {
                //pcm size大小，处于2是因为audioFormat为AudioFormat.ENCODING_PCM_16BIT 16位即2个字节
                size = new FileInputStream(pcmFile).getChannel().size() / 2;
            } catch (IOException e) {
                e.printStackTrace();
            }
            // static模式可以通过设置setNotificationMarkerPosition 和 setPlaybackPositionUpdateListener来判断是否播放完毕，
           // 只针对static有效，stream无效

            audioTrack.setNotificationMarkerPosition((int) size);
            audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack track) {
                    Log.d(TAG, "onMarkerReached: playState=" + track.getPlayState());
                    isPlaying = false;
                }

                @Override
                public void onPeriodicNotification(AudioTrack track) {
                    Log.d(TAG, "onPeriodicNotification: playState=" + track.getPlayState());

                }
            });
        if (audioTrack != null) {
            return audioTrack.getState();
        }
        return AudioTrack.STATE_UNINITIALIZED;
    }

    private byte[] staicBuff;

    private void initStaticBuff() {

        //staic模式是一次读取全部的数据，在play之前要先完成{@link audioTrack.write()}


        if (audioTrackThread != null) {
            audioTrackThread.interrupt();
        }

        audioTrackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileInputStream fileInputStream = null;
                try {
                    //init audioTrack 需要先确定buffersize
                    fileInputStream = new FileInputStream(pcmFile);
                    long size = fileInputStream.getChannel().size();
                    staicBuff = new byte[(int) size];

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(staicBuff.length);
                    int byteValue = 0;
                    long startTime = System.currentTimeMillis();
                    while ((byteValue = fileInputStream.read()) != -1) {
//                        Log.d(TAG, "run: " + byteValue);
                        //耗时操作
                        byteArrayOutputStream.write(byteValue);
                    }
                    Log.d(TAG, "byteArrayOutputStream write Time: " + (System.currentTimeMillis() - startTime));
                    staicBuff = byteArrayOutputStream.toByteArray();

                    isReadying = true;

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "playWithStaicMode: end");
                }
            }
        });
        audioTrackThread.start();

    }

    private void play(byte[] staicBuff) {
        //1. static模式是一次读去pcm到内存，比较耗时，只有读取完之后才可以调用play
        if (!isReadying) {
            Toast.makeText(this, "请稍后", Toast.LENGTH_SHORT).show();
            return;
        }

        //2.如果正在播放中，重复点击播放，则停止当次播放，调用reloadStaticData重新加载数据，然后play
        if (isPlaying) {
            audioTrack.stop();
            audioTrack.reloadStaticData();
            Log.d(TAG, "playWithStaicMode: reloadStaticData");
            audioTrack.play();
            return;
        }
        //3。否则，就先释放audiotrack，然后重新初始化audiotrack进行

        releaseAudioTrack();

        int state = initAudioTrackWithMode(AudioTrack.MODE_STATIC, staicBuff.length);
        if (state == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "run: state is uninit");
            return;
        }
        //4. 把pcm写入audioTrack，然后进行播放
        long startTime = System.currentTimeMillis();
        int result = audioTrack.write(staicBuff, 0, staicBuff.length);
        Log.d(TAG, "audioTrack.write staic: result=" + result+" totaltime="+ (System.currentTimeMillis() - startTime));
        audioTrack.play();
        isPlaying = true;
    }


    private void pausePlay() {
        if (audioTrack != null) {
            if (audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
                audioTrack.pause();
                audioTrack.flush();
            }
            isPlaying = false;
            Log.d(TAG, "pausePlay: isPlaying false");
        }
        if (audioTrackThread != null) {
            audioTrackThread.interrupt();
        }
    }

    private void releaseAudioTrack() {
        if (audioTrack != null && audioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audioTrack.stop();
            audioTrack.release();
            isPlaying = false;
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
