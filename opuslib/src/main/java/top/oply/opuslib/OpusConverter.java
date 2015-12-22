package top.oply.opuslib;

/**
 * Created by young on 2015/8/5.
 */
public class OpusConverter {

    private OpusConverter(){}
    private static volatile OpusConverter singleton ;
    public static OpusConverter getInstance(){
        if(singleton==null)
            synchronized(OpusConverter.class){
                if(singleton==null)
                    singleton = new OpusConverter();
            }
        return singleton;
    }
    private static String TAG = OpusConverter.class.getName();
    private static final int STATE_NONE = 0;
    private static final int STATE_CONVERTING = 1;
    private static final  boolean TYPE_ENC = true;
    private static final  boolean TYPE_DEC = false;

    private volatile int state = STATE_NONE;
    private boolean convertType;
    private String inputFile;
    private String outputFile;
    private String option;

    private OpusTool mTool = new OpusTool();
    private Thread mThread = new Thread();
    private OpusEvent mEventSender = null;

    public void setEventSender(OpusEvent es) {
        mEventSender = es;
    }
    class ConvertThread implements Runnable {
        public void run() {
            if(mEventSender != null)
                mEventSender.sendEvent(OpusEvent.CONVERT_STARTED);

            if(convertType == TYPE_ENC)
                mTool.encode(inputFile, outputFile, option);
            else if(convertType == TYPE_DEC)
                mTool.decode(inputFile, outputFile, option);
            state = STATE_NONE;

            OpusTrackInfo.getInstance().addOpusFile(outputFile);
            if(mEventSender != null)
                mEventSender.sendEvent(OpusEvent.CONVERT_FINISHED, outputFile);

        }
    }

    public void encode(String fileNameIn, String fileNameOut, String opt) {
        if(!Utils.isWAVFile(fileNameIn)) {
            if(mEventSender != null)
                mEventSender.sendEvent(OpusEvent.CONVERT_FAILED);
            return;
        }
        state = STATE_CONVERTING;
        convertType = TYPE_ENC;
        inputFile = fileNameIn;
        outputFile = fileNameOut;
        option  = opt;
        mThread = new Thread(new ConvertThread(), "Opus Enc Thrd");
        mThread.start();

    }

    public void decode(String fileNameIn, String fileNameOut, String opt) {
        if(!Utils.isFileExist(fileNameIn) || mTool.isOpusFile(fileNameIn) == 0) {
            if(mEventSender != null)
                mEventSender.sendEvent(OpusEvent.CONVERT_FAILED);
            return;
        }
        state = STATE_CONVERTING;
        convertType = TYPE_DEC;
        inputFile = fileNameIn;
        outputFile = fileNameOut;
        option  = opt;
        mThread = new Thread(new ConvertThread(), "Opus Dec Thrd");
        mThread.start();

    }

    public boolean isWorking() {
        return state != STATE_NONE;
    }
    public void release() {
        try{
            if(state == STATE_CONVERTING && mThread.isAlive())
                mThread.interrupt();
        }catch (Exception e) {
            Utils.printE(TAG, e);
        } finally {
            state = STATE_NONE;
            if(mEventSender != null)
                mEventSender.sendEvent(OpusEvent.CONVERT_FAILED);
        }

    }
}
