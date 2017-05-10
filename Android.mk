LOCAL_PATH:= $(call my-dir)

ifeq ($(VIDEO_TYPE), huanxin)
$(warning "huanxin============########") 
	include $(LOCAL_PATH)/huanxin/Android.mk
else ifeq ($(VIDEO_TYPE), shengwang)
$(warning "shengwang============########") 
	include $(LOCAL_PATH)/shengwang/Android.mk
else ifeq ($(VIDEO_TYPE), webrtc)
$(warning "webrtc============########") 
	include $(LOCAL_PATH)/webrtc/Android.mk
else ifeq ($(VIDEO_TYPE), tutk)
	include $(LOCAL_PATH)/tutk/Android.mk
else
$(warning "#########=====$$$$$$$$===########") 
	include $(LOCAL_PATH)/huanxin/Android.mk
endif