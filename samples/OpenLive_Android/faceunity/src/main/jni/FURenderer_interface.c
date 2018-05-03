#include <jni.h>
#include <string.h>

#include "authpack.h"
#include "android_util.h"
#include "faceunity_renderer.h"
//#include "android_native_interface.h"

AAssetManager *assetManager;

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_initFURenderer(JNIEnv *env, jclass type,
                                                              jobject manager) {
    AAssetManager *mgr = AAssetManager_fromJava(env, manager);
    //读取assets中的v3.bundle数据，并初始化
    void *v3;
    int v3Size;
    readAAsset(mgr, "v3.bundle", &v3, &v3Size);

    //读取assets中的anim_model.bundle数据
    void *anim;
    int animSize;
    readAAsset(mgr, "anim_model.bundle", &anim, &animSize);

    //读取assets中的ardata_ex.bundle数据
    void *arData;
    int arDataSize;
    readAAsset(mgr, "ardata_ex.bundle", &arData, &arDataSize);

    initFURenderer(g_auth_package, sizeof(g_auth_package), v3, v3Size, anim, animSize, arData,
                   arDataSize);

    free(v3);
    free(anim);
    free(arData);

    assetManager = mgr;
}

void onSurfaceCreateInner() {
    //读取assets中的face_beautification.bundle数据，并初始化
    void *beautification;
    int beautificationSize;
    readAAsset(assetManager, "face_beautification.bundle", &beautification, &beautificationSize);

    //读取assets中的face_beautification.bundle数据，并初始化
    void *fxaa;
    int fxaaSize;
    readAAsset(assetManager, "fxaa.bundle", &fxaa, &fxaaSize);

    onSurfaceCreated(beautification, beautificationSize, fxaa, fxaaSize);

    free(beautification);
    free(fxaa);
}

void onSurfaceCreate() {
    fuAndroidNativeCreateEGLContext();

    onSurfaceCreateInner();
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onSurfaceCreated(JNIEnv *env, jobject instance,
                                                                jobject manager) {
    onSurfaceCreateInner();
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onSurfaceChanged(JNIEnv *env, jobject instance,
                                                                jint wight, jint height) {
    onSurfaceChanged(0, 0, wight, height);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onDrawFrame(JNIEnv *env, jobject instance,
                                                           jbyteArray img_, jint textureId,
                                                           jint width, jint height,
                                                           jfloatArray mtx_) {

    jbyte *img = (*env)->GetByteArrayElements(env, img_, NULL);
    jfloat *mtx = (*env)->GetFloatArrayElements(env, mtx_, NULL);

    onDrawFrame(img, textureId, width, height, mtx);

    (*env)->ReleaseByteArrayElements(env, img_, img, 0);
    (*env)->ReleaseFloatArrayElements(env, mtx_, mtx, 0);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onSurfaceDestroy(JNIEnv *env, jobject instance) {
    onSurfaceDestroy();
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_switchCamera(JNIEnv *env, jobject instance,
                                                            jint cameraType,
                                                            jint inputImageOrientation) {
    switchCamera(cameraType, inputImageOrientation);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_resetStatus(JNIEnv *env, jobject instance) {

    resetStatus();
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onEffectSelected(JNIEnv *env, jobject instance,
                                                                jobject assetManager_,
                                                                jstring effectItemName_) {
    const char *effectItemName = (*env)->GetStringUTFChars(env, effectItemName_, 0);
    //判断当前显示的道具状态，并加载相应的道具
    void *effectData = NULL;
    int effectSize = 0;
    if (strncmp("none", effectItemName, 4) != 0)
        readAAsset(assetManager, effectItemName, &effectData, &effectSize);
    createEffect(effectItemName, effectData, effectSize);
    free(effectData);
    (*env)->ReleaseStringUTFChars(env, effectItemName_, effectItemName);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onFilterLevelSelected(JNIEnv *env, jobject instance,
                                                                     jfloat progress) {
    onFilterLevelSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onFilterSelected(JNIEnv *env, jobject instance,
                                                                jstring filterName_) {
    const char *filterName = (*env)->GetStringUTFChars(env, filterName_, 0);
    onFilterSelected(filterName);
//    (*env)->ReleaseStringUTFChars(env, filterName_, filterName);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onALLBlurLevelSelected(JNIEnv *env, jobject instance,
                                                                      jfloat isAll) {
    onALLBlurLevelSelected(isAll);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onBeautySkinTypeSelected(JNIEnv *env,
                                                                        jobject instance,
                                                                        jfloat skinType) {
    onBeautySkinTypeSelected(skinType);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onBlurLevelSelected(JNIEnv *env, jobject instance,
                                                                   jfloat level) {
    onBlurLevelSelected(level);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onColorLevelSelected(JNIEnv *env, jobject instance,
                                                                    jfloat progress) {
    onColorLevelSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onRedLevelSelected(JNIEnv *env, jobject instance,
                                                                  jfloat progress) {
    onRedLevelSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onBrightEyesSelected(JNIEnv *env, jobject instance,
                                                                    jfloat progress) {
    onBrightEyesSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onBeautyTeethSelected(JNIEnv *env, jobject instance,
                                                                     jfloat progress) {
    onBeautyTeethSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onFaceShapeSelected(JNIEnv *env, jobject instance,
                                                                   jfloat faceShape) {
    onFaceShapeSelected(faceShape);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onEnlargeEyeSelected(JNIEnv *env, jobject instance,
                                                                    jfloat progress) {
    onEnlargeEyeSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onCheekThinSelected(JNIEnv *env, jobject instance,
                                                                   jfloat progress) {
    onCheekThinSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onChinLevelSelected(JNIEnv *env, jobject instance,
                                                                   jfloat progress) {
    onChinLevelSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onForeheadLevelSelected(JNIEnv *env,
                                                                       jobject instance,
                                                                       jfloat progress) {
    onForeheadLevelSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onThinNoseLevelSelected(JNIEnv *env,
                                                                       jobject instance,
                                                                       jfloat progress) {
    onThinNoseLevelSelected(progress);
}

JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onMouthShapeSelected(JNIEnv *env, jobject instance,
                                                                    jfloat progress) {
    onMouthShapeSelected(progress);
}


JNIEXPORT void JNICALL
Java_com_faceunity_fulivenativedemo_FURenderer_onMusicFilterTime(JNIEnv *env, jobject instance,
                                                                 jlong time) {
    onMusicFilterTime(time);
}