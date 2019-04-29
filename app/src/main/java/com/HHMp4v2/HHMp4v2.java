package com.HHMp4v2;

public class HHMp4v2 {
    /**
     * 初始化MP4文件
     * @param fullPath mp4文件全路径名
     * @param width 视频宽
     * @param height 视频高
     * @param fps 视频帧率
     * @param channel 声道
     * @param samplerate 音频采样率
     * @return 1 if success, 0 if fail
     * */
    public native int initMp4Packer(String fullPath, int width, int height, int fps, int channel, int samplerate);

    /**
     * 封装视频数据帧进Mp4文件
     * @param data 视频帧数据
     * @param dataLen 帧数据data的长度
     * @return -1 if failed, dataLen pack in if success
     * */
    public native int packMp4Video(byte[] data, int dataLen);

    /**
     * 封装音频数据帧进Mp4文件
     * @param data 已编码为AAC格式的音频帧数据
     * @param dataLen 帧数据data的长度
     * @return -1 if failed, dataLen pack in if success
     * */
    public native int packMp4Audio(byte[] data, int dataLen);

    /**
     * 结束并关闭Mp4文件
     * */
    public native void mp4Close();

    static {
        //加载libHHMp4v2.so动态库，用于测试
        System.loadLibrary("HHMp4v2");
    }
}
