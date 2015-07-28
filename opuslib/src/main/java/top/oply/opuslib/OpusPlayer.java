package top.oply.opuslib;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by young on 2015/7/2.
 */
public class OpusPlayer {

    private OpusTool opusLib = new OpusTool();
    private static final String TAG = OpusPlayer.class.getName();
    private static final int STATE_NONE = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_PAUSED = 2;

    private volatile int state = STATE_NONE;
    private AudioTrack audioTrack;
    private int bufferSize;
    private long duration;
    private long offset;

    private String currentFileName;

    private volatile Thread playTread = null;

    class PlayThread implements Runnable {
        public void run() {
            readAudioDataFromFile();
        }
    }

    public void play(String fileName) {
        if (state != STATE_NONE) {
            stop();
        }
        state = STATE_NONE;
        currentFileName = fileName;

        int res = opusLib.openOpusFile(currentFileName);
        if (res == 0) {
            Log.e(TAG, "Open opus file error!");
            return;
        }

        duration = opusLib.getTotalPcmDuration();
        offset = 0;

        try {
            bufferSize = AudioTrack.getMinBufferSize(48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
            audioTrack.play();
        } catch (Exception e) {
            Utils.printE(TAG, e);
            destroyPlayer();
            return;
        }

        state = STATE_STARTED;
        playTread = new Thread( new PlayThread(),"OpusPlay Thrd");
        playTread.start();
    }

    protected void readAudioDataFromFile() {
        if (state != STATE_STARTED) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        boolean isFinished = false;
        while (state != STATE_NONE) {
            if (state == STATE_PAUSED){
                try {
                    Thread.sleep(200);
                    continue;
                }
                catch (Exception e) {
                    Log.e(TAG, e.toString());
                    continue;
                }

            }
            else  if (state == STATE_STARTED) {
                opusLib.readOpusFile(buffer, bufferSize);
                int size = opusLib.getSize();
                long pmcOffset = opusLib.getPcmOffset();

                if (size != 0) {
                    buffer.rewind();
                    byte[] data = new byte[size];
                    buffer.get(data);
                    audioTrack.write(data, 0, size);
                }
                offset = pmcOffset;
                float scale = 0;
                if (duration != 0) {
                    scale = offset / (float) duration;
                }
                isFinished = opusLib.getFinished() == 1;
                if (isFinished)
                    break;
            }

        }
        if (state != STATE_NONE)
            state = STATE_NONE;
    }

    public void pause() {
        if (state == STATE_STARTED) {
            audioTrack.pause();
            state = STATE_PAUSED;
        }
        float scale = 0;
        if (duration != 0) {
            scale = offset / (float) duration;
        }
    }

    public void resume() {
        if (state == STATE_PAUSED) {
            audioTrack.play();
            state = STATE_STARTED;
        }
    }

    public void stop() {
        state = STATE_NONE;
        try {
            Thread.sleep(200);
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        destroyPlayer();
    }

    /**
     *
     * @param fileName
     * @return What the next function should be.
     */
    public String toggle(String fileName) {
        if (state == STATE_PAUSED) {
            resume();
            return "Pause";
        } else if (state == STATE_STARTED) {
            pause();
            return "Resume";
        } else {
            play(fileName);
            return "Pause";
        }
    }


    private void destroyPlayer() {
        opusLib.closeOpusFile();
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }
}
