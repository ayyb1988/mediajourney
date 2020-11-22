package com.av.mediajourney.mediacodec;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AACToPCMDelegate {

    private static final String TAG = "AACToPCMDelegate";
    private static final long TIMEOUT_US = 0;
    private Context mContext;

    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private MediaExtractor mediaExtractor;
    private MediaCodec decodec;

    private FileOutputStream fileOutputStream;

    public AACToPCMDelegate(MediaCodecActivity context) {
        this.mContext = context;
    }

    /**
     * 通过aacToPCM 熟悉mediaCodec的流程，以及通过同步和异步两种方式实现
     */
    void aacToPCM() {
        boolean isAsync = true;
        if (isAsync && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            isAsync = false;
        }

        //1. initFile
        File file = initFile(isAsync);
        if (file == null) {
            return;
        }

        //2. 初始化mediaCodec
        initMediaCodec(file.getAbsolutePath(), isAsync);
        if (decodec == null) {
            Toast.makeText(mContext, "decodec is null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isAsync) {
            //同步处理
            decodecAacToPCMSync();
        }
    }

    private File initFile(boolean isAsync) {
        String child = isAsync ? "aacToPcmAsync.pcm" : "aacToPcmSync.pcm";
        File outputfile = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), child);
        if (outputfile.exists()) {
            outputfile.delete();
        }
        try {
            fileOutputStream = new FileOutputStream(outputfile.getAbsolutePath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        File file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "sanguo.aac");
        if (!file.exists()) {
            Toast.makeText(mContext, "文件不存在", Toast.LENGTH_SHORT).show();
            return null;
        }
        return file;
    }

    private void initMediaCodec(String path, boolean isASync) {
        mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(path);
            int trackCount = mediaExtractor.getTrackCount();
            for (int i = 0; i < trackCount; i++) {

                MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (TextUtils.isEmpty(mime)) {
                    continue;
                }
                Log.i(TAG, "initMediaCodec: mime=" + mime);
                if (mime.startsWith("audio/")) {
                    mediaExtractor.selectTrack(i);
                }
                //生成MediaCodec，此时处于Uninitialized状态
                decodec = MediaCodec.createDecoderByType(mime);
                //configure 处于Configured状态
                decodec.configure(trackFormat, null, null, 0);

                if (isASync) {
                    setAsyncCallBack();
                }
                //处于Excuting状态 Flushed子状态
                decodec.start();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    inputBuffers = decodec.getInputBuffers();
                    outputBuffers = decodec.getOutputBuffers();
                    Log.i(TAG, "initMediaCodec: inputBuffersSize=" + inputBuffers.length + " outputBuffersSize=" + outputBuffers.length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 成功输出到目标文件
     * 到处生成的pcm，用ffplay播放pcm文件 发现和之前的aac是一样的
     * ffplay -ar 44100 -channels 2 -f s16le -i /Users/yabin/Desktop/tmp/aacToPcm.pcm
     */
    private void decodecAacToPCMSync() {
        boolean isInputBufferEOS = false;
        boolean isOutPutBufferEOS = false;

        while (!isOutPutBufferEOS) {
            if (!isInputBufferEOS) {
                //1. 从codecInputBuffer中拿到empty input buffer的index
                int index = decodec.dequeueInputBuffer(TIMEOUT_US);
                if (index >= 0) {
                    ByteBuffer inputBuffer;
                    //2. 通过index获取到inputBuffer
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        inputBuffer = decodec.getInputBuffer(index);
                    } else {
                        inputBuffer = inputBuffers[index];
                    }
                    if (inputBuffer != null) {
                        Log.i(TAG, "decodecAacToPCMSync: " + "  index=" + index);

                        inputBuffer.clear();
                    }
                    //extractor读取sampleData
                    int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                    //3. 如果读取不到数据，则认为是EOS。把数据生产端的buffer 送回到code的inputbuffer
                    Log.i(TAG, "decodecAacToPCMSync: sampleSize=" + sampleSize);
                    if (sampleSize < 0) {
                        isInputBufferEOS = true;
                        decodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        decodec.queueInputBuffer(index, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                        //读取下一帧
                        mediaExtractor.advance();
                    }
                }
            }

            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            //4. 数据消费端Client 拿到一个有数据的outputbuffer的index
            int index = decodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);
            if (index < 0) {
                continue;
            }
            ByteBuffer outputBuffer;
            //5. 通过index获取到inputBuffer
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                outputBuffer = decodec.getOutputBuffer(index);
            } else {
                outputBuffer = outputBuffers[index];
            }

            Log.i(TAG, "decodecAacToPCMSync: outputbuffer index=" + index + " size=" + bufferInfo.size + " flags=" + bufferInfo.flags);
            //把数据写入到FileOutputStream
            byte[] bytes = new byte[bufferInfo.size];
            outputBuffer.get(bytes);
            try {
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //6. 然后清空outputbuffer，再释放给codec的outputbuffer
            decodec.releaseOutputBuffer(index, false);
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                isOutPutBufferEOS = true;
            }
        }

        close();
    }

    private void close() {
        mediaExtractor.release();
        decodec.stop();
        decodec.release();
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAsyncCallBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            decodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                    Log.i(TAG, "setAsyncCallBack - onInputBufferAvailable: index=" + index);
                    //1. 从codecInputBuffer中拿到empty input buffer的index
                    if (index >= 0) {
                        ByteBuffer inputBuffer;
                        //2. 通过index获取到inputBuffer
                        inputBuffer = decodec.getInputBuffer(index);
                        if (inputBuffer != null) {
                            Log.i(TAG, "setAsyncCallBack- onInputBufferAvailable: " + "  index=" + index);
                            inputBuffer.clear();
                        }
                        //extractor读取sampleData
                        int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
                        //3. 如果读取不到数据，则认为是EOS。把数据生产端的buffer 送回到code的inputbuffer
                        Log.i(TAG, "setAsyncCallBack- onInputBufferAvailable: sampleSize=" + sampleSize);
                        if (sampleSize < 0) {
                            decodec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            decodec.queueInputBuffer(index, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                            //读取下一帧
                            mediaExtractor.advance();
                        }
                    }
                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo bufferInfo) {
                    Log.i(TAG, "setAsyncCallBack - onOutputBufferAvailable: index=" + index + " size=" + bufferInfo.size + " flags=" + bufferInfo.flags);
                    //4. 数据消费端Client 拿到一个有数据的outputbuffer的index
                    if (index >= 0) {
                        ByteBuffer outputBuffer;
                        //5. 通过index获取到inputBuffer
                        outputBuffer = decodec.getOutputBuffer(index);

                        Log.i(TAG, "setAsyncCallBack - onOutputBufferAvailable: outputbuffer index=" + index + " size=" + bufferInfo.size + " flags=" + bufferInfo.flags);
                        //把数据写入到FileOutputStream
                        byte[] bytes = new byte[bufferInfo.size];
                        outputBuffer.get(bytes);
                        try {
                            fileOutputStream.write(bytes);
                            fileOutputStream.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //6. 然后清空outputbuffer，再释放给codec的outputbuffer
                        decodec.releaseOutputBuffer(index, false);
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            close();
                        }
                    }

                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                    Log.e(TAG, "setAsyncCallBack - onError: ");
                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                    Log.i(TAG, "setAsyncCallBack - onOutputFormatChanged: ");

                }
            });
        }

    }

}
