package top.oply.opuslib;

import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by young on 2015/7/5.
 */
public class Utils {
    static void printE(String tag, Exception e){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Log.e(tag, sw.toString());
    }
}
