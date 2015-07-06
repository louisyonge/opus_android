package top.oply.opusplayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import top.oply.opuslib.OpusPlayer;
import top.oply.opuslib.OpusRecorder;
import top.oply.opuslib.OpusTool;


public class oplayer extends Activity {

    OpusPlayer opusPlayer = null;
    OpusRecorder opusRecorder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oplayer);
     }

    private void print(String str) {
        TextView tv;
        tv = (TextView)findViewById(R.id.mainLog);
        tv.setText( Utils.CurTime() + ": " + str + "\n" + tv.getText());
    }

    public void btnDecClick(View view){

        String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
        String fileName = SDPATH + "test.opus";
        File f = new File(fileName);
        if (!f.exists()){

            print(fileName + " is not exist, please put it there");
        }
        String fileNameOut = fileName + ".wav";

        print("Start decoding...");
        OpusTool oTool = new OpusTool();
        Log.d("encode:", oTool.nativeGetString());
        int result = oTool.decode(fileName,fileNameOut, null);
        if (result == 0){
            String str = "Decode is complete. Output file is: " + fileNameOut;
            print(str);
        } else{
            String str = "Decode failed.";
            print(str);
        }
    }

    public void btnEncClick(View view){

        String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
        String fileName = SDPATH + "test.wav";
        File f = new File(fileName);
        if (!f.exists()){
            String str = fileName + " is not exist, please put it there.";
            print(str);
        }
        String fileNameOut = fileName + ".opus";

        print("Start encoding...");
        OpusTool oTool = new OpusTool();
        Log.d("encode:", oTool.nativeGetString());
        int result = oTool.encode(fileName, fileNameOut, null);
        if (result == 0){
            print("Encode is complete. Output file is: " + fileNameOut);
        } else{
            print("Encode failed");
        }

    }

    public void btnPlayClick(View v) {
        if(opusPlayer == null)
            opusPlayer = new OpusPlayer();
        String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
        String fileName = SDPATH + "test.opus";
        opusPlayer.play(fileName);
        print("start palying" + fileName);
    }

    public void btnPausePClick(View v) {
        if(opusPlayer == null)
            return;
        String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
        String fileName = SDPATH + "test.opus";
        String str = opusPlayer.toggle(fileName);
        ((Button)v).setText(str);
        print("You might want to" + str);
    }
    public void btnStopPClick(View v) {
        if(opusPlayer == null)
            return;
        opusPlayer.stop();
        print("Stop Playing");
    }
    public void btnStopRClick(View v) {
        if(opusRecorder == null)
            return;
        opusRecorder.stopRecording();
        print("Stop Recording");
    }
    public void btnRecordClick(View v) {
        if(opusRecorder == null)
            opusRecorder = new OpusRecorder();
        String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
        String fileName = SDPATH + "test.opus";
        opusRecorder.startRecording(fileName);
        print("Start Recording.. Save file to: " + fileName);
    }

}
