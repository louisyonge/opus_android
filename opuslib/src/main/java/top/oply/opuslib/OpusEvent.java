package top.oply.opuslib;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by young on 2015/8/5.
 */
public class OpusEvent {

    public OpusEvent(Context context) {
        this.mContext = context;
    }

    //below are values of EventType
    public static final int PLAYING_FINISHED = 1001;
    public static final int PLAYING_STARTED = 1002;
    public static final int PLAYING_FAILED = 1003;
    public static final int PLAYING_PAUSED = 1004;

    public static final int PLAY_PROGRESS_UPDATE = 1011;
    public static final int PLAY_GET_AUDIO_TRACK_INFO = 1012;

    public static final int RECORD_FINISHED = 2001;
    public static final int RECORD_STARTED = 2002;
    public static final int RECORD_FAILED = 2003;
    public static final int RECORD_PROGRESS_UPDATE = 2004;

    public static final int CONVERT_FINISHED = 3001;
    public static final int CONVERT_STARTED = 3002;
    public static final int CONVERT_FAILED = 3003;

    //below are types of EventType
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String EVENT_PLAY_PROGRESS_POSITION = "PLAY_PROGRESS_POSITION";
    public static final String EVENT_PLAY_DURATION = "PLAY_DURATION";
    public static final String EVENT_PLAY_TRACK_INFO = "PLAY_TRACK_INFO";
    public static final String EVENT_RECORD_PROGRESS = "RECORD_PROGRESS";
    public static final String EVENT_MSG = "EVENT_MSG";



    /**
     * Default UI broadcast
     */
    public static final String ACTION_OPUS_UI_RECEIVER = "top.oply.oplayer.action.ui_receiver";

    /**
     * Handler for sending status updates
     */
    private Context mContext = null;
    private String mAction = ACTION_OPUS_UI_RECEIVER;

    public void setActionReceiver(String action) {
        mAction = action;
    }

    public void sendEvent(Bundle bundle) {

        Intent i = new Intent();
        i.setAction(mAction);
        i.putExtras(bundle);
        mContext.sendBroadcast(i);
    }

    /**
     * Send Event to UI
     * @param eventType
     */
    public void sendEvent(int eventType) {
        Bundle b = new Bundle();
        b.putInt(EVENT_TYPE,eventType);
        Intent i = new Intent();
        i.setAction(mAction);
        i.putExtras(b);
        mContext.sendBroadcast(i);
    }

    /**
     * Send Event to UI
     * @param eventType
     */
    public void sendEvent(int eventType, String msg) {
        Bundle b = new Bundle();
        b.putInt(EVENT_TYPE,eventType);
        b.putString(EVENT_MSG, msg);
        Intent i = new Intent();
        i.setAction(mAction);
        i.putExtras(b);
        mContext.sendBroadcast(i);
    }

    /**
     * Send playback progress to UI
     * @param currentPosition
     * @param totalDuration
     */
    public void sendProgressEvent(long currentPosition, long totalDuration) {
        Bundle b = new Bundle();
        b.putInt(EVENT_TYPE, PLAY_PROGRESS_UPDATE);
        b.putLong(EVENT_PLAY_PROGRESS_POSITION, currentPosition);
        b.putLong(EVENT_PLAY_DURATION,totalDuration);
        Intent i = new Intent();
        i.setAction(mAction);
        i.putExtras(b);
        mContext.sendBroadcast(i);
    }

    /**
     * Send record progress(time, units: second) to UI
     */
    public void sendRecordProgressEvent(String time) {
        Bundle b = new Bundle();
        b.putInt(EVENT_TYPE, RECORD_PROGRESS_UPDATE);
        b.putString(EVENT_RECORD_PROGRESS, time);
        Intent i = new Intent();
        i.setAction(mAction);
        i.putExtras(b);
        mContext.sendBroadcast(i);
    }

    /**
     * Send inforList to UI
     * @param inforList
     */
    public void sendTrackinforEvent(OpusTrackInfo.AudioPlayList inforList) {
        Bundle b = new Bundle();
        b.putInt(EVENT_TYPE, PLAY_GET_AUDIO_TRACK_INFO);
        //TODO; bug when sending the serializable inforList
        b.putSerializable(EVENT_PLAY_TRACK_INFO, inforList);
        Intent i = new Intent();
        i.setAction(mAction);
        i.putExtras(b);
        mContext.sendBroadcast(i);
    }

}
