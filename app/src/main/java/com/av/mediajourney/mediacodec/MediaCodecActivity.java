package com.av.mediajourney.mediacodec;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.av.mediajourney.R;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MediaCodecActivity extends Activity {
    private static final String TAG = "MediaCodecActivity";
    @BindView(R.id.tv_mediacodec_print_info)
    TextView tvMediacodecPrintInfo;
    @BindView(R.id.tv_mediacodec_info)
    TextView tvMediacodecInfo;
    @BindView(R.id.tv_audio_decodec)
    TextView tvAudioDecodec;
    @BindView(R.id.tv_video_decodec)
    TextView tvVideoDecodec;
    @BindView(R.id.tv_av_encodec_decodec)
    TextView tvAvEncodecDecodec;

    private AACToPCMDelegate aacToPCMDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediacodec_layout);
        ButterKnife.bind(this);
        aacToPCMDelegate = new AACToPCMDelegate(this);
    }


    private void printMediaCodecInfo() {
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            String name = codecInfoAt.getName();
            String canonicalName = codecInfoAt.getCanonicalName();
            String[] supportedTypes = codecInfoAt.getSupportedTypes();
            boolean encoder = codecInfoAt.isEncoder();
            boolean hardwareAccelerated = codecInfoAt.isHardwareAccelerated();

            Log.i(TAG, "printMediaCodecInfo: name=" + name + " hardwareAccelerated=" + hardwareAccelerated + " encoder=" + encoder);
        }

        MediaExtractor mediaExtractor = new MediaExtractor();
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "forme.mp4");
        Log.i(TAG, "printMediaCodecInfo: path=" + file.getAbsolutePath());
        if (!file.exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaExtractor.setDataSource(file.getAbsolutePath());
            int trackCount = mediaExtractor.getTrackCount();
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(0);


            MediaCodecList mediaCodecList = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
                String decoderForFormat = mediaCodecList.findDecoderForFormat(trackFormat);//OMX.qcom.video.decoder.avc
                String encoderForFormat = mediaCodecList.findEncoderForFormat(trackFormat);//OMX.qcom.video.encoder.avc
                Log.i(TAG, "printMediaCodecInfo: trackCount=" + trackCount + " decoderForFormat=" + decoderForFormat + " encoderForFormat=" + encoderForFormat + " trackFromat=" + trackFormat);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.tv_mediacodec_print_info, R.id.tv_audio_decodec, R.id.tv_video_decodec, R.id.tv_av_encodec_decodec})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_mediacodec_print_info:
                printMediaCodecInfo();
                break;
            case R.id.tv_audio_decodec:
                aacToPCMDelegate.aacToPCM();
                break;
            case R.id.tv_video_decodec:
                playMP4();
                break;
            case R.id.tv_av_encodec_decodec:
                recordAVToMP4();
                break;
        }
    }



    private void playMP4() {

    }


    private void recordAVToMP4() {

    }
}
