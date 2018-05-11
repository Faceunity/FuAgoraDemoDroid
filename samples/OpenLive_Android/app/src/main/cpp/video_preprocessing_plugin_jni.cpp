#include <jni.h>
#include <android/log.h>
#include <stdlib.h>
#include <string.h>
#include <dlfcn.h>

#include "../../../../../../libs/include/IAgoraRtcEngine.h"
#include "../../../../../../libs/include/IAgoraMediaEngine.h"

#include "video_preprocessing_plugin_jni.h"
#include <unistd.h>
#include <pthread.h>

void (*onSurfaceCreated)() = NULL;
void (*onDrawFrame)(void*, void*, void*, int, int, int, int, int, int) = NULL;
void (*onSurfaceDestroy)() = NULL;

const int status_init = 0;
const int status_running = 1;
const int status_kill = 2;
const int status_dead = 3;

int status;
agora::media::IVideoFrameObserver::VideoFrame* frame = NULL;

pthread_t thread;

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t cond = PTHREAD_COND_INITIALIZER;

void* run(void * args) {
    onSurfaceCreated();
    while (status != status_kill) {
        if (frame != NULL) {
            pthread_mutex_lock(&mutex);
            onDrawFrame(frame->yBuffer, frame->uBuffer, frame->vBuffer,
                        frame->yStride, frame->uStride, frame->vStride,
                        frame->width, frame->height, frame->rotation);
            frame = NULL;
            pthread_cond_signal(&cond);
            pthread_mutex_unlock(&mutex);
        }
        usleep(1);
    }
    onSurfaceDestroy();
    return NULL;
}

class AgoraVideoFrameObserver : public agora::media::IVideoFrameObserver
{
public:
    virtual bool onCaptureVideoFrame(VideoFrame& videoFrame) override
    {
        pthread_mutex_lock(&mutex);
        frame = &videoFrame;
        pthread_cond_wait(&cond, &mutex);
        pthread_mutex_unlock(&mutex);

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
    void* handle = dlopen("libfaceunity-native.so", RTLD_LAZY);
    if (onSurfaceCreated == NULL) {
        onSurfaceCreated = (void (*)()) dlsym(handle, "onSurfaceCreate");
        onDrawFrame = (void (*)(void *, void *, void *, int, int, int, int, int, int)) dlsym(handle, "onDrawFrameYuv");
        onSurfaceDestroy = (void (*)()) dlsym(handle, "onSurfaceDestroyed");
    }

    if (!rtcEngine)
        return;
    agora::util::AutoPtr<agora::media::IMediaEngine> mediaEngine;
    mediaEngine.queryInterface(rtcEngine, agora::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {
        if (enable) {
            status = status_init;
            pthread_create(&thread, NULL, run, NULL);
            mediaEngine->registerVideoFrameObserver(&s_videoFrameObserver);
        } else {
            mediaEngine->registerVideoFrameObserver(NULL);
            status = status_kill;
        }
    }
}

#ifdef __cplusplus
}
#endif
