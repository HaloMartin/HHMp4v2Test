//
// Created by M君 on 2019/4/29.
//

#ifndef HHMP4V2TEST_MP4RECORD_H
#define HHMP4V2TEST_MP4RECORD_H

#import "mp4v2/mp4v2.h"

#define _NALU_SPS_  7
#define _NALU_PPS_  8
#define _NALU_IDR_  5
#define _NALU_BP_   1

typedef struct MP4V2_CONTEXT{
    int m_vWidth,m_vHeight;
    int m_vFrateR,m_vTimeScale;
    int m_aChannel,m_aSamplerate;
    int64_t m_lastTimestamp;
    MP4FileHandle m_mp4FHandle;
    MP4TrackId m_vTrackId,m_aTrackId;
    double m_vFrameDur;
} MP4V2_CONTEXT;

MP4V2_CONTEXT * initMp4Handler(const char * filename,int width,int height, int fps, int channel, int samplerate);
int packMp4Video(MP4V2_CONTEXT * recordCtx, uint8_t * data, int len);
int packMp4Audio(MP4V2_CONTEXT * recordCtx, uint8_t * data, int len);
void closeMp4Handler(MP4V2_CONTEXT * recordCtx);

/**
 * 获取当前时间，用于计算帧时长，目前是固定帧率，没有实际用途
 * */
int64_t GetMilliSecondTime();

/**
 * 获取NALU的类型
 * @param nalu NAL单元数据
 * @return 返回上面定义的_NALU_SPS_，_NALU_PPS_，_NALU_IDR_，_NALU_BP_四种类型之一，未知或识别不出来的的类型则返回-1
 * */
int getNALUType(uint8_t * nalu);

/**
 * 获取配置AAC配置，这个是从实际项目中移植过来的，demo中没有测试这个功能
 * */
bool GetConfiguration(int aactype, int sampleRate,int chnCount, unsigned char* cfgBuf, int cfgBufLen);

#endif //HHMP4V2TEST_MP4RECORD_H
