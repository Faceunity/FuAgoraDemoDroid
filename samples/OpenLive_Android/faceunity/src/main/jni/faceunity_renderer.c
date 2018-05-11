//
// Created by Jiahua Tu on 2018/4/19.
//

#include <jni.h>
#include <string.h>

#include "GlUtils.h"
#include "android_native_interface.h"
#include "faceunity_renderer.h"

volatile int frame_id = 0;

//美颜和滤镜的默认参数
int isNeedUpdateFaceBeauty = 1;
float mFaceBeautyFilterLevel = 1.0f;//滤镜强度
char *mFilterName = "origin";

float mFaceBeautyALLBlurLevel = 1.0f;//精准磨皮
float mFaceBeautyType = 0.0f;//美肤类型
float mFaceBeautyBlurLevel = 0.7f;//磨皮
float mFaceBeautyColorLevel = 0.5f;//美白
float mFaceBeautyRedLevel = 0.5f;//红润
float mBrightEyesLevel = 0.0f;//亮眼
float mBeautyTeethLevel = 0.0f;//美牙

float mFaceBeautyFaceShape = 4.0f;//脸型
float mFaceShapeLevel = 1.0f;//程度
float mFaceBeautyEnlargeEye = 0.4f;//大眼
float mFaceBeautyCheekThin = 0.4f;//瘦脸
float mChinLevel = 0.3f;//下巴
float mForeheadLevel = 0.3f;//额头
float mThinNoseLevel = 0.5f;//瘦鼻
float mMouthShape = 0.4f;//嘴形

volatile char *mEffectName = "none";

static int ITEM_ARRAYS_FACE_BEAUTY_INDEX = 0;
static int ITEM_ARRAYS_EFFECT = 1;
static int ITEM_ARRAYS_ANIMOJI_3D = 2;
volatile int itemsArray[] = {0, 0, 0};

int mCameraType = 1;
int mInputImageOrientation = 270;

int mIsTracking = -1;
int systemErrorStatus = 0;//success number

void updateEffectItemParams(int itemHandle) {
    fuAndroidNativeItemSetParamd(itemHandle, "isAndroid", 1.0);

    //rotationAngle 参数是用于旋转普通道具
    fuAndroidNativeItemSetParamd(itemHandle, "rotationAngle", 360 - mInputImageOrientation);

    //这两句代码用于识别人脸默认方向的修改，主要针对animoji道具的切换摄像头倒置问题
    fuAndroidNativeItemSetParamd(itemHandle, "camera_change", 1.0);
    fuSetDefaultRotationMode((360 - mInputImageOrientation) / 90);
    //is3DFlipH 参数是用于对3D道具的镜像
    fuAndroidNativeItemSetParamd(itemHandle, "is3DFlipH", mCameraType == 0 ? 1 : 0);
    //isFlipExpr 参数是用于对人像驱动道具的镜像
    fuAndroidNativeItemSetParamd(itemHandle, "isFlipExpr", mCameraType == 0 ? 1 : 0);
    //loc_y_flip与loc_x_flip 参数是用于对手势识别道具的镜像
    fuAndroidNativeItemSetParamd(itemHandle, "loc_y_flip", mCameraType == 0 ? 1 : 0);
    fuAndroidNativeItemSetParamd(itemHandle, "loc_x_flip", mCameraType == 0 ? 1 : 0);
}

void createEffect(char *name, void *effectData, int effectSize) {

    if (strncmp("none", name, 4) == 0) {
        if (itemsArray[ITEM_ARRAYS_EFFECT] > 0)
            fuAndroidNativeDestroyItem(itemsArray[ITEM_ARRAYS_EFFECT]);
        itemsArray[ITEM_ARRAYS_EFFECT] = 0;
    } else {
        int temp = itemsArray[ITEM_ARRAYS_EFFECT];
        itemsArray[ITEM_ARRAYS_EFFECT] = fuAndroidNativeCreateItemFromPackage(effectData,
                                                                              effectSize);
        updateEffectItemParams(itemsArray[ITEM_ARRAYS_EFFECT]);
        if (temp > 0) {
            fuAndroidNativeDestroyItem(temp);
        }
    }
}

void initFURenderer(void *auth, int authSize, void *v3, int v3Size, void *anim, int animSize,
                    void *arData, int arDataSize) {

    fuAndroidNativeSetup(v3, v3Size, auth, authSize);
    fuLoadAnimModel(anim, animSize);
    fuLoadExtendedARData(arData, arDataSize);
}

void onSurfaceCreated(void *beautification, int beautificationSize, void *fxaa, int fxaaSize) {
    LOGE("version %s ", fuAndroidNativGetVersion());
    //初始化opengl绘画需要的program
    createProgram();

    itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = fuAndroidNativeCreateItemFromPackage(beautification,
                                                                                     beautificationSize);

    itemsArray[ITEM_ARRAYS_ANIMOJI_3D] = fuAndroidNativeCreateItemFromPackage(fxaa, fxaaSize);

    fuSetExpressionCalibration(1);
//    fuAndroidNativeSetDefaultOrientation((360 - mInputImageOrientation) / 90);
}

void onSurfaceChanged(int x, int y, int width, int height) {
    glViewport(x, y, width, height);
}

void preDrawFrame() {
    int systemError = fuAndroidNativeGetSystemError();
    if (systemError != systemErrorStatus) {
        systemErrorStatus = systemError;
        LOGE("system error %d %s", systemError, fuAndroidNativeGetSystemErrorString(systemError));
    }

    int isTracking = fuAndroidNativeIsTracking();
    if (mIsTracking != isTracking) {
        LOGE("isTracking %d ", isTracking);
        mIsTracking = isTracking;
    }

    //修改美颜参数
    if (isNeedUpdateFaceBeauty && itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] != 0) {
        //filter_level 滤镜强度 范围0~1 SDK默认为 1
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "filter_level",
                                     mFaceBeautyFilterLevel);
        //filter_name 滤镜
        fuAndroidNativeItemSetParams(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "filter_name",
                                     mFilterName);

        //skin_detect 精准美肤 0:关闭 1:开启 SDK默认为 0
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "skin_detect",
                                     mFaceBeautyALLBlurLevel);
        //heavy_blur 美肤类型 0:清晰美肤 1:朦胧美肤 SDK默认为 0
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "heavy_blur",
                                     mFaceBeautyType);
        //blur_level 磨皮 范围0~6 SDK默认为 6
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "blur_level",
                                     6.0f * mFaceBeautyBlurLevel);
        //blur_blend_ratio 磨皮结果和原图融合率 范围0~1 SDK默认为 1
//          fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "blur_blend_ratio", 1);

        //color_level 美白 范围0~1 SDK默认为 1
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "color_level",
                                     mFaceBeautyColorLevel);
        //red_level 红润 范围0~1 SDK默认为 1
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "red_level",
                                     mFaceBeautyRedLevel);
        //eye_bright 亮眼 范围0~1 SDK默认为 0
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "eye_bright",
                                     mBrightEyesLevel);
        //tooth_whiten 美牙 范围0~1 SDK默认为 0
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "tooth_whiten",
                                     mBeautyTeethLevel);

        //face_shape_level 美型程度 范围0~1 SDK默认为1
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape_level",
                                     mFaceShapeLevel);
        //face_shape 脸型 0：女神 1：网红 2：自然 3：默认 SDK默认为 3
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "face_shape",
                                     mFaceBeautyFaceShape);
        //eye_enlarging 大眼 范围0~1 SDK默认为 0
        //cheek_thinning 瘦脸 范围0~1 SDK默认为 0
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "eye_enlarging",
                                     mFaceBeautyEnlargeEye);
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX],
                                     "cheek_thinning", mFaceBeautyCheekThin);
        //intensity_chin 下巴 范围0~1 SDK默认为 0.5    大于0.5变大，小于0.5变小
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_chin",
                                     mChinLevel);
        //intensity_forehead 额头 范围0~1 SDK默认为 0.5    大于0.5变大，小于0.5变小
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX],
                                     "intensity_forehead", mForeheadLevel);
        //intensity_nose 鼻子 范围0~1 SDK默认为 0
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_nose",
                                     mThinNoseLevel);
        //intensity_mouth 嘴型 范围0~1 SDK默认为 0.5   大于0.5变大，小于0.5变小
        fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX], "intensity_mouth",
                                     mMouthShape);
        isNeedUpdateFaceBeauty = 0;
    }
}

int rotationNow = -1;

void onDrawFrameYuv(void* yBuffer, void* uBuffer, void* vBuffer, int yStride, int uStride, int vStride, int width, int height, int rotation) {
    preDrawFrame();

    if (rotationNow == -1) {
        rotationNow = rotation;
    }

    if (rotationNow != rotation) {
        fuAndroidNativeClearReadbackRelated();
        fuOnCameraChange();
        rotationNow = rotation;
    }

    fuAndroidNativeRenderToYUVImage(yBuffer, uBuffer, vBuffer, yStride, uStride, vStride, width, height, frame_id++, itemsArray, sizeof(itemsArray) / sizeof(int), 0);
}

