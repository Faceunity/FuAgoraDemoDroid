#include <jni.h>
#include <android/log.h>
#include <stdlib.h>

#include "../../../../../../libs/include/IAgoraRtcEngine.h"
#include "../../../../../../libs/include/IAgoraMediaEngine.h"

#include "video_preprocessing_plugin_jni.h"

JavaVM* javaVM;
JNIEnv* env;

jclass renderClass;
jmethodID fuRenderToNV21ImageMethod;

class AgoraVideoFrameObserver : public agora::media::IVideoFrameObserver
{
public:
    virtual bool onCaptureVideoFrame(VideoFrame& videoFrame) override
    {
        javaVM->AttachCurrentThread(&env, NULL);

        fuRenderToNV21ImageMethod = env->GetStaticMethodID(renderClass, "fuRenderToNV21Image", "([BII)V");

        jsize len = videoFrame.width * videoFrame.height * 3 / 2;
        jbyteArray array = env->NewByteArray(len);
        jbyte buf[len];
        int yLength = videoFrame.width * videoFrame.height;
        int i = 0;
        for (; i < yLength; i++) {
            buf[i] = ((jbyte *) videoFrame.yBuffer)[i];
        }
        int uLength = yLength / 4;
        for (int j = 0; j < uLength; i += 2, j++) {
            buf[i] = ((jbyte *) videoFrame.vBuffer)[j];
            buf[i + 1] = ((jbyte *) videoFrame.uBuffer)[j];
        }
        env->SetByteArrayRegion(array, 0, len, buf);

        env->CallStaticVoidMethod(renderClass, fuRenderToNV21ImageMethod, array, videoFrame.width, videoFrame.height);

        env->GetByteArrayRegion(array, 0, len, buf);
        for (i = 0; i < yLength; i++) {
            ((jbyte *) videoFrame.yBuffer)[i] = buf[i];
        }
        for (int j = 0; j < uLength; i += 2, j++) {
            ((jbyte *) videoFrame.vBuffer)[j] = buf[i];
            ((jbyte *) videoFrame.uBuffer)[j] = buf[i + 1];
        }

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
    mediaEngine.queryInterface(*rtcEngine, agora::rtc::AGORA_IID_MEDIA_ENGINE);
    if (mediaEngine) {
        if (enable) {
            mediaEngine->registerVideoFrameObserver(&s_videoFrameObserver);
        } else {
            mediaEngine->registerVideoFrameObserver(NULL);
        }
    }

    env->GetJavaVM(&javaVM);

    renderClass = env->FindClass("com/faceunity/Render");
    renderClass = (jclass) env->NewGlobalRef(renderClass);
}

#ifdef __cplusplus
}
#endif
