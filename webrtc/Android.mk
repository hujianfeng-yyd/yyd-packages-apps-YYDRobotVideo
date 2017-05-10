LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE = YYDRobotVideo
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS = APPS
LOCAL_MODULE_SUFFIX := $(COMMON_ANDROID_PACKAGE_SUFFIX)
LOCAL_CERTIFICATE = platform
LOCAL_SRC_FILES = YYDRobotVideo_WebRtc.apk

LOCAL_MULTILIB :=32
LOCAL_PREBUILT_JNI_LIBS:= \
  @lib/armeabi-v7a/libjingle_peerconnection_so.so
  
include $(BUILD_PREBUILT)  

