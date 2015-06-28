#ifndef LOG_INCLUDED
#define LOG_INCLUDED

#include "config.h"

#ifdef ANDROID_V
#include <android/log.h>
#include<errno.h>

#define  LOG_TAG "libOpusTool"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

#ifdef perror
#undef perror
#endif
#define perror(smg) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,"opus error:%s :%s",smg, strerror(errno))

#ifdef fprintf
#undef fprintf
#endif
#define fprintf(strm,...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#else
#include <stdio.h>
#include <stdlib.h>
#define LOGE(fmt,arg...) fprintf(stderr,fmt , ##arg)
#define LOGD(fmt,arg...) fprintf(stderr,fmt , ##arg)
#endif

#endif