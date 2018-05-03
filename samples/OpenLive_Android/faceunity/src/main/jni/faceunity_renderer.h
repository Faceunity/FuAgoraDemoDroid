//
// Created by Jiahua Tu on 2018/4/19.
//

#ifndef FULIVENATIVEDEMO_FACEUNITY_RENDERER_H
#define FULIVENATIVEDEMO_FACEUNITY_RENDERER_H

void initFURenderer(void *auth, int authSize, void *v3, int v3Size, void *anim, int animSize,
                    void *arData, int arDataSize);

void createEffect(char *name, void *effectData, int effectSize);

void onSurfaceCreated(void *beautification, int beautificationSize, void *fxaa, int fxaaSize);

void onSurfaceChanged(int x, int y, int width, int height);

void onDrawFrame(void *img, int textureId, int width, int height, float *mtx);

void onSurfaceDestroy();

void switchCamera(int cameraType, int inputImageOrientation);

void resetStatus();

void onFilterLevelSelected(float progress);

void onFilterSelected(char *filterName);

void onALLBlurLevelSelected(float isAll);

void onBeautySkinTypeSelected(float skinType);

void onBlurLevelSelected(float level);

void onColorLevelSelected(float progress);

void onRedLevelSelected(float progress);

void onBrightEyesSelected(float progress);

void onBeautyTeethSelected(float progress);

void onFaceShapeSelected(float faceShape);

void onEnlargeEyeSelected(float progress);

void onCheekThinSelected(float progress);

void onChinLevelSelected(float progress);

void onForeheadLevelSelected(float progress);

void onThinNoseLevelSelected(float progress);

void onMouthShapeSelected(float progress);

void onMusicFilterTime(long time);

#endif //FULIVENATIVEDEMO_FACEUNITY_RENDERER_H
