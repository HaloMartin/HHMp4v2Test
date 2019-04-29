#include <jni.h>

#ifndef _Included_com_HHMp4v2_HHMp4v2
#define _Included_com_HHMp4v2_HHMp4v2
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_HHMp4v2_HHMp4v2
 * Method:    initMp4Packer
 * Signature: (Ljava/lang/String;IIIII)I
 */
JNIEXPORT jint JNICALL Java_com_HHMp4v2_HHMp4v2_initMp4Packer
        (JNIEnv *, jobject, jstring, jint, jint, jint, jint, jint);

/*
 * Class:     com_HHMp4v2_HHMp4v2
 * Method:    packMp4Video
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_com_HHMp4v2_HHMp4v2_packMp4Video
        (JNIEnv *, jobject, jbyteArray, jint);

/*
 * Class:     com_HHMp4v2_HHMp4v2
 * Method:    packMp4Audio
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_com_HHMp4v2_HHMp4v2_packMp4Audio
        (JNIEnv *, jobject, jbyteArray, jint);

/*
 * Class:     com_HHMp4v2_HHMp4v2
 * Method:    mp4Close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_HHMp4v2_HHMp4v2_mp4Close
        (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
