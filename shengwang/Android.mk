LOCAL_PATH := $(call my-dir)

#Build .so  
#include $(CLEAR_VARS)
#LOCAL_MODULE := libmsc
#LOCAL_SRC_FILES_64 := libs/arm64-v8a/libmsc.so
#LOCAL_MULTILIB := 64
#LOCAL_MODULE_CLASS := SHARED_LIBRARIES
#LOCAL_MODULE_SUFFIX := .so
#include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libagora-rtc-sdk-jni
#ifeq ($(TARGET_ARCH),arm64)
#	LOCAL_SRC_FILES_64 := libs/arm64-v8a/libagora-rtc-sdk-jni.so
#	LOCAL_MULTILIB := 64
#else ifeq ($(TARGET_ARCH),arm)
	LOCAL_SRC_FILES_32 := libs/armeabi/libagora-rtc-sdk-jni.so
	LOCAL_MULTILIB := 32
#endif
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libHDACEngine
#ifeq ($(TARGET_ARCH),arm64)
#	LOCAL_SRC_FILES_64 := libs/arm64-v8a/libHDACEngine.so
#	LOCAL_MULTILIB := 64
#else ifeq ($(TARGET_ARCH),arm)
	LOCAL_SRC_FILES_32 := libs/armeabi/libHDACEngine.so
	LOCAL_MULTILIB := 32
#endif
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)


#-----------------------------------------------------
#Build apk
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

#LOCAL_CERTIFICATE := platform

LOCAL_PACKAGE_NAME := YYDRobotVideo
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
#LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets

LOCAL_STATIC_JAVA_LIBRARIES := \
	android-support-v4 \
	libagora-rtc-sdk \
	libyydrobotvideocodec \
	libyydrobotvideocomm \
	libyydrobotvideonet \
	libyydrobotvideosdk \
	libvideogson \
	libvideomsc \
	alipaycore
	
	
LOCAL_JNI_SHARED_LIBRARIES += libmsc libagora-rtc-sdk-jni libHDACEngine

#ifeq ($(TARGET_ARCH),arm64)
#	LOCAL_MULTILIB := 64
#else ifeq ($(TARGET_ARCH),arm)
	LOCAL_MULTILIB := 32
#endif

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)	

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libagora-rtc-sdk:libs/agora-rtc-sdk.jar \
	libyydrobotvideocodec:libs/yydrobotvideocodec.jar \
	libyydrobotvideocomm:libs/yydrobotvideocomm.jar \
	libyydrobotvideonet:libs/yydrobotvideonet.jar \
	libyydrobotvideosdk:libs/yydrobotvideosdk.jar \
	libvideogson:libs/gson-2.3.1.jar \
	libvideomsc:libs/Msc.jar \
	alipaycore:libs/core-2.1.jar
	
include $(BUILD_MULTI_PREBUILT)
