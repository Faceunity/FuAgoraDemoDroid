LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
# Agora Video Engine
LOCAL_MODULE := agora-av
LOCAL_SRC_FILES := ../../../../../../libs/$(TARGET_ARCH_ABI)/libHDACEngine.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
# Agora RTC SDK
LOCAL_MODULE := agora-rtc
LOCAL_SRC_FILES := ../../../../../../libs/$(TARGET_ARCH_ABI)/libagora-rtc-sdk-jni.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_SRC_FILES := \
	video_preprocessing_plugin_jni.cpp \

LOCAL_LDLIBS := -ldl -llog
LOCAL_MODULE := apm-plugin-video-preprocessing
LOCAL_SHARED_LIBRARIES := agora-av agora-rtc
include $(BUILD_SHARED_LIBRARY)
