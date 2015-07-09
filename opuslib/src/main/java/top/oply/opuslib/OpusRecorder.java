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
    private ByteBuffer fileBuffer = ByteBuffer.allocateDirect(1920);

    class RecordThread implements Runnable {
        public void run() {
            writeAudioDataToFile();
        }
    }


    public void startRecording(final String file) {

        if (state == STATE_STARTED)
            return;

        int minBufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        bufferSize = minBufferSize;
        bufferSize = 960 * 8;
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
//                    Log.d(TAG, "1buffer position  and limit are: " + buffer.position() + "-" + buffer.limit());
//                    buffer.flip();
//                    Log.d(TAG, "2buffer position  and limit are: " + buffer.position() + "-" + buffer.limit());
                    writeAudioDataToOpus(buffer, len);
                    Log.d(TAG, "3buffer position  and limit are: " + buffer.position() + "-" + buffer.limit());

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

//    //--Wav header
//    private void copyWaveFile(String inFilename, String outFilename) {
//
//        FileInputStream in = null;
//
//        FileOutputStream out = null;
//
//        long totalAudioLen = 0;
//
//        long totalDataLen = totalAudioLen + 36;
//
//        long longSampleRate = RECORDER_SAMPLERATE;
//
//        int channels = 1;
//
//        long byteRate = 16 * RECORDER_SAMPLERATE * channels / 8;
//
//        byte[] data = new byte[bufferSize];
//
//        try {
//
//            in = new FileInputStream(inFilename);
//
//            out = new FileOutputStream(outFilename);
//
//            totalAudioLen = in.getChannel().size();
//
//            totalDataLen = totalAudioLen + 36;
//
//            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
//
//                    longSampleRate, channels, byteRate);
//            int size=0;
//            while ((size = in.read(data)) != -1) {
//                out.write(data,0,size);
//            }
//
//            in.close();
//
//            out.close();
//
//        } catch (FileNotFoundException e) {
//            Utils.printE(TAG, e);
//
//        } catch (IOException e) {
//            Utils.printE(TAG, e);
//
//        }
//
//    }
//
//    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
//
//                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
//
//            throws IOException {
//
//        byte[] header = new byte[44];
//
//        header[0] = 'R'; // RIFF/WAVE header
//
//        header[1] = 'I';
//
//        header[2] = 'F';
//
//        header[3] = 'F';
//
//        header[4] = (byte) (totalDataLen & 0xff);
//
//        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
//
//        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
//
//        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
//
//        header[8] = 'W';
//
//        header[9] = 'A';
//
//        header[10] = 'V';
//
//        header[11] = 'E';
//
//        header[12] = 'f'; // 'fmt ' chunk
//
//        header[13] = 'm';
//
//        header[14] = 't';
//
//        header[15] = ' ';
//
//        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
//
//        header[17] = 0;
//
//        header[18] = 0;
//
//        header[19] = 0;
//
//        header[20] = 1; // format = 1
//
//        header[21] = 0;
//
//        header[22] = (byte) channels;
//
//        header[23] = 0;
//
//        header[24] = (byte) (longSampleRate & 0xff);
//
//        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
//
//        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
//
//        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
//
//        header[28] = (byte) (byteRate & 0xff);
//
//        header[29] = (byte) ((byteRate >> 8) & 0xff);
//
//        header[30] = (byte) ((byteRate >> 16) & 0xff);
//
//        header[31] = (byte) ((byteRate >> 24) & 0xff);
//
//        header[32] = (byte) (2 * 16 / 8); // block align
//
//        header[33] = 0;
//
//        header[34] = 16; // bits per sample
//
//        header[35] = 0;
//
//        header[36] = 'd';
//
//        header[37] = 'a';
//
//        header[38] = 't';
//
//        header[39] = 'a';
//
//        header[40] = (byte) (totalAudioLen & 0xff);
//
//        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
//
//        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
//
//        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
//
//        out.write(header, 0, 44);
//
//    }
//    //--end of WAV header

}
