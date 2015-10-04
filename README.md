# README #

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

1.	Get the source code.[Git] (https://bitbucket.org/louisyoung/opus_android.git)  
2.	Open it in Android Studio, and modify the path of SDK and NDK in the file "local.properties"
3.	Compile and run.  

* Test Hints:

1. This demo use external storage to store audio file. Be sure your Android device has a SD card when testing this demo. You could also store them in internal storage by changing source code.
2. Recommend to use a real Android device instead of Android virtual device.
3. When testing the master branch, firstly you need to copy at least one wav file and an opus file to the folder "OpusPlayer" under the root of SD card. Secondly, lauch the demo.
 

### Open Projects ###

1. Opus (git://git.opus-codec.org/opus.git)

2. Opus-tools (git://git.xiph.org/opus-tools.git)

3. Opusfile (git://git.xiph.org/opusfile.git)

### Enjoy ###