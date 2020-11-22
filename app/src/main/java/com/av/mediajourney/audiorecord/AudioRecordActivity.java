package com.av.mediajourney.audiorecord;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.av.mediajourney.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class AudioRecordActivity extends Activity {
    private static final String TAG = "AudioRecordActivity";

    @BindView(R.id.tvStartRecord)
    TextView tvStartRecord;
    @BindView(R.id.tvEndRecord)
    TextView tvEndRecord;
    @BindView(R.id.tvPCMPath)
    TextView tvPCMPath;
    @BindView(R.id.tvPcmToWav)
    TextView tvPcmToWav;

    private AudioRecord audioRecord;
    private int bufferSize;
    private boolean isRecording = false;
    private File pcmFile;

    private int sampleRateInHz;
    private int channelConfig;
    private int audioFormat;

    private String pcmTextStr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record_layout);
        ButterKnife.bind(this);

        createAudioRecord();
        initPCMFile();
    }


    private void createAudioRecord() {
        sampleRateInHz = 44100;
        channelConfig = AudioFormat.CHANNEL_IN_MONO;
        audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz, channelConfig, audioFormat, bufferSize);

        //audioRecord的状态
        int state = audioRecord.getState();
        Log.d(TAG, "createAudioRecord: state=" + state + " bufferSize=" + bufferSize);
        if (AudioRecord.STATE_INITIALIZED != state) {
            new Exception("AudioRecord无法初始化，请检查录制权限或者是否其他app没有释放录音器");
        }
    }

    private void initPCMFile() {
        pcmFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "raw.pcm");
        Log.d(TAG, "initPCMFile: pcmFile=" + pcmFile);

        pcmTextStr = "pcm地址: " + pcmFile.getAbsolutePath();
        tvPCMPath.setText(pcmTextStr);
    }

    @OnClick({R.id.tvStartRecord, R.id.tvEndRecord, R.id.tvPcmToWav})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tvStartRecord:
                startRecord();
                break;
            case R.id.tvEndRecord:
                stopRecord();
                break;
            case R.id.tvPcmToWav:
                long startTime = System.currentTimeMillis();
                convertPcmToWav();
                long time = System.currentTimeMillis() - startTime;
                Log.d(TAG, "convertPcmToWav: time=" + time);
                break;
        }
    }

    private void startRecord() {

        if (pcmFile.exists()) {
            pcmFile.delete();
        }

        isRecording = true;
        final byte[] buffer = new byte[bufferSize];
        audioRecord.startRecording();
        tvStartRecord.setText("录制中...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(pcmFile);
                    if (fileOutputStream != null) {
                        while (isRecording) {
                            int readStatus = audioRecord.read(buffer, 0, bufferSize);
                            Log.d(TAG, "run: readStatus=" + readStatus);
                            fileOutputStream.write(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "run: ", e);
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    private void stopRecord() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
        }
        tvStartRecord.setText("开始录制");
    }

    private File wavFile;

    private void convertPcmToWav() {
        wavFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC) , "convert.wav");
        if (wavFile.exists()) {
            wavFile.delete();
        }

        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;

        try {
            fileInputStream = new FileInputStream(pcmFile);
            fileOutputStream = new FileOutputStream(wavFile);

            long audioByteLen = fileInputStream.getChannel().size();
            long wavByteLen = audioByteLen + 36;

            addWavHeader(fileOutputStream, audioByteLen, wavByteLen, sampleRateInHz,
                    channelConfig, audioFormat);
            byte[] buffer = new byte[bufferSize];
            while (fileInputStream.read(buffer) != -1) {
                fileOutputStream.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                tvPCMPath.setText(pcmTextStr+"\n wav地址： "+wavFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void addWavHeader(FileOutputStream fileOutputStream, long audioByteLen, long wavByteLen, int sampleRateInHz, int channelConfig, int audioFormat) {
        byte[] header = new byte[44];

        // RIFF/WAVE header chunk
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (wavByteLen & 0xff);
        header[5] = (byte) ((wavByteLen >> 8) & 0xff);
        header[6] = (byte) ((wavByteLen >> 16) & 0xff);
        header[7] = (byte) ((wavByteLen >> 24) & 0xff);

        //WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';

        // 'fmt ' chunk 4 个字节
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 4 bytes: size of 'fmt ' chunk（格式信息数据的大小 header[20] ~ header[35]）
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // format = 1 编码方式
        header[20] = 1;
        header[21] = 0;
        // 声道数目
        int channelSize = channelConfig == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
        header[22] = (byte) channelSize;
        header[23] = 0;
        // 采样频率
        header[24] = (byte) (sampleRateInHz & 0xff);
        header[25] = (byte) ((sampleRateInHz >> 8) & 0xff);
        header[26] = (byte) ((sampleRateInHz >> 16) & 0xff);
        header[27] = (byte) ((sampleRateInHz >> 24) & 0xff);
        // 每秒传输速率
        long byteRate = audioFormat * sampleRateInHz * channelSize;
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // block align 数据库对齐单位，每个采样需要的字节数
        header[32] = (byte) (2 * 16 / 8);
        header[33] = 0;
        // bits per sample 每个采样需要的 bit 数
        header[34] = 16;
        header[35] = 0;

        //data chunk
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        // pcm字节数
        header[40] = (byte) (audioByteLen & 0xff);
        header[41] = (byte) ((audioByteLen >> 8) & 0xff);
        header[42] = (byte) ((audioByteLen >> 16) & 0xff);
        header[43] = (byte) ((audioByteLen >> 24) & 0xff);

        Log.d(TAG, "addWavHeader: "+"header[40]="+header[40]+"header[41]="+header[41]+"header[42]="+header[42]+"header[43]="+header[43]+" audioByteLen="+audioByteLen);

        try {
            fileOutputStream.write(header, 0, 44);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord != null) {
            audioRecord.release();
        }
        audioRecord = null;
    }
}
