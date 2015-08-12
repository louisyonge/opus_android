package top.oply.opuslib;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by young on 2015/7/2.
 */
public class OpusRecorder {
    private OpusRecorder(){}
    private static volatile OpusRecorder singleton ;
    public static OpusRecorder getInstance(){
        if(singleton==null)
            synchronized(OpusRecorder.class){
                if(singleton==null)
                    singleton = new OpusRecorder();
            }
        return singleton;
    }

    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;

    private static final String TAG = OpusRecorder.class.getName();
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private volatile int state = STATE_NONE;

    private AudioRecord recorder = null;
    private Thread recordingThread = new Thread();
    private OpusTool opusTool = new OpusTool();
    private int bufferSize = 0;
    private String filePath = null;
    private ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);// Should be 1920, to accord with function writeFreme()
    private OpusEvent mEventSender = null;
    private Timer mProgressTimer = null;
    private Utils.AudioTime mRecordTime = new Utils.AudioTime();

    public void setEventSender(OpusEvent es) {
        mEventSender = es;
    }

    class RecordThread implements Runnable {
        public void run() {
            writeAudioDataToFile();
        }
    }
    private String initRecordFileName() {
        String name;
        String extention = ".opus";
        OpusTrackInfo info =  OpusTrackInfo.getInstance();
        HashSet<String> set = new HashSet<String>(100);
        List<Map<String, Object>> lst =  info.getTrackInfor().getList();
        for (Map<String, Object>map : lst) {
            set.add(map.get(OpusTrackInfo.TITLE_TITLE).toString());
        }
        name = "OpusRecord";
        int i = 0;
        while (true) {
            i++;
            if(!set.contains(name + i + extention))
                break;
        }

        return info.getAppExtDir() + name + i + extention;
    }

    public void startRecording(final String file) {

        if (state == STATE_STARTED)
            return;

        int minBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        bufferSize = (minBufferSize / 1920 + 1) * 1920;

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferSize);
        recorder.startRecording();
        state = STATE_STARTED;

        filePath = file.isEmpty() ? initRecordFileName() : file;
        int rst = opusTool.startRecording(filePath);
        if (rst != 1) {
            if(mEventSender != null)
                mEventSender.sendEvent(OpusEvent.RECORD_FAILED);
            Log.e(TAG,"recorder initially error");
            return;
        }

        if(mEventSender != null)
            mEventSender.sendEvent(OpusEvent.RECORD_STARTED);

        mProgressTimer = new Timer();
        mRecordTime.setTimeInSecond(0);
        mProgressTimer.schedule(new MyTimerTask(),1000,1000);

        recordingThread = new Thread(new RecordThread(), "OpusRecord Thrd");
        recordingThread.start();
    }


    private void writeAudioDataToOpus(ByteBuffer buffer, int size) {
        ByteBuffer finalBuffer = ByteBuffer.allocateDirect(size);
        finalBuffer.put(buffer);
        finalBuffer.rewind();
        boolean flush = false;

        //write data to Opus file
        while (state == STATE_STARTED && finalBuffer.hasRemaining()) {
            int oldLimit = -1;
            if (finalBuffer.remaining() > fileBuffer.remaining()) {
                oldLimit = finalBuffer.limit();
                finalBuffer.limit(fileBuffer.remaining() + finalBuffer.position());
            }
            fileBuffer.put(finalBuffer);
            if (fileBuffer.position() == fileBuffer.limit() || flush) {
                int length = !flush ? fileBuffer.limit() : finalBuffer.position();

                int rst = opusTool.writeFrame(fileBuffer, length);
                if (rst != 0) {
                    fileBuffer.rewind();
                }
            }
            if (oldLimit != -1) {
                finalBuffer.limit(oldLimit);
            }
        }
    }
    private void writeAudioDataToFile() {
        if (state != STATE_STARTED)
            return;

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);

        while (state == STATE_STARTED) {
            buffer.rewind();
            int len = recorder.read(buffer, bufferSize);
            Log.d(TAG, "\n lengh of buffersize is " + len);
            if (len != AudioRecord.ERROR_INVALID_OPERATION) {
                try {
                    writeAudioDataToOpus(buffer, len);
                }
                catch (Exception e)
                {
                    if(mEventSender != null)
                        mEventSender.sendEvent(OpusEvent.RECORD_FAILED);
                    Utils.printE(TAG, e);
                }
            }

        }
        if(mEventSender != null)
            mEventSender.sendEvent(OpusEvent.RECORD_FINISHED);
        OpusTrackInfo info =  OpusTrackInfo.getInstance();
        info.addOpusFile(filePath);
    }

    public void stopRecording() {
        if (state != STATE_STARTED)
            return;

        state = STATE_NONE;
        try {
            Thread.sleep(200);
        }
        catch (Exception e) {
            Utils.printE(TAG,e);
        }

        if (null != recorder) {
            opusTool.stopRecording();
            recordingThread = null;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }
    public boolean isWorking() {
        return state != STATE_NONE;
    }
    public void release() {
        if(state != STATE_NONE) {
            stopRecording();
        }
    }

    private class MyTimerTask extends TimerTask {
        public void run () {
            if(state != STATE_STARTED) {
                mProgressTimer.cancel();
            } else {
                mRecordTime.add(1);
                String progress = mRecordTime.getTime();
                if(mEventSender != null)
                    mEventSender.sendRecordProgressEvent(progress);
            }
        }
    }

}
