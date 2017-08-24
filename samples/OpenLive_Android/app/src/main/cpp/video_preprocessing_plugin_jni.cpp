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
jmethodID renderToI420ImageMethod;

class AgoraVideoFrameObserver : public agora::media::IVideoFrameObserver
{
public:
    virtual bool onCaptureVideoFrame(VideoFrame& videoFrame) override
    {
        javaVM->AttachCurrentThread(&env, NULL);

        renderToI420ImageMethod = env->GetStaticMethodID(renderClass, "renderToI420Image", "([BII)V");

        jsize len = videoFrame.yStride * videoFrame.height * 3 / 2;
        jbyteArray array = env->NewByteArray(len);
        jbyte* buf = new jbyte[len];
        int yLength = videoFrame.yStride * videoFrame.height;
        memcpy(buf, videoFrame.yBuffer, yLength);
        int uLength = yLength / 4;
        memcpy(buf + yLength, videoFrame.uBuffer, uLength);
        memcpy(buf + yLength + uLength, videoFrame.vBuffer, uLength);
        env->SetByteArrayRegion(array, 0, len, buf);

        env->CallStaticVoidMethod(renderClass, renderToI420ImageMethod, array, videoFrame.yStride, videoFrame.height);

        env->GetByteArrayRegion(array, 0, len, buf);
        memcpy(videoFrame.yBuffer, buf, yLength);
        memcpy(videoFrame.uBuffer, buf + yLength, uLength);
        memcpy(videoFrame.vBuffer, buf + yLength + uLength, uLength);

        delete []buf;

        env->DeleteLocalRef(array);

        javaVM->DetachCurrentThread();

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

    env->GetJavaVM(&javaVM);

    renderClass = env->FindClass("com/faceunity/MRender");
    renderClass = (jclass) env->NewGlobalRef(renderClass);
}

#ifdef __cplusplus
}
#endif
