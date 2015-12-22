package top.oply.opusplayer;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by young on 2015/6/7.
 */
public class Utils {
    static String CurTime(){
//        SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        SimpleDateFormat sformat = new SimpleDateFormat("HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = sformat.format(curDate);
        return str;
    }

    static String getExtention(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return extension;
    }

    static void printE(String tag, Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Log.e(tag, sw.toString());
    }

}
