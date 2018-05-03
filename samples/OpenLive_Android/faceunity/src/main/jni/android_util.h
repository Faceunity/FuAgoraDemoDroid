#ifndef FULIVENATIVEDEMO_ANDROID_LOG_H
#define FULIVENATIVEDEMO_ANDROID_LOG_H

#include <android/log.h>
#include <stdlib.h>

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define LOG_TAG "faceunity-native"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

void readAAsset(AAssetManager *mgr, const char *file, void **data, int *size);
void readAssets(JNIEnv *env, jobject assetManager, const char *file, void **data, int *size);

#endif //FULIVENATIVEDEMO_ANDROID_LOG_H
