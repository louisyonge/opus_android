package top.oply.opuslib;

import android.os.Environment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

;

/**
 * Created by young on 2015/8/7.
 */
public class OpusTrackInfo {

    private static volatile OpusTrackInfo oTrackInfo ;
    public static OpusTrackInfo getInstance(){
        if(oTrackInfo == null)
            synchronized(OpusTrackInfo.class){
                if(oTrackInfo == null)
                    oTrackInfo = new OpusTrackInfo();
            }
        return oTrackInfo;
    }

    private String TAG = OpusTrackInfo.class.getName();
    private OpusEvent mEventSender;
    private OpusTool mTool = new OpusTool();
    private String appExtDir;
    private File requestDirFile;
    private Thread mThread = new Thread();
    private AudioPlayList mTrackInforList = new AudioPlayList();
    private Utils.AudioTime mAudioTime = new Utils.AudioTime();

    public static final String TITLE_TITLE = "TITLE";
    public static final String TITLE_ABS_PATH = "ABS_PATH";
    public static final String TITLE_DURATION = "DURATION";
    public static final String TITLE_IMG = "TITLE_IMG";
    public static final String TITLE_IS_CHECKED = "TITLE_IS_CHECKED";

    public void setEvenSender(OpusEvent opusEven) {
        mEventSender = opusEven;
    }
    private OpusTrackInfo() {

        //create OPlayer directory if it does not exist.
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return;
        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        appExtDir = sdcardPath + "/OPlayer/";
        File fp = new File(appExtDir);
        if(!fp.exists())
            fp.mkdir();

        getTrackInfor(appExtDir);
    }

    public void addOpusFile(String file) {
        try {
            Thread.sleep(10);
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }

        File f = new File(file);
        if(f.exists() && "opus".equalsIgnoreCase(Utils.getExtention(file))
                && mTool.openOpusFile(file) != 0) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(TITLE_TITLE, f.getName());
            map.put(TITLE_ABS_PATH, file);
            mAudioTime.setTimeInSecond(mTool.getTotalDuration());
            map.put(TITLE_DURATION, mAudioTime.getTime());
            map.put(TITLE_IS_CHECKED,false);
            //TODO: get imagin from opus files
            map.put(TITLE_IMG, 0);
            mTrackInforList.add(map);
            mTool.closeOpusFile();

            if(mEventSender != null)
                mEventSender.sendTrackinforEvent(mTrackInforList);
        }
    }

    public String getAppExtDir() {
        return  appExtDir;
    }

    public void sendTrackInforToUi() {
        if(mEventSender != null)
            mEventSender.sendTrackinforEvent(mTrackInforList);
    }
    public AudioPlayList getTrackInfor() {
        return mTrackInforList;
    }

    private void getTrackInfor(String Dir) {
        if(Dir.length() == 0)
            Dir = appExtDir;
        File file = new File(Dir);
        if (file.exists() && file.isDirectory())
            requestDirFile = file;

        mThread = new Thread(new MyThread(), "Opus Trc Trd");
        mThread.start();
    }

    public String getAValidFileName(String prefix) {
        String name = prefix;
        String extention = ".opus";
        HashSet<String> set = new HashSet<String>(100);
        List<Map<String, Object>> lst =  getTrackInfor().getList();
        for (Map<String, Object>map : lst) {
            set.add(map.get(OpusTrackInfo.TITLE_TITLE).toString());
        }
        int i = 0;
        while (true) {
            i++;
            if(!set.contains(name + i + extention))
                break;
        }

        return appExtDir + name + i + extention;
    }

    private void prepareTrackInfor(File file) {
        try {
            File[] files = file.listFiles();
            for(File f : files) {
                if (f.isFile()) {
                    String name = f.getName();
                    String absPath = f.getAbsolutePath();
                    if ("opus".equalsIgnoreCase(Utils.getExtention(name))
                            && mTool.openOpusFile(absPath) != 0) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put(TITLE_TITLE, f.getName());
                        map.put(TITLE_ABS_PATH,absPath);
                        mAudioTime.setTimeInSecond(mTool.getTotalDuration());
                        map.put(TITLE_DURATION, mAudioTime.getTime());
                        //TODO: get imagin from opus files
                        map.put(TITLE_IS_CHECKED,false);
                        map.put(TITLE_IMG, 0);
                        mTrackInforList.add(map);
                        mTool.closeOpusFile();
                    }

                } else if (f.isDirectory()){
                    prepareTrackInfor(f);
                }
            }
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }
    }

    public static class AudioPlayList implements Serializable {
        public AudioPlayList() {

        }
        public static final long serialVersionUID=1234567890987654321L;
        private List<Map<String, Object>> mAudioInforList = new ArrayList<Map<String, Object>>(32);

        public void add(Map<String, Object> map) {
            mAudioInforList.add(map);
        }
        public List<Map<String, Object>> getList() {
            return mAudioInforList;
        }
        public boolean isEmpty() {
            return mAudioInforList.isEmpty();
        }
        public int size() {
            return mAudioInforList.size();
        }
        public void clear() {
            mAudioInforList.clear();
        }
    }

    class MyThread implements Runnable {
        public void run() {
            prepareTrackInfor(requestDirFile);
            sendTrackInforToUi();
        }
    }

    public void release() {
        try{
            if(mThread.isAlive())
                mThread.interrupt();
        }catch (Exception e) {
            Utils.printE(TAG, e);
        }
    }
}
