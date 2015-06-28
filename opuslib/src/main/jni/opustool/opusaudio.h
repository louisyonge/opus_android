#ifndef __OPUSADDIO_H
#define __OPUSADDIO_H

#include "config.h"
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <stdint.h>

int encode(char *, char *,char *);

int decode(char *, char *, char *);

int startRecording(const char *pathStr);

void stopRecording(void);

long getTotalPcmDuration(void);

int getFinished(void);

int getSize(void);

long getPcmOffset(void);

void readOpusFile(uint8_t *buffer, int capacity);

int writeFrame(uint8_t *framePcmBytes, unsigned int frameByteCount);

int seekOpusFile(float position);

int openOpusFile(const char *path);

void closeOpusFile(void);

int isOpusFile(const char *path);


#define MAX_CMD_NUM 32
#define MAX_CMD_BUFFER 1024

int strToArgv(char *str, char *arg[]);

#endif
