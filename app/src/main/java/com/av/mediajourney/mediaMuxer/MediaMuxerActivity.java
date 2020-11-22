package com.av.mediajourney.mediaMuxer;

import android.app.Activity;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
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
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 1. 提取MP4中的音轨和视频轨道    MediaExtractor
 * 2. 加入新的音频轨道合成新的MP4  MediaMuxer
 * <p>
 * 需要资源
 * 1 mp4
 * 2 mp3 --许巍
 */
public class MediaMuxerActivity extends Activity {

    private static final String TAG = "MediaMuxerActivity";
    @BindView(R.id.tv_extractor_and_muxer)
    TextView tvDemuxer;
    @BindView(R.id.tv_out)
    TextView tvOut;
    @BindView(R.id.tv_mux1)
    TextView tvMux1;
    @BindView(R.id.tv_out1)
    TextView tvOut1;
    @BindView(R.id.tv_mux2)
    TextView tvMux2;
    @BindView(R.id.tv_out2)
    TextView tvOut2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_muxer_layout);
        ButterKnife.bind(this);
        tvOut.setVisibility(View.GONE);
        tvOut1.setVisibility(View.GONE);
        tvOut2.setVisibility(View.GONE);
    }

    @OnClick({R.id.tv_extractor_and_muxer, R.id.tv_mux1, R.id.tv_mux2})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_extractor_and_muxer:
                extractorAndMuxerMP4();
                break;
            case R.id.tv_mux1:
                String path = muxerMp4("audio.aac", "muxer.mp4");
                tvOut1.setText("合成视频路径：" + path);
                tvOut1.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_mux2:
                String path2 = muxerMp4("sanguo.aac", "muxer2.mp4");
                tvOut2.setText("合成视频路径：" + path2);
                tvOut2.setVisibility(View.VISIBLE);
                break;
        }
    }



    /**
     * 把音轨和视频轨再合成新的视频
     */
    private String muxerMp4(String inputAudio , String outPutVideo) {
        File videoFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "video.mp4");
        File audioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), inputAudio);
        File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), outPutVideo);

        if (outputFile.exists()) {
            outputFile.delete();
        }
        if (!videoFile.exists()) {
            Toast.makeText(this, "视频源文件不存在", Toast.LENGTH_SHORT).show();
            return "";
        }
        if (!audioFile.exists()) {
            Toast.makeText(this, "音频源文件不存在", Toast.LENGTH_SHORT).show();
            return "";
        }

        MediaExtractor videoExtractor = new MediaExtractor();
        MediaExtractor audioExtractor = new MediaExtractor();

        try {
            MediaMuxer mediaMuxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int videoTrackIndex = 0;
            int audioTrackIndex = 0;

            //先添加视频轨道
            videoExtractor.setDataSource(videoFile.getAbsolutePath());
            int trackCount = videoExtractor.getTrackCount();
            Log.i(TAG, "muxerToMp4: trackVideoCount=" + trackCount);

            for (int i = 0; i < trackCount; i++) {
                MediaFormat trackFormat = videoExtractor.getTrackFormat(i);
                String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
                if (TextUtils.isEmpty(mimeType)) {
                    continue;
                }
                if (mimeType.startsWith("video/")) {
                    videoExtractor.selectTrack(i);

                    videoTrackIndex = mediaMuxer.addTrack(trackFormat);
                    Log.i(TAG, "muxerToMp4: videoTrackIndex=" + videoTrackIndex);
                    break;
                }
            }

            //再添加音频轨道
            audioExtractor.setDataSource(audioFile.getAbsolutePath());
            int trackCountAduio = audioExtractor.getTrackCount();
            Log.i(TAG, "muxerToMp4: trackCountAduio=" + trackCountAduio);
            for (int i = 0; i < trackCountAduio; i++) {
                MediaFormat trackFormat = audioExtractor.getTrackFormat(i);
                String mimeType = trackFormat.getString(MediaFormat.KEY_MIME);
                if (TextUtils.isEmpty(mimeType)) {
                    continue;
                }
                if (mimeType.startsWith("audio/")) {
                    audioExtractor.selectTrack(i);
                    audioTrackIndex = mediaMuxer.addTrack(trackFormat);
                    Log.i(TAG, "muxerToMp4: audioTrackIndex=" + audioTrackIndex);
                    break;
                }

            }



            //再进行合成
            mediaMuxer.start();

            ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int sampleSize = 0;

            while ((sampleSize = videoExtractor.readSampleData(byteBuffer, 0)) > 0) {

                bufferInfo.flags = videoExtractor.getSampleFlags();
                bufferInfo.offset = 0;
                bufferInfo.size = sampleSize;
                bufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo);
                videoExtractor.advance();
            }

            int audioSampleSize = 0;

            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


            while ((audioSampleSize = audioExtractor.readSampleData(byteBuffer, 0)) > 0) {

                audioBufferInfo.flags = audioExtractor.getSampleFlags();
                audioBufferInfo.offset = 0;
                audioBufferInfo.size = audioSampleSize;
                audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                mediaMuxer.writeSampleData(audioTrackIndex, byteBuffer, audioBufferInfo);
                audioExtractor.advance();
            }

            //最后释放资源
            videoExtractor.release();
            audioExtractor.release();
            mediaMuxer.stop();
            mediaMuxer.release();

        } catch (IOException e) {
            e.printStackTrace();
            return "";

        }
        return outputFile.getAbsolutePath();


    }

    private void muxerStart(MediaExtractor audioExtractor, MediaExtractor videoExtractor, MediaMuxer mediaMuxer, int videoTrackIndex, int audioTrackIndex) {
        mediaMuxer.start();

        ByteBuffer byteBuffer = ByteBuffer.allocate(500 * 1024);

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int sampleSize = 0;

        while ((sampleSize = videoExtractor.readSampleData(byteBuffer, 0)) > 0) {

            bufferInfo.flags = videoExtractor.getSampleFlags();
            bufferInfo.offset = 0;
            bufferInfo.size = sampleSize;
            bufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
            mediaMuxer.writeSampleData(videoTrackIndex, byteBuffer, bufferInfo);
            videoExtractor.advance();
        }

        int audioSampleSize = 0;

        MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


        while ((audioSampleSize = audioExtractor.readSampleData(byteBuffer, 0)) > 0) {

            audioBufferInfo.flags = audioExtractor.getSampleFlags();
            audioBufferInfo.offset = 0;
            audioBufferInfo.size = audioSampleSize;
             audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
            mediaMuxer.writeSampleData(audioTrackIndex, byteBuffer, audioBufferInfo);
            audioExtractor.advance();
        }
    }

    private void extractorAndMuxerMP4() {
        tvOut.setText("");
        File inputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "forme.mp4");
        if (!inputFile.exists()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        //数据提取(解封装)
        //1. 构造MediaExtractor
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            //2.设置数据源
            mediaExtractor.setDataSource(inputFile.getAbsolutePath());
            //3. 获取轨道数
            int trackCount = mediaExtractor.getTrackCount();
            Log.i(TAG, "demuxerMP4: trackCount=" + trackCount);
            //遍历轨道，查看音频轨或者视频轨道信息
            for (int i = 0; i < trackCount; i++) {
                //4. 获取某一轨道的媒体格式
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                String keyMime = trackFormat.getString(MediaFormat.KEY_MIME);
                Log.i(TAG, "demuxerMp4: keyMime=" + keyMime);
                if (TextUtils.isEmpty(keyMime)) {
                    continue;
                }
                //5.通过mime信息识别音轨或视频轨道，打印相关信息
                if (keyMime.startsWith("video/")) {
                    File outputFile = extractorAndMuxer(mediaExtractor, i, "/video.mp4");
                    tvOut.setText("纯视频文件路径：" + outputFile.getAbsolutePath());
                    Log.i(TAG, "extractorAndMuxerMP4: videoWidth="+trackFormat.getInteger(MediaFormat.KEY_WIDTH)+" videoHeight="+trackFormat.getInteger(MediaFormat.KEY_HEIGHT));

                } else if (keyMime.startsWith("audio/")) {
                    File outputFile = extractorAndMuxer(mediaExtractor, i, "/audio.aac");

                    Log.i(TAG, "extractorAndMuxerMP4: channelCount="+trackFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)+" bitRate="+trackFormat.getInteger(MediaFormat.KEY_BIT_RATE));

                    tvOut.setText(tvOut.getText().toString() + "\n纯音频路径：" + outputFile.getAbsolutePath());
                    tvOut.setVisibility(View.VISIBLE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mediaExtractor.release();
        }

    }

    private File extractorAndMuxer(MediaExtractor mediaExtractor, int i, String outputName) throws IOException {
        MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
        MediaMuxer mediaMuxer;
        mediaExtractor.selectTrack(i);

        File outputFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath() + outputName);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        //1. 构造MediaMuxer
        mediaMuxer = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        //2. 添加轨道信息 参数为MediaFormat
        mediaMuxer.addTrack(trackFormat);
        //3. 开始合成
        mediaMuxer.start();

        //4. 设置buffer
        ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

        //5.通过mediaExtractor.readSampleData读取数据流
        int sampleSize = 0;
        while ((sampleSize = mediaExtractor.readSampleData(buffer, 0)) > 0) {
            bufferInfo.flags = mediaExtractor.getSampleFlags();
            bufferInfo.offset = 0;
            bufferInfo.size = sampleSize;
            bufferInfo.presentationTimeUs = mediaExtractor.getSampleTime();
            int isEOS = bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM;
            Log.i(TAG, "demuxerMp4:  flags=" + bufferInfo.flags + " size=" + sampleSize + " time=" + bufferInfo.presentationTimeUs + " outputName" + outputName+" isEOS="+isEOS);
            //6. 把通过mediaExtractor解封装的数据通过writeSampleData写入到对应的轨道
            mediaMuxer.writeSampleData(0, buffer, bufferInfo);
            mediaExtractor.advance();
        }
        Log.i(TAG, "extractorAndMuxer: " + outputName + "提取封装完成");

        mediaExtractor.unselectTrack(i);
        //6.关闭
        mediaMuxer.stop();
        mediaMuxer.release();
        return outputFile;
    }
}
