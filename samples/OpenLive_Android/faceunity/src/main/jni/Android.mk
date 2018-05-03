LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := nama
LOCAL_SRC_FILES := $(TARGET_ARCH_ABI)/libnama.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := faceunity-native
LOCAL_SRC_FILES := FURenderer_interface.c faceunity_renderer.c android_util.c android_util.h GlUtils.c GlUtils.h android_native_interface.h
LOCAL_SHARED_LIBRARIES := nama
LOCAL_LDLIBS := -llog -landroid -lGLESv2
include $(BUILD_SHARED_LIBRARY)

