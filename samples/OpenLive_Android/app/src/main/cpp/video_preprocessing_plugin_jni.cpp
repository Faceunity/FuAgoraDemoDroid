#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>

#include "../../../../../../libs/include/IAgoraRtcEngine.h"
#include "../../../../../../libs/include/IAgoraMediaEngine.h"

#include "video_preprocessing_plugin_jni.h"

JavaVM* javaVM;
JNIEnv* env;

jclass renderClass;
jmethodID renderItemsToYUVFrameMethod;

//Faceunity Start 使用FUManager将道具渲染到原始数据上
void renderItemsToYUVFrame(void* yBuffer, void* uBuffer, void* vBuffer, int yStride, int uStride, int vStride, int width, int height, int rotation) {
    javaVM->AttachCurrentThread(&env, NULL);

    renderItemsToYUVFrameMethod = env->GetStaticMethodID(renderClass, "renderItemsToYUVFrame", "(JJJIIIIII)V");

    env->CallStaticVoidMethod(renderClass, renderItemsToYUVFrameMethod, (jlong) yBuffer, (jlong) uBuffer, (jlong) vBuffer, yStride, uStride, vStride, width, height, rotation);

    javaVM->DetachCurrentThread();
}
//Faceunity End

class AgoraVideoFrameObserver : public agora::media::IVideoFrameObserver
{
public:
    virtual bool onCaptureVideoFrame(VideoFrame& videoFrame) override
    {
        //Faceunity Start 调用方法
        renderItemsToYUVFrame(videoFrame.yBuffer, videoFrame.uBuffer, videoFrame.vBuffer, videoFrame.yStride, videoFrame.uStride, videoFrame.vStride, videoFrame.width, videoFrame.height, videoFrame.rotation);
        //Faceunity End

        return true;
	}

    virtual bool onRenderVideoFrame(unsigned int uid, VideoFrame& videoFrame) override
    {
        return true;
    }
};

static AgoraVideoFrameObserver s_videoFrameObserver;
static agora::rtc::IRtcEngine* rtcEngine = NULL;

#ifdef __cplusplus
extern "C" {
#endif

int __attribute__((visibility("default"))) loadAgoraRtcEnginePlugin(agora::rtc::IRtcEngine* engine)
{
    __android_log_print(ANDROID_LOG_ERROR, "plugin", "plugin loadAgoraRtcEnginePlugin");
    rtcEngine = engine;
    return 0;
}

void __attribute__((visibility("default"))) unloadAgoraRtcEnginePlugin(agora::rtc::IRtcEngine* engine)
{
    __android_log_print(ANDROID_LOG_ERROR, "plugin", "plugin unloadAgoraRtcEnginePlugin");
    rtcEngine = NULL;
}

JNIEXPORT void JNICALL Java_io_agora_propeller_preprocessing_VideoPreProcessing_enablePreProcessing
  (JNIEnv *env, jobject obj, jboolean enable)
{
    if (!rtcEngine)
        return;
    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtcEngine, agora::rtc::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {
        if (enable) {
            mediaEngine->registerVideoFrameObserver(&s_videoFrameObserver);
        } else {
            mediaEngine->registerVideoFrameObserver(NULL);
        }
    }

    //Faceunity Start 获取JavaVM和Java类FUManager
    env->GetJavaVM(&javaVM);

    renderClass = env->FindClass("com/faceunity/FUManager");
    renderClass = (jclass) env->NewGlobalRef(renderClass);
    //Faceunity End
}

#ifdef __cplusplus
}
#endif
