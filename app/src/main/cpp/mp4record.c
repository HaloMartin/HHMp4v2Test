//
// Created by M君 on 2019/4/29.
//

#include "mp4record.h"
#include "string.h"
#include <stdlib.h>
#include <sys/time.h>


MP4V2_CONTEXT * initMp4Handler(const char * filename,int width,int height, int fps, int channel, int samplerate)
{
    MP4V2_CONTEXT * recordCtx = (MP4V2_CONTEXT*)malloc(sizeof(struct MP4V2_CONTEXT));
    if (!recordCtx) {
        printf("MartinDev HHMp4v2 error:malloc context \n");
        return NULL;
    }

    recordCtx->m_vWidth = width;
    recordCtx->m_vHeight = height;
    recordCtx->m_vFrateR = fps;
    recordCtx->m_vTimeScale = 90000;
    recordCtx->m_vFrameDur = 3000;
    recordCtx->m_aChannel = channel;
    recordCtx->m_aSamplerate = samplerate;
    recordCtx->m_lastTimestamp = GetMilliSecondTime();
    recordCtx->m_vTrackId = MP4_INVALID_TRACK_ID;
    recordCtx->m_aTrackId = MP4_INVALID_TRACK_ID;

    recordCtx->m_mp4FHandle = MP4Create(filename,0);
    if (recordCtx->m_mp4FHandle == MP4_INVALID_FILE_HANDLE) {
        printf("MartinDev HHMp4v2 error : MP4Create failed\n");
        return NULL;
    }
    //设置每秒tick数
    MP4SetTimeScale(recordCtx->m_mp4FHandle, (uint32_t) recordCtx->m_vTimeScale);
    printf("MartinDev HHMp4v2 ok : initMp4Handler file=%s success\n",filename);
    return recordCtx;
}

int packMp4Video(MP4V2_CONTEXT * recordCtx, uint8_t * data, int len)
{
    int retNum = 0;
    if (!recordCtx || !recordCtx->m_mp4FHandle) {
        printf("MartinDev HHMp4v2 error : packMp4Video not init yet");
        return -1;
    }
    if (!data) {
        printf("MartinDev HHMp4v2 error : packMp4Video data NULL, please check");
        return -1;
    }
    int naluType = getNALUType(data);
    uint8_t * naluData = data;
    int naluSize = len;
    if (naluType == -1) {
        return -1;
    }
    switch (naluType) {
        case _NALU_SPS_:
            //根据SPS信息，先创建track ID
            if (recordCtx->m_vTrackId == MP4_INVALID_TRACK_ID) {
                recordCtx->m_vTrackId = MP4AddH264VideoTrack
                        (recordCtx->m_mp4FHandle,
                         (uint32_t) recordCtx->m_vTimeScale,
                         (MP4Duration) recordCtx->m_vTimeScale / recordCtx->m_vFrateR,
                         (uint16_t) recordCtx->m_vWidth,
                         (uint16_t) recordCtx->m_vHeight,
                         naluData[5], //sps[1] AVCProfileIndication
                         naluData[6], //sps[2] profile_compat
                         naluData[7], //sps[3] AVCLevelIndication
                         3);
                if (recordCtx->m_vTrackId == MP4_INVALID_TRACK_ID) {
                    printf("MartinDev HHMp4v2 add h264 video track id failed.\n");
                    return -1;
                }
                MP4SetVideoProfileLevel(recordCtx->m_mp4FHandle, 0x7F); //  Simple Profile @ Level 3
            }
            //Set SPS, attention that nalu's flag, first 4 bytes:0x00 00 00 01, should be ignored, payload from 5th byte
            MP4AddH264SequenceParameterSet(recordCtx->m_mp4FHandle, recordCtx->m_vTrackId, naluData+4, (uint16_t) (naluSize-4));
            printf("MartinDev HHMp4v2 add SPS success.");
            break;
        case _NALU_PPS_:
            //Set PPS, atttention that nalu's flag, first 4 bytes:0x00 00 00 01, should be ignored, payload from 5th byte
            MP4AddH264PictureParameterSet(recordCtx->m_mp4FHandle,recordCtx->m_vTrackId,naluData+4,naluSize-4);
            printf("MartinDev HHMp4v2 add PPS success");
            break;
        case _NALU_IDR_:
        {
            if (recordCtx->m_vTrackId == MP4_INVALID_TRACK_ID) {
                printf("MartinDev HHMp4v2 error: invalid track id, not ready yet");
                return -1;
            }
            //replace the first 4 bytes with nalu payload's size
            naluData[0] = (uint8_t) ((naluSize - 4) >> 24);
            naluData[1] = (uint8_t) ((naluSize - 4) >> 16);
            naluData[2] = (uint8_t) ((naluSize - 4) >> 8);
            naluData[3] = (uint8_t) ((naluSize - 4) & 0xFF);
            bool result = MP4WriteSample(recordCtx->m_mp4FHandle
                    , recordCtx->m_vTrackId
                    , naluData
                    , (uint32_t) naluSize
                    , MP4_INVALID_DURATION
                    , 0
                    , 1);
            if (!result) {
                printf("MartinDev HHMp4v2 error: packMp4Video MP4WriteSample failed.");
                return -1;
            }
            retNum = naluSize;
            printf("MartinDev HHMp4v2 ok : packMp4Video success.");
            break;
        }
        case _NALU_BP_:
        {
            if (recordCtx->m_vTrackId == MP4_INVALID_TRACK_ID) {
                printf("MartinDev HHMp4v2 error: invalid track id, not ready yet");
                return -1;
            }
            //replace the first 4 bytes with nalu payload's size
            naluData[0] = (uint8_t) ((naluSize - 4) >> 24);
            naluData[1] = (uint8_t) ((naluSize - 4) >> 16);
            naluData[2] = (uint8_t) ((naluSize - 4) >> 8);
            naluData[3] = (uint8_t) ((naluSize - 4) & 0xFF);
            bool result = MP4WriteSample(recordCtx->m_mp4FHandle
                    , recordCtx->m_vTrackId
                    , naluData
                    , (uint32_t) naluSize
                    , MP4_INVALID_DURATION
                    , 0
                    , 0);
            if (!result) {
                printf("MartinDev HHMp4v2 error: packMp4Video MP4WriteSample failed.");
                return -1;
            }
            retNum = naluSize;
            printf("MartinDev HHMp4v2 ok : packMp4Video success.");
            break;
        }
        default:
            break;
    }
    return retNum;
}

