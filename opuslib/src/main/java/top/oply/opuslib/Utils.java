package top.oply.opuslib;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;


/**
 * Created by young on 2015/7/5.
 */
public class Utils {
    public static void printE(String tag, Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Log.e(tag, sw.toString());
    }


    public static String getFileName(String path) {
        String rst = null;
        try {
            File f = new File(path);
            rst = f.getName();
        } catch (Exception e) {
            printE("OpusTool",e);
        }
        return rst;
    }

    public static boolean isWAVFile(String fileName) {

        byte header[] = new byte[16];
        try {
            File f = new File(fileName);
            if(!f.exists()){
                Log.d("OpusTool",fileName + ":" + "File does not exist.");
                return false;
            }
            long actualLength = f.length();
            FileInputStream io = new FileInputStream(f);
            io.read(header, 0, 16);
            io.close();

            String tag = new String(header,0,4) + new String(header, 8,8);
            if (!tag.equals("RIFFWAVEfmt ")) {
                Log.d("OpusTool",fileName + ":" + "It's not a WAV file!");
                return false;
            }

            long paraLength = (header[4] & 0x000000ff) | ((header[5] << 8) & 0x0000ff00) |
                    ((header[6] << 16) & 0x00ff0000) | ((header[7] << 24) & 0xff000000);
            if (paraLength != actualLength - 8) {
                Log.d("OpusTool",fileName + ":" + "It might be a WAV file, but it's corrupted!");
                return false;
            }
            return  true;

        } catch (Exception e) {
            Log.d("OpusTool",fileName + ":" + "File Error");
            return false;
        }
    }

    public static boolean isFileExist(String fileName) {
        return new File(fileName).exists();
    }

    static String getExtention(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }

    public static void saveObj(String fileName, Object obj) {

        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(obj);
            fout.close();
            oout.close();
        } catch (Exception e) {
            printE("OpusTool",e);
        }
    }
    public static Object readObj(String fileName) {
        Object obj = new Object();
        try {
            FileInputStream fin = new FileInputStream(fileName);
            ObjectInputStream oin = new ObjectInputStream(fin);
            obj = oin.readObject();
            fin.close();
            oin.close();
        } catch (Exception e) {
            printE("OpusTool",e);
        } finally {
            return obj;
        }
    }

    public static class AudioTime implements Serializable {
        private String mFormat = "%02d:%02d:%02d";
        private int mHour = 0;
        private int mMinute = 0;
        private int mSecond = 0;

        public AudioTime() {

        }

        public AudioTime(long seconds) {
            setTimeInSecond(seconds);
        }
        /**
         * get time in the format of "HH:MM:SS"
         * @return
         */
        public String getTime() {

            return String.format(mFormat, mHour, mMinute, mSecond);
        }
        public void setTimeInSecond(long seconds) {
            mSecond =(int)(seconds % 60);
            long m = seconds / 60;
            mMinute = (int)(m % 60);
            mHour = (int)(m / 60);

        }
        public void add(int seconds) {
            mSecond += seconds;
            if (mSecond >= 60) {
                mSecond %= 60;
                mMinute++;

                if (mMinute >= 60) {
                    mMinute %= 60;
                    mHour++;
                }
            }
        }

    }

}
