# FuAgoraDemoDroid
FuAgoraDemoDroid 是集成了 Faceunity 面部跟踪和虚拟道具功能和声网[视频通话 + 直播 SDK](https://www.agora.io/cn/download/)的 Demo 。
本文是 FaceUnity SDK 快速对接声网的导读说明，关于 FaceUnity SDK 的更多详细说明，请参看 [FULiveDemo](https://github.com/Faceunity/FULiveDemoDroid/tree/dev)
## 快速集成方法
### 添加module
添加 faceunity module 到工程中，在 app dependencies 里添加 `compile project(':faceunity')`
### 修改代码
#### 初始化 nama 库
在 AGApplication 的 onCreate 方法中添加
```
FURenderer.initFURenderer(getAssets());
```
#### 加载美颜和默认道具
在LiveRoomActivity的onCreate方法中添加
```
mFURenderer = new FURenderer();
```
#### 开启与关闭
在WorkerThread的
joinChannel方法中添加代码（开启美颜处理）
```
new VideoPreProcessing().enablePreProcessing(true);
```
leaveChannel方法中添加代码（关闭美颜处理）
```
new VideoPreProcessing().enablePreProcessing(false);
```
#### 渲染道具到原始数据上
在 video_preprocessing_plugin_jni.cpp 里
##### 增加变量
```
void (*onSurfaceCreated)();
void (*onDrawFrame)(void*, void*, void*, int, int, int, int, int, int);
void (*onSurfaceDestroy)();

const int status_init = 0;
const int status_running = 1;
const int status_kill = 2;
const int status_dead = 3;

int status;
```
##### 修改onCaptureVideoFrame
```
virtual bool onCaptureVideoFrame(VideoFrame& videoFrame) override
{
    switch (status) {
        case status_init:
            //初始化并加载默认道具美颜
            onSurfaceCreated();

            status = status_running;
        case status_running:
            //将道具渲染到原始数据上
            onDrawFrame(videoFrame.yBuffer, videoFrame.uBuffer, videoFrame.vBuffer,
                        videoFrame.yStride, videoFrame.uStride, videoFrame.vStride,
                        videoFrame.width, videoFrame.height, videoFrame.rotation);
            break;
        case status_kill:
            //销毁
            onSurfaceDestroy();

            status = status_dead;
            break;
        default:break;
    }

    return true;
}
```
##### 修改Java_io_agora_propeller_preprocessing_VideoPreProcessing_enablePreProcessing
初始化函数指针和开关美颜处理
```
JNIEXPORT void JNICALL Java_io_agora_propeller_preprocessing_VideoPreProcessing_enablePreProcessing
  (JNIEnv *env, jobject obj, jboolean enable)
{
    if (!rtcEngine)
        return;
    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtcEngine, agora::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {
        if (enable) {
            status = status_init;
            mediaEngine->registerVideoFrameObserver(&s_videoFrameObserver);
        } else {
            status = status_kill;
//            mediaEngine->registerVideoFrameObserver(NULL);
        }
    }

    void* handle = dlopen("libfaceunity-native.so", RTLD_LAZY);
    onSurfaceCreated = (void (*)()) dlsym(handle, "onSurfaceCreate");
    onDrawFrame = (void (*)(void *, void *, void *, int, int, int, int, int, int)) dlsym(handle, "onDrawFrameYuv");
    onSurfaceDestroy = (void (*)()) dlsym(handle, "onSurfaceDestroyed");
}
```
### 修改默认美颜参数
修改faceunity中faceunity中以下代码
```
private float mFaceBeautyALLBlurLevel = 1.0f;//精准磨皮
private float mFaceBeautyType = 0.0f;//美肤类型
private float mFaceBeautyBlurLevel = 0.7f;//磨皮
private float mFaceBeautyColorLevel = 0.5f;//美白
private float mFaceBeautyRedLevel = 0.5f;//红润
private float mBrightEyesLevel = 1000.7f;//亮眼
private float mBeautyTeethLevel = 1000.7f;//美牙

private float mFaceBeautyFaceShape = 4.0f;//脸型
private float mFaceBeautyEnlargeEye = 0.4f;//大眼
private float mFaceBeautyCheekThin = 0.4f;//瘦脸
private float mFaceBeautyEnlargeEye_old = 0.4f;//大眼
private float mFaceBeautyCheekThin_old = 0.4f;//瘦脸
private float mChinLevel = 0.3f;//下巴
private float mForeheadLevel = 0.3f;//额头
private float mThinNoseLevel = 0.5f;//瘦鼻
private float mMouthShape = 0.4f;//嘴形
```
参数含义与取值范围参考[这里](https://github.com/Faceunity/FULiveDemoDroid/tree/dev#%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE)，如果使用界面，则需要同时修改界面中的初始值。