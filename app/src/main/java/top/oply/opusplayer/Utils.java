package top.oply.opusplayer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by young on 2015/6/7.
 */
public class Utils {
    static String CurTime(){
        SimpleDateFormat sformat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());
        String str = sformat.format(curDate);
        return str;
    }
}
