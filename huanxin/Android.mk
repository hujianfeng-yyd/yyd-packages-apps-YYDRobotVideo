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
LOCAL_MODULE := libeasemob_jni
ifeq ($(TARGET_ARCH),arm64)
	LOCAL_SRC_FILES_64 := libs/arm64-v8a/libeasemob_jni.so
	LOCAL_MULTILIB := 64
else ifeq ($(TARGET_ARCH),arm)
	LOCAL_SRC_FILES_32 := libs/armeabi/libeasemob_jni.so
	LOCAL_MULTILIB := 32
endif
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)
LOCAL_MODULE := libeasemobservice
ifeq ($(TARGET_ARCH),arm64)
	LOCAL_SRC_FILES_64 := libs/arm64-v8a/libeasemobservice.so
	LOCAL_MULTILIB := 64
else ifeq ($(TARGET_ARCH),arm)
	LOCAL_SRC_FILES_32 := libs/armeabi/libeasemobservice.so
	LOCAL_MULTILIB := 32
endif
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
	libeasemobchat \
	libvideogson \
	libvideogson \
	libvideomsc
	
LOCAL_JNI_SHARED_LIBRARIES += libmsc libeasemob_jni libeasemobservice

ifeq ($(TARGET_ARCH),arm64)
	LOCAL_MULTILIB := 64
else ifeq ($(TARGET_ARCH),arm)
	LOCAL_MULTILIB := 32
endif

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)	

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libeasemobchat:libs/easemobchat_2.2.4.jar \
	libvideogson:libs/gson-2.3.1.jar \
	libvideomsc:libs/Msc.jar
	
include $(BUILD_MULTI_PREBUILT)