int packMp4Audio(MP4V2_CONTEXT * recordCtx, uint8_t * data, int len)
{
    if (!recordCtx->m_mp4FHandle) {
        printf("MartinDev HHMp4v2 error: file handler not init yet");
        return -1;
    }
    if (MP4_INVALID_TRACK_ID == recordCtx->m_vTrackId) {
        printf("MartinDev HHMp4v2 error: Video track id invalid.");
        return -1;
    }
    if (MP4_INVALID_TRACK_ID == recordCtx->m_aTrackId) {//add audio track id
        recordCtx->m_aTrackId =MP4AddAudioTrack(recordCtx->m_mp4FHandle
                , (uint32_t) recordCtx->m_aSamplerate
                , 1024
                , MP4_MPEG4_AUDIO_TYPE);
        if (MP4_INVALID_TRACK_ID == recordCtx->m_aTrackId) {
            printf("MartinDev HHMp4v2 error: add audio track failed, invalid track id.");
            return -1;
        }
        MP4SetAudioProfileLevel(recordCtx->m_mp4FHandle, 0x2);
        unsigned char pConfig[2] = {0};
        GetConfiguration(2, recordCtx->m_aSamplerate, recordCtx->m_aChannel, pConfig, 2);
        MP4SetTrackESConfiguration(recordCtx->m_mp4FHandle, recordCtx->m_aTrackId, pConfig, 2);
    }
    if (MP4_INVALID_TRACK_ID == recordCtx->m_aTrackId) {
        printf("MartinDev HHMp4v2 error: Audio track id invalid.");
        return -1;
    }
    MP4WriteSample(recordCtx->m_mp4FHandle, recordCtx->m_aTrackId, (const uint8_t*)&data[7], len-7, MP4_INVALID_DURATION, 0, 1);

    return len;
}

void closeMp4Handler(MP4V2_CONTEXT * recordCtx)
{
    if (recordCtx) {
        if (MP4_INVALID_FILE_HANDLE != recordCtx->m_mp4FHandle) {
            MP4Close(recordCtx->m_mp4FHandle, 0);
            recordCtx->m_mp4FHandle = NULL;
        }
        free(recordCtx);
        recordCtx = NULL;
    }
}

int64_t GetMilliSecondTime() {
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

int getNALUType(uint8_t * nalu)
{
    int type = -1;
    if (nalu[0] == 0 && nalu[1] == 0 && nalu[2] == 0 && nalu[3] == 1) {
        if ((nalu[4] & 0x1F) == _NALU_SPS_) {
            return _NALU_SPS_;
        } else if ((nalu[4] & 0x1F) == _NALU_PPS_) {
            return _NALU_PPS_;
        } else if ((nalu[4] & 0x1F) == _NALU_IDR_) {
            return _NALU_IDR_;
        } else if ((nalu[4] & 0x1F) == _NALU_BP_) {
            return _NALU_BP_;
        } else {
            return type;
        }
    }else {
        return type;
    }
}

bool GetConfiguration(int aactype, int sampleRate,int chnCount, unsigned char* cfgBuf, int cfgBufLen)
{
    /*前五个位为 AAC object types  LOW     2
     接着4个位为 码率index        16000      8
     接着4个位为 channels 个数                 1
     应打印出的正确2进制形式为  00010 | 1000 | 0001 | 000
     */
    cfgBuf[0] = (uint8_t) ((aactype & 0x1f) << 3);
    int index = 0;
    if (92017 <= sampleRate) {index = 0;}
    else if (75132 <= sampleRate){ index = 1;}
    else if (55426 <= sampleRate){ index = 2;}
    else if (46009 <= sampleRate){ index = 3;}
    else if (37566 <= sampleRate){ index = 4;}
    else if (27713 <= sampleRate){ index = 5;}
    else if (23004 <= sampleRate){ index = 6;}
    else if (18783 <= sampleRate){ index = 7;}
    else if (13856 <= sampleRate){ index = 8;}
    else if (11502 <= sampleRate){ index = 9;}
    else if (9391 <= sampleRate){ index = 10;}
    else{ index = 11;}
    cfgBuf[0] += (uint8_t) ((index & 0x0f) >> 1);
    cfgBuf[1] = (uint8_t) ((index & 0x01) << 7);
    cfgBuf[1] += (uint8_t) ((chnCount & 0x0f) << 3);
    return true;
}