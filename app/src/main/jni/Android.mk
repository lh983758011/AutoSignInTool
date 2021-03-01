LOCAL_PATH:=$(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := server
LOCAL_SRC_FILES := server.c
LOCAL_LDLIBS +=-L$(SYSROOT)/usr/lib -lm -llog
include $(BUILD_EXECUTABLE)