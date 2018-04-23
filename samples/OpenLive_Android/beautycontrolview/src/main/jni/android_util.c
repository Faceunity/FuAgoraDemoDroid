#include "android_util.h"

void readAAsset(AAssetManager *mgr, const char *file, void **data, int *size) {
    if (mgr == NULL) {
        LOGE("readAssets AAssetManager == NULL");
    }

    AAsset *asset = AAssetManager_open(mgr, file, AASSET_MODE_UNKNOWN);
    if (asset == NULL) {
        LOGE("readAssets asset == NULL");
    }
    /*获取文件大小*/
    off_t bufferSize = AAsset_getLength(asset);
    *data = malloc(bufferSize);
    *size = AAsset_read(asset, *data, bufferSize);
    LOGE("readAssets  bufferSize %d numBytesRead %d", bufferSize, *size);
    AAsset_close(asset);
}

void readAssets(JNIEnv *env, jobject assetManager, const char *file, void **data, int *size) {
    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);

    readAAsset(mgr, file, data, size);
}