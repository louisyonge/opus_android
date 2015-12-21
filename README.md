Opus for Android
================


Welcome to the Opus demo for Android.

### Summary ###

The is a demo on how to use Opus codec for Android project, including audio record, playback, encode and decode.  


### Getting started ###

* pre-requisites  

1.	JDK v1.8 or higher  
2.	SDK v2.2.1 or higher  
3.	NDK  r10d or higher (Note: remember to export NDK's path) 
4.	Android Studio (with SDK) 1.2.1 or higher  


* Summary of set up:

1.	Get the source code.[Git] (https://github.com/louisyonge/opus_android.git)  
2	remember to export NDK's path. Take Linux for example, add the following code to the end of the file "/etc/profile", and then reboot your system.
```
NDK_ROOT=/usr/local/lib/android-ndk-r9d
export PATH=$NDK_ROOT:$PATH
```

3.	Open it in Android Studio, and modify the path of SDK and NDK in the file "local.properties"
4.	Compile and run.  

* Test Hints:

1. This demo use external storage to store audio file. Be sure your Android device has a SD card when testing this demo. You could also store them in internal storage by changing source code.
2. Recommend to use a real Android device instead of Android virtual device. . Some times AVD has no sond system supports.
3. When testing the master branch, firstly you need to copy at least one wav file and an opus file to the folder "OpusPlayer" under the root of SD card. Secondly, lauch the demo. Then you can play or encode/decode these audio files.
 

### Open Projects ###

1. Opus (git://git.opus-codec.org/opus.git)

2. Opus-tools (git://git.xiph.org/opus-tools.git)

3. Opusfile (git://git.xiph.org/opusfile.git)

### Enjoy ###

# Welcome


Welcome to wiki!
----------------
## Development Environment

SDK, NDK, Android Studio, Eclipse

Note: Android Studio does not support the debug of Native code. So it is wise to develop the C&C++ code in Eclipse. For this project, codes under the folder "OpusPlayer\opuslib\src\main\jni" are Native codes. The rest of this project is Java codes, developed by Android Studio.

## Project structure

This project contains two module, one is opuslib, including both native Opus library and high level interfaces to call native codes of Opus; the other is a demo, showing how to use the opuslib. The opuslib module's output file "opuslib.arr" can be used directly by other Android project, which is exactly the purpose of this module. 
 
## How to debug Native code

* Android compiler  
1. cd "OpusPlayer\opuslib\src\main\jni".
2. Issure command "ndk-build".
5. Click build in Android Studio.
6. Watch logs in logcat.
* Linux  
As Android Studio does not support JNI debugging, using Eclipse in Linux is a good way to debug Native codes. There is a valid Makefile under the JNI folder of this project. So you can ether import the JNI code to a Eclipse project, or just cd to the folder and issue compile command "make". When compiling for Linux, Comment out "#define ANDROID_V" in the file "\OpusPlayer\opuslib\src\main\jni\include\config.h", and uncomment it when compiling by for Android. This Macro is a switch to redirect std-stream to logcat, and vice versa. "Opus_demo.c" is the console demo of testing opuslib for Linux. Cygwin is not recommended, because you might encounter some strange compilation problem. 


## How to use the OpusLib codes (Method 1)
OpusService is the highest level interface to programmer. It's a background Server running automatically. All you need to do is sending Intents to it, and receiving the feedback messages through a Broadcast Receiver. The approach is recommended over the Method 2.

###Sending message. 

Many static public method can be called directly. For details, please refer to the source code of OpusService.java
```
OpusService.play(Context context, String fileName);
OpusService.record(Context context, String fileName);
......
```

###Receiving message.

A Broadcast Receiver is needed to receive the feadback messages while playing, recording or converting a opus file. Below is an example.
```
//register a broadcast receiver
mReceiver = new OpusReceiver();
IntentFilter filter = new IntentFilter();
filter.addAction(OpusEvent.ACTION_OPUS_UI_RECEIVER);
registerReceiver(mReceiver, filter);

//define a broadcast receiver
class OpusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            int type = bundle.getInt(OpusEvent.EVENT_TYPE, 0);
            switch (type) {
                case OpusEvent.CONVERT_FINISHED:
                    break;
                case OpusEvent.CONVERT_FAILED:
                    break;
                case OpusEvent.CONVERT_STARTED:
                    break;
                case OpusEvent.RECORD_FAILED: 
                    break;
                case OpusEvent.RECORD_FINISHED: 
                    break;
                case OpusEvent.RECORD_STARTED: 
                    break;
                case OpusEvent.RECORD_PROGRESS_UPDATE:  
                    break;
                case OpusEvent.PLAY_PROGRESS_UPDATE:
					break
                case OpusEvent.PLAY_GET_AUDIO_TRACK_INFO:  
					break
                case OpusEvent.PLAYING_FAILED:
                    break;
                case OpusEvent.PLAYING_FINISHED:
                    break;
                case OpusEvent.PLAYING_PAUSED:            
                    break;
                case OpusEvent.PLAYING_STARTED:                
                    break;
                default:
                    Log.d(TAG, intent.toString() + "Invalid request,discarded");
                    break;
            }
        }
    }

```


## How to use the OpusLib codes (Method 2)
Encode and Decode
```
OpusTool oTool = new OpusTool();
oTool.decode(fileName,fileNameOut, null);
oTool.encode(fileName, fileNameOut, null);
```
Playback
```
OpusPlayer opusPlayer = OpusPlayer.getInstance();
opusPlayer.play(fileName);
opusPlayer.stop();
```
Record
```
OpusRecorder opusRecorder = OpusRecorder.getInstance();
opusRecorder.startRecording(fileName);
opusRecorder.stopRecording();
```

Licence
--------
Project uses [MIT License](LICENSE)


Have fun!