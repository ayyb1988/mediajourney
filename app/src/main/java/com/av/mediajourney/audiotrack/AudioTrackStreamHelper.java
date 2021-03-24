package com.av.mediajourney.audiotrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AudioTrackStreamHelper {

    private static final String TAG = "AudioTrackStreamHelper";
    private AudioTrack audioTrack;
    private int sampleRateInHz;
    private int channels;
    private int audioFormat;
    private int bufferSize;
    private int mode = -1;

    private boolean hasPcmFile = false;
    private File pcmFile;
    private Thread audioTrackThread;


    public void initAudioTrackParams(String path) {
        sampleRateInHz = 44100;
        channels = AudioFormat.CHANNEL_OUT_MONO;//错误的写成了CHANNEL_IN_MONO
        audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channels, audioFormat);

        pcmFile = new File(path);//"raw.pcm"
        if (pcmFile.exists()) {
            hasPcmFile = true;
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

    public boolean isHasPcmFile() {
        return hasPcmFile;
    }

    public void play() {
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


    public void pausePlay() {
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

    public void destroy() {
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
