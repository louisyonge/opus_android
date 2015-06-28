LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#include the .mk files
include celt_sources.mk
include silk_sources.mk
include opus_sources.mk

MY_MODULE_DIR       := opustool

LOCAL_MODULE        := $(MY_MODULE_DIR)

#float point sources
SILK_SOURCES += $(SILK_SOURCES_FLOAT)
OPUS_SOURCES += $(OPUS_SOURCES_FLOAT)
#ARM build
CELT_SOURCES += $(CELT_SOURCES_ARM)
SILK_SOURCES += $(SILK_SOURCES_ARM)
LOCAL_SRC_FILES     := \
$(CELT_SOURCES) $(SILK_SOURCES) $(OPUS_SOURCES)

LOCAL_LDLIBS        := -lm -llog

LOCAL_C_INCLUDES    := \
$(LOCAL_PATH)/include \
$(LOCAL_PATH)/silk \
$(LOCAL_PATH)/silk/float \
$(LOCAL_PATH)/celt
LOCAL_CFLAGS        := -Wall -W -Wstrict-prototypes -Wextra -Wcast-align -Wnested-externs -Wshadow
LOCAL_CFLAGS        := -DNULL=0 -D_FILE_OFFSET_BITS=64
LOCAL_CFLAGS        += -DOPUS_BUILD -DHAVE_CONFIG_H -DUSE_ALLOCA -DHAVE_LRINT -DHAVE_LRINTF -O2 -std=gnu99 -g 
#LOCAL_CPPFLAGS      := -DBSD=1
#LOCAL_CPPFLAGS      += -ffast-math -O3 -funroll-loops

include $(BUILD_SHARED_LIBRARY)