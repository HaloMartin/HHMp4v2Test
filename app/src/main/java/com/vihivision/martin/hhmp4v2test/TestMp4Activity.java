package com.vihivision.martin.hhmp4v2test;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.HHMp4v2.HHMp4v2;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: archko 2014/4/29 :15:57
 */
public class TestMp4Activity extends Activity {
//    private final static String TAG = "TestMp4Activity";
    private final static String TAG = "MartinDev";//Only for debug

    Button test;

    ProgressDialog mProgressDialog;

    private SurfaceView mSurface = null;
    private SurfaceHolder mSurfaceHolder;
    private Thread mDecodeThread;
    private MediaCodec mCodec;
    private boolean mStopFlag = false;
    private DataInputStream mInputStream;
    private String FileName = "mtv.h264";
    private static final int VIDEO_WIDTH = 1920;
    private static final int VIDEO_HEIGHT = 1080;
    private int FrameRate = 15;
    private Boolean UseSPSandPPS = true;
//    private String filePath = Environment.getExternalStorageDirectory() + "/" + FileName;
    private String filePath = FileName;
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMddHHmm");
    String outFilepath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "h264-" + mSimpleDateFormat.format(new Date()) + ".mp4";

    private static int logcount = 1;//只打印一次

    //音频
    private MediaCodec mAudioDecode;

    //
    private HHMp4v2 mHHMp4v2 = new HHMp4v2();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        mSurface = (SurfaceView) findViewById(R.id.surfaceview);
        test = (Button) findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
        try {
            //获取文件输入流
//            mInputStream = new DataInputStream(new FileInputStream(new File(filePath)));
            mInputStream = new DataInputStream(this.getAssets().open(filePath));
            Log.d(TAG, "filePath:" + filePath);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "FileNotFoundException Occurs, info:" + Log.getStackTraceString(e));
            e.printStackTrace();
            try {
                if (mInputStream != null) {
                    mInputStream.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException Occurs, info:" + Log.getStackTraceString(e));
            e.printStackTrace();
        }
        mSurfaceHolder = mSurface.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCodec(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCodec.stop();
                mCodec.release();
            }
        });
    }


    private void test() {
        startDecodingThread();
    }


    private class decodeThread implements Runnable {
        @Override
        public void run() {
            try {
                Log.d(TAG, "decodeThread Start");
                decodeLoop();
            } catch (Exception e) {
                Log.d(TAG, "Exception Occurs, info: " + Log.getStackTraceString(e));
                if (mHHMp4v2 != null) {
                    mHHMp4v2.mp4Close();
                }
                Log.d(TAG, "HHMp4v2.mp4Close(), decodeLoop End");
            }
        }

        private void decodeLoop() {
            //存放目标文件的数据
            ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
            //解码后的数据，包含每一个buffer的元数据信息，例如偏差，在相关解码器中有效的数据大小
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            long startMs = System.currentTimeMillis();
            long timeoutUs = 10000;
            byte[] marker0 = new byte[]{0, 0, 0, 1};
            byte[] dummyFrame = new byte[]{0x00, 0x00, 0x01, 0x20};
            byte[] streamBuffer = new byte[1024 * 1024 * 5];

            try {
                while (true) {
                    int length = mInputStream.available();
                    if (length > 0) {
                        int count = mInputStream.read(streamBuffer);
                        mStopFlag = false;
                        int bytes_cnt = 0;
                        while (mStopFlag == false) {
                            bytes_cnt = streamBuffer.length;
                            if (bytes_cnt == 0) {
                                streamBuffer = dummyFrame;
                            }

                            int startIndex = 0;
                            int remaining = bytes_cnt;
                            while (true) {
                                if (remaining == 0 || startIndex >= remaining) {
                                    break;
                                }
                                int nextFrameStart = KMPMatch(marker0, streamBuffer, startIndex + 2, remaining);
                                if (nextFrameStart == -1) {
                                    nextFrameStart = remaining;
                                } else {
                                }
                                if (logcount > 0) {
                                    logcount --;
                                    Log.d(TAG, "streamBuffer(As long as logcat can):" + byte2HexString(streamBuffer, 6 * 1024));
                                }
                                Log.d(TAG, "Parameters:(nextFrameStart, startIndex, remaining)=("
                                        + nextFrameStart + ", " + startIndex + ", " + remaining + ")");

                                int inIndex = mCodec.dequeueInputBuffer(timeoutUs);
                                if (inIndex >= 0) {
                                    byte[] newData = new byte[nextFrameStart - startIndex];
                                    System.arraycopy(streamBuffer, startIndex, newData, 0, newData.length);
                                    int i = -1;
                                    if (mHHMp4v2 != null) {
                                        i = mHHMp4v2.packMp4Video(newData, newData.length);
                                    }
                                    Log.d(TAG, "HHMp4v2.packMp4Video(\"" + newData + "\", " + newData.length + ") return:" + i);
                                    String hexStr = byte2HexString(newData, (newData.length > 50?50:newData.length));//只转换newData的前50个字节来看即可
//                                    Log.d(TAG, "newData(Pre " + (newData.length > 50?50:newData.length) + " bytes):" + hexStr);

                                    ByteBuffer byteBuffer = inputBuffers[inIndex];
                                    byteBuffer.clear();
                                    byteBuffer.put(streamBuffer, startIndex, nextFrameStart - startIndex);
                                    //在给指定Index的inputbuffer[]填充数据后，调用这个函数把数据传给解码器
                                    mCodec.queueInputBuffer(inIndex, 0, nextFrameStart - startIndex, 0, 0);
                                    startIndex = nextFrameStart;
                                } else {
                                    continue;
                                }

                                int outIndex = mCodec.dequeueOutputBuffer(info, timeoutUs);
                                if (outIndex >= 0) {
                                    //帧控制是不在这种情况下工作，因为没有PTS H264是可用的
                                    while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                                        try {
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    boolean doRender = (info.size != 0);
                                    //对outputbuffer的处理完后，调用这个函数把buffer重新返回给codec类。
                                    mCodec.releaseOutputBuffer(outIndex, doRender);
                                } else {
                                }
                            }
                            mStopFlag = true;
                            if (mHHMp4v2 != null) {
                                mHHMp4v2.mp4Close();
                            }
                            Log.d(TAG, "HHMp4v2.mp4Close(), decodeLoop End");
                        }
                    }

                }
//                streamBuffer = getBytes(mInputStream);
            } catch (IOException e) {
                Log.d(TAG, "IOException Occurs, info:" + Log.getStackTraceString(e));
                e.printStackTrace();
            }

        }
    }

    int KMPMatch(byte[] pattern, byte[] bytes, int start, int remain) {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] lsp = computeLspTable(pattern);

        int j = 0;  // Number of chars matched in pattern
        for (int i = start; i < remain; i++) {
            while (j > 0 && bytes[i] != pattern[j]) {
                // Fall back in the pattern
                j = lsp[j - 1];  // Strictly decreasing
            }
            if (bytes[i] == pattern[j]) {
                // Next char matched, increment position
                j++;
                if (j == pattern.length)
                    return i - (j - 1);
            }
        }

        return -1;  // Not found
    }

    int[] computeLspTable(byte[] pattern) {
        int[] lsp = new int[pattern.length];
        lsp[0] = 0;  // Base case
        for (int i = 1; i < pattern.length; i++) {
            // Start by assuming we're extending the previous LSP
            int j = lsp[i - 1];
            while (j > 0 && pattern[i] != pattern[j])
                j = lsp[j - 1];
            if (pattern[i] == pattern[j])
                j++;
            lsp[i] = j;
        }
        return lsp;
    }

    private void initCodec(SurfaceHolder holder) {
        try {
            //通过多媒体格式名创建一个可用的解码器
            mCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //初始化编码器
        final MediaFormat mediaformat = MediaFormat.createVideoFormat("video/avc", VIDEO_WIDTH, VIDEO_HEIGHT);
        //获取h264中的pps及sps数据
        if (UseSPSandPPS) {
            byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
            byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};
            mediaformat.setByteBuffer("csd-0", ByteBuffer.wrap(header_sps));
            mediaformat.setByteBuffer("csd-1", ByteBuffer.wrap(header_pps));
        }
        //设置帧率
        mediaformat.setInteger(MediaFormat.KEY_FRAME_RATE, FrameRate);
        //https://developer.android.com/reference/android/media/MediaFormat.html#KEY_MAX_INPUT_SIZE
        //设置配置参数，参数介绍 ：
        // format	如果为解码器，此处表示输入数据的格式；如果为编码器，此处表示输出数据的格式。
        //surface	指定一个surface，可用作decode的输出渲染。
        //crypto	如果需要给媒体数据加密，此处指定一个crypto类.
        //   flags	如果正在配置的对象是用作编码器，此处加上CONFIGURE_FLAG_ENCODE 标签。
        mCodec.configure(mediaformat, holder.getSurface(), null, 0);
//        startDecodingThread();

        if (mHHMp4v2 == null) {
            mHHMp4v2 = new HHMp4v2();
        }
        int i = mHHMp4v2.initMp4Packer(outFilepath, 1080, 720, 25, 1, 36000);
        Log.d(TAG, "HHMp4v2.initMp4Packer(\"" +outFilepath + "\", 1080, 720, 25, 1, 36000) return:" + i + "");
    }

    private String byte2HexString(byte[] bytes, int size) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < size; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
//            sb.append(hex + ((i%2==0)?"":" "));
            sb.append(hex);
        }
        return sb.toString();
    }

    private void startDecodingThread() {
        mCodec.start();
        mDecodeThread = new Thread(new decodeThread());
        mDecodeThread.start();
    }

}