void onDrawFrame(void *img, int textureId, int width, int height, float *mtx) {
    preDrawFrame();

    int flags = FU_ADM_FLAG_EXTERNAL_OES_TEXTURE;
    if (mCameraType == 0) {
        flags |= ANDROID_NATIVE_NAMA_RENDER_OPTION_FLIP_X;
    }
    int texture = fuAndroidNativeDualInputToTexture(img, (GLuint) textureId, flags, width, height,
                                                    frame_id++, itemsArray,
                                                    sizeof(itemsArray) / sizeof(int), NULL, width,
                                                    height, NULL, 0);

    //把处理完的纹理绘制到屏幕上
    drawFrame(texture, mtx);
}

void onSurfaceDestroy() {
    resetStatus();
    fuAndroidNativeDestroyAllItems();
    fuAndroidNativeOnDeviceLost();
    releaseProgram();
}

void onSurfaceDestroyed() {
    onSurfaceDestroy();

    fuAndroidNativeReleaseEGLContext();
}

void switchCamera(int cameraType, int inputImageOrientation) {
    mCameraType = cameraType;
    mInputImageOrientation = inputImageOrientation;
    fuOnCameraChange();
    updateEffectItemParams(itemsArray[ITEM_ARRAYS_EFFECT]);
//    fuAndroidNativeSetDefaultOrientation((360 - mInputImageOrientation) / 90);
}

void resetStatus() {

    frame_id = 0;

    isNeedUpdateFaceBeauty = 1;
    mFaceBeautyFilterLevel = 1.0f;//滤镜强度
    mFilterName = "origin";

    mFaceBeautyALLBlurLevel = 1.0f;//精准磨皮
    mFaceBeautyType = 0.0f;//美肤类型
    mFaceBeautyBlurLevel = 0.7f;//磨皮
    mFaceBeautyColorLevel = 0.5f;//美白
    mFaceBeautyRedLevel = 0.5f;//红润
    mBrightEyesLevel = 0.0f;//亮眼
    mBeautyTeethLevel = 0.0f;//美牙

    mFaceBeautyFaceShape = 3.0f;//脸型
    mFaceShapeLevel = 1.0f;//程度
    mFaceBeautyEnlargeEye = 0.4f;//大眼
    mFaceBeautyCheekThin = 0.4f;//瘦脸
    mChinLevel = 0.3f;//下巴
    mForeheadLevel = 0.3f;//额头
    mThinNoseLevel = 0.5f;//瘦鼻
    mMouthShape = 0.4f;//嘴形

    mEffectName = "none";

    itemsArray[ITEM_ARRAYS_ANIMOJI_3D] = itemsArray[ITEM_ARRAYS_FACE_BEAUTY_INDEX] = itemsArray[ITEM_ARRAYS_EFFECT] = 0;

    mIsTracking = 0;
}

void onFilterLevelSelected(float progress) {
    mFaceBeautyFilterLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onFilterSelected(char *filterName) {
    mFilterName = filterName;
    isNeedUpdateFaceBeauty = 1;
}

void onALLBlurLevelSelected(float isAll) {
    mFaceBeautyALLBlurLevel = isAll;
    isNeedUpdateFaceBeauty = 1;
}

void onBeautySkinTypeSelected(float skinType) {
    mFaceBeautyType = skinType;
    isNeedUpdateFaceBeauty = 1;
}

void onBlurLevelSelected(float level) {
    mFaceBeautyBlurLevel = level;
    isNeedUpdateFaceBeauty = 1;
}

void onColorLevelSelected(float progress) {
    mFaceBeautyColorLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onRedLevelSelected(float progress) {
    mFaceBeautyRedLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onBrightEyesSelected(float progress) {
    mBrightEyesLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onBeautyTeethSelected(float progress) {
    mBeautyTeethLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onFaceShapeSelected(float faceShape) {
    mFaceBeautyFaceShape = faceShape;
    isNeedUpdateFaceBeauty = 1;
}

void onEnlargeEyeSelected(float progress) {
    mFaceBeautyEnlargeEye = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onCheekThinSelected(float progress) {
    mFaceBeautyCheekThin = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onChinLevelSelected(float progress) {
    mChinLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onForeheadLevelSelected(float progress) {
    mForeheadLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onThinNoseLevelSelected(float progress) {
    mThinNoseLevel = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onMouthShapeSelected(float progress) {
    mMouthShape = progress;
    isNeedUpdateFaceBeauty = 1;
}

void onMusicFilterTime(long time) {
    fuAndroidNativeItemSetParamd(itemsArray[ITEM_ARRAYS_EFFECT], "music_time", time);
}
