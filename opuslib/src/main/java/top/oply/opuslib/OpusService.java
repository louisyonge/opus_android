package top.oply.opuslib;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


public class OpusService extends Service {

    private String TAG = OpusService.class.getName();

    //Looper
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;

    //This server
    private static final String ACTION_OPUSSERVICE = "top.oply.opuslib.action.OPUSSERVICE";

    private static final String EXTRA_FILE_NAME = "FILE_NAME";
    private static final String EXTRA_FILE_NAME_OUT = "FILE_NAME_OUT";
    private static final String EXTRA_OPUS_CODING_OPTION = "OPUS_CODING_OPTION";
    private static final String EXTRA_CMD = "CMD";
    private static final String EXTRA_SEEKFILE_SCALE = "SEEKFILE_SCALE";

    private static final int CMD_PLAY           = 10001;
    private static final int CMD_PAUSE          = 10002;
    private static final int CMD_STOP_PLAYING   = 10003;
    private static final int CMD_TOGGLE         = 10004;
    private static final int CMD_SEEK_FILE      = 10005;
    private static final int CMD_GET_TRACK_INFO = 10006;
    private static final int CMD_ENCODE         = 20001;
    private static final int CMD_DECODE         = 20002;
    private static final int CMD_RECORD         = 30001;
    private static final int CMD_STOP_REOCRDING = 30002;
    private static final int CMD_RECORD_TOGGLE  = 30003;

    private OpusPlayer mPlayer;
    private OpusRecorder mRecorder;
    private OpusConverter mConverter;
    private OpusTrackInfo mTrackInfo;
    private OpusEvent mEvent = null;

    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public static void play(Context context, String fileName) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_PLAY);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void record(Context context, String fileName) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_RECORD);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void toggle(Context context, String fileName) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_TOGGLE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void seekFile(Context context, float scale) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_SEEK_FILE);
        intent.putExtra(EXTRA_SEEKFILE_SCALE, scale);
        context.startService(intent);
    }

    /**
     * Request the Track info of all the opus files in the directory of this app
     * @param context
     */
    public static void getTrackInfo(Context context) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_GET_TRACK_INFO);
        context.startService(intent);
    }

    public static void recordToggle(Context context, String fileName) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_RECORD_TOGGLE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        context.startService(intent);
    }

    public static void pause(Context context) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_PAUSE);
        context.startService(intent);
    }

    public static void stopRecording(Context context) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_STOP_REOCRDING);
        context.startService(intent);
    }

    public static void stopPlaying(Context context) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_STOP_PLAYING);
        context.startService(intent);
    }

    public static void encode(Context context, String fileName, String fileNameOut, String option) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_ENCODE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_FILE_NAME_OUT, fileNameOut);
        intent.putExtra(EXTRA_OPUS_CODING_OPTION, option);
        context.startService(intent);
    }

    public static void decode(Context context, String fileName, String fileNameOut, String option) {
        Intent intent = new Intent(context, OpusService.class);
        intent.setAction(ACTION_OPUSSERVICE);
        intent.putExtra(EXTRA_CMD, CMD_DECODE);
        intent.putExtra(EXTRA_FILE_NAME, fileName);
        intent.putExtra(EXTRA_FILE_NAME_OUT, fileNameOut);
        intent.putExtra(EXTRA_OPUS_CODING_OPTION, option);
        context.startService(intent);
    }

    public void onCreate() {
        super.onCreate();
        mEvent = new OpusEvent(getApplicationContext());
        mPlayer = OpusPlayer.getInstance();
        mRecorder = OpusRecorder.getInstance();
        mConverter = OpusConverter.getInstance();
        mTrackInfo = OpusTrackInfo.getInstance();

        mTrackInfo.setEvenSender(mEvent);
        mPlayer.setEventSender(mEvent);
        mRecorder.setEventSender(mEvent);
        mConverter.setEventSender(mEvent);

        //start looper in onCreate() instead of onStartCommand()
        HandlerThread thread = new HandlerThread("OpusServiceHander");
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

    }

    public void onDestroy() {
        //quit looper
        mServiceLooper.quit();

        mPlayer.release();
        mRecorder.release();
        mConverter.release();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    private void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_OPUSSERVICE.equals(action)) {
                int request = intent.getIntExtra(EXTRA_CMD, 0);
                String fileName;
                String fileNameOut;
                String option;
                switch (request) {
                    case CMD_PLAY:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        handleActionPlay(fileName);
                        break;
                    case CMD_PAUSE:
                        handleActionPause();
                        break;
                    case CMD_TOGGLE:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        handleActionToggle(fileName);
                        break;
                    case CMD_STOP_PLAYING:
                        handleActionStopPlaying();
                        break;
                    case CMD_RECORD:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        handleActionRecord(fileName);
                        break;
                    case CMD_STOP_REOCRDING:
                        handleActionStopRecording();
                        break;
                    case CMD_ENCODE:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        fileNameOut = intent.getStringExtra(EXTRA_FILE_NAME_OUT);
                        option = intent.getStringExtra(EXTRA_OPUS_CODING_OPTION);
                        handleActionEncode(fileName, fileNameOut, option);
                        break;
                    case CMD_DECODE:
                        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                        fileNameOut = intent.getStringExtra(EXTRA_FILE_NAME_OUT);
                        option = intent.getStringExtra(EXTRA_OPUS_CODING_OPTION);
                        handleActionDecode(fileName, fileNameOut, option);
                        break;
                    case CMD_RECORD_TOGGLE:
                        if(mRecorder.isWorking()) {
                            handleActionStopRecording();
                        } else {
                            fileName = intent.getStringExtra(EXTRA_FILE_NAME);
                            handleActionRecord(fileName);
                        }
                        break;
                    case CMD_SEEK_FILE:
                        float scale = intent.getFloatExtra(EXTRA_SEEKFILE_SCALE,0);
                        handleActionSeekFile(scale);
                        break;
                    case CMD_GET_TRACK_INFO:
                        mTrackInfo.sendTrackInforToUi();
                        break;
                    default:
                        Log.e(TAG,"Unknown intent CMD,discarded!");
                }

            } else {
                Log.e(TAG,"Unknown intent action,discarded!");
            }

        }
    }


    private void handleActionPlay(String fileName) {
        mPlayer.play(fileName);
    }
    private void handleActionStopPlaying() {
        mPlayer.stop();
    }
    private void handleActionPause() {
        mPlayer.pause();
    }
    private void handleActionToggle(String fileName) {
        mPlayer.toggle(fileName);
    }
    private void handleActionSeekFile(float scale) {
        mPlayer.seekOpusFile(scale);
    }
    private void handleActionRecord(String fileName) {
        mRecorder.startRecording(fileName);
    }
    private void handleActionStopRecording() {
        mRecorder.stopRecording();
    }
    private void handleActionEncode(String fileNameIn, String fileNameOut, String option) {
        mConverter.encode(fileNameIn, fileNameOut, option);
    }
    private void handleActionDecode(String fileNameIn, String fileNameOut, String option) {
        mConverter.decode(fileNameIn, fileNameOut, option);
    }


    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
            //stopSelf()
        }
    }
}
