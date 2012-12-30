LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libmtpcustom

LOCAL_SRC_FILES := \
                  mtp_custom/MtpDataPacket.cpp           \
                  mtp_custom/MtpDebug.cpp                \
                  mtp_custom/MtpDevice.cpp               \
                  mtp_custom/MtpEventPacket.cpp          \
                  mtp_custom/MtpDeviceInfo.cpp           \
                  mtp_custom/MtpObjectInfo.cpp           \
                  mtp_custom/MtpPacket.cpp               \
                  mtp_custom/MtpProperty.cpp             \
                  mtp_custom/MtpRequestPacket.cpp        \
                  mtp_custom/MtpResponsePacket.cpp       \
                  mtp_custom/MtpStorageInfo.cpp          \
                  mtp_custom/MtpStringBuffer.cpp         \
                  mtp_custom/MtpStorage.cpp              \
                  mtp_custom/MtpUtils.cpp                \
                  jni_custom/android_mtp_MtpDatabase.cpp \
                  jni_custom/android_mtp_MtpDevice.cpp   \

ANDROID_ROOT := /home/kashimoto/jcrom_work/

LOCAL_C_INCLUDES := \
                    $(ANDROID_ROOT)/system/core/include/ \
                    $(ANDROID_ROOT)/frameworks/native/include/ \
                    $(ANDROID_ROOT)/frameworks/native/include/ \
                    $(ANDROID_ROOT)/bionic/libc/kernel/common/ \
                    $(ANDROID_ROOT)/external/jhead \
                    $(ANDROID_ROOT)/libnativehelper/include/ \
                    $(ANDROID_ROOT)/libnativehelper/include/nativehelper \
                    $(ANDROID_ROOT)/frameworks/base/include/ \
                    $(LOCAL_PATH)/../mtp_custom/ \
                    $(LOCAL_PATH)/mtp_custom/ \

LOCAL_C_INCLUDES += $(call include-path-for, system-core)/cutils

LOCAL_CFLAGS :=  -DMTP_HOST

LOCAL_CFLAGS += -DHAVE_PTHREADS

LOCAL_LDLIBS := -ldl -lGLESv1_CM -llog -lc \
     		   $(ANDROID_ROOT)/out/target/product/maguro/obj/lib/libutils.so \
    	       $(ANDROID_ROOT)/out/target/product/maguro/obj/lib/libcutils.so \
     		   $(ANDROID_ROOT)/out/target/product/maguro/obj/lib/libusbhost.so \
       		   $(ANDROID_ROOT)/out/target/product/maguro/obj/lib/libexif.so \
     		   $(ANDROID_ROOT)/out/target/product/maguro/obj/lib/libnativehelper.so \
       		   $(ANDROID_ROOT)/out/target/product/maguro/obj/lib/libandroid_runtime.so \
# $(LOCAL_PATH)/../obj/local/armeabi/libmtpcustom.so \

include $(BUILD_SHARED_LIBRARY)
