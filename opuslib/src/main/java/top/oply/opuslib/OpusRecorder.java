package top.oply.opuslib;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by young on 2015/7/2.
 */
public class OpusRecorder {

    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_COMPLETED = 2;
    private static final String TAG = OpusRecorder.class.getName();
    private static final int RECORDER_SAMPLERATE = 16000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private volatile int state = STATE_NONE;

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private OpusTool opusTool = new OpusTool();
    private int bufferSize = 0;
    private String filePath = null;
    private ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);// Should be 1920, to accord with function writeFreme()

    class RecordThread implements Runnable {
        public void run() {
            writeAudioDataToFile();
        }
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
        filePath = file;
        int rst = opusTool.startRecording(filePath);
        if (rst != 1) {
            Log.e(TAG,"recorder initially error");
            return;
        }
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
                    Utils.printE(TAG, e);
                }
            }

        }

    }

    public void stopRecording() {
        if (state != STATE_STARTED)
            return;

        state = STATE_COMPLETED;
        try {
            Thread.sleep(200);
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        if (null != recorder) {
            state = STATE_COMPLETED;
            opusTool.stopRecording();
            recordingThread = null;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

}
