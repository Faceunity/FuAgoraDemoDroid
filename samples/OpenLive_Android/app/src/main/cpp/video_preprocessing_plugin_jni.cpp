#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <dlfcn.h>

#include "../../../../../../libs/include/IAgoraRtcEngine.h"
#include "../../../../../../libs/include/IAgoraMediaEngine.h"

#include "video_preprocessing_plugin_jni.h"

void (*onSurfaceCreated)();
void (*onDrawFrame)(void*, void*, void*, int, int, int, int, int, int);
void (*onSurfaceDestroy)();

const int status_init = 0;
const int status_running = 1;
const int status_kill = 2;
const int status_dead = 3;

int status;

class AgoraVideoFrameObserver : public agora::media::IVideoFrameObserver
{
public:
    virtual bool onCaptureVideoFrame(VideoFrame& videoFrame) override
    {
        switch (status) {
            case status_init:
                onSurfaceCreated();

                status = status_running;
            case status_running:
                onDrawFrame(videoFrame.yBuffer, videoFrame.uBuffer, videoFrame.vBuffer,
                            videoFrame.yStride, videoFrame.uStride, videoFrame.vStride,
                            videoFrame.width, videoFrame.height, videoFrame.rotation);
                break;
            case status_kill:
                onSurfaceDestroy();

                status = status_dead;
                break;
            default:break;
        }

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

#ifdef __cplusplus
}
#endif
