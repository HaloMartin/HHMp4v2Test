//
// Created by M君 on 2019/4/28.
//
#include <com_HHMp4v2_HHMp4v2.h>
#include "mp4record.h"
#include "mp4record.c"

MP4V2_CONTEXT * _mp4Handle;


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
JNIEXPORT jint JNICALL Java_com_HHMp4v2_HHMp4v2_initMp4Packer
        (JNIEnv *env, jobject obj, jstring fullpath, jint width, jint height, jint fps, jint channel, jint samplerate)
{
    const char *local_title = (*env)->GetStringUTFChars(env, fullpath, NULL);
    _mp4Handle = initMp4Handler(local_title, width, height, fps, channel, samplerate);
    if (_mp4Handle != NULL) { return 1;}
    return 0;
}


/**
 * 封装视频数据帧进Mp4文件
 * @param data 视频帧数据
 * @param dataLen 帧数据data的长度
 * @return -1 if failed, dataLen pack in if success
 * */
JNIEXPORT jint JNICALL Java_com_HHMp4v2_HHMp4v2_packMp4Video
        (JNIEnv *env, jobject obj, jbyteArray data, jint dataLen)
{
    unsigned char *buf = (unsigned char *) (*env)->GetByteArrayElements(env, data, JNI_FALSE);
    int nalsize = dataLen;
    int reseult = packMp4Video(_mp4Handle, buf, nalsize);
    return reseult;
}

/**
 * 封装音频数据帧进Mp4文件
 * @param data 已编码为AAC格式的音频帧数据
 * @param dataLen 帧数据data的长度
 * @return -1 if failed, dataLen pack in if success
 * */
JNIEXPORT jint JNICALL Java_com_HHMp4v2_HHMp4v2_packMp4Audio
        (JNIEnv *env, jobject obj, jbyteArray data, jint dataLen)
{
    unsigned char *buf = (unsigned char *) (*env)->GetByteArrayElements(env, data, JNI_FALSE);
    int nalsize = dataLen;
    int reseult = packMp4Audio(_mp4Handle, buf, nalsize);
    return reseult;
}

/**
 * 结束并关闭Mp4文件
 * */
JNIEXPORT void JNICALL Java_com_HHMp4v2_HHMp4v2_mp4Close
        (JNIEnv *env, jobject obj)
{
    closeMp4Handler(_mp4Handle);
}
