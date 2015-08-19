package top.oply.opusplayer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import top.oply.opuslib.OpusPlayer;
import top.oply.opuslib.OpusRecorder;
import top.oply.opuslib.OpusTool;


public class oplayer extends Activity {

    private OpusPlayer opusPlayer = null;
    private OpusRecorder opusRecorder = null;
    private OpusTool oTool = new OpusTool();

    private ListView lvFiles;
    private List<String> lstFiles = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oplayer);

        //initial listView
        lvFiles = (ListView)findViewById(R.id.lvFile);
        lvFiles.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        initData();
        adapter = new ArrayAdapter<String>( this , android.R.layout.simple_list_item_single_choice,lstFiles);
        lvFiles.setAdapter(adapter);
        lvFiles.setItemChecked(lstFiles.size()-1, true);
     }
    private List<String> initData(){
        lstFiles = new ArrayList<String>();
        String SDPATH = Environment.getExternalStorageDirectory().getPath();
        path = SDPATH + "/OpusPlayer/";
        File fp = new File(path);
        if(!fp.exists())
            fp.mkdir();

        File[] files = fp.listFiles();
        for (File f : files) {
            lstFiles.add(f.getName());
        }
        return lstFiles;
    }

    private void updateList(String str){
        if (lstFiles.contains(str))
            return;
        else {
            lstFiles.add(str);
            adapter.notifyDataSetChanged();
            lvFiles.setItemChecked(lstFiles.size()-1, true);
        }

    }

    private void print(String str) {
        TextView tv;
        tv = (TextView)findViewById(R.id.mainLog);
        tv.setText( Utils.CurTime() + ": " + str + "\n" + tv.getText());
    }

    public void btnDecClick(View view){

        String selectName =      adapter.getItem(lvFiles.getCheckedItemPosition());
        String fileName = path + selectName;

        File f = new File(fileName);
        if (!f.exists()){

            print(fileName + " is not exist, please put it there");
        }
        String fileNameOut = fileName + ".wav";
        print("Start decoding...");
        Log.d("encode:", oTool.nativeGetString());
        int result = oTool.decode(fileName, fileNameOut, null);
        if (result == 0){
            String str = "Decode is complete. Output file is: " + fileNameOut;
            updateList(selectName + ".wav");
            print(str);
        } else{
            String str = "Decode failed.";
            print(str);
        }
    }

    public void btnEncClick(View view){
        String selectName = adapter.getItem(lvFiles.getCheckedItemPosition());
        String fileName = path + selectName;
        File f = new File(fileName);
        if (!f.exists()){
            String str = fileName + " is not exist, please put it there.";
            print(str);
        }
        String fileNameOut = fileName + ".opus";
        print("Start encoding...");
        Log.d("encode:", oTool.nativeGetString());
        int result = oTool.encode(fileName, fileNameOut, null);
        if (result == 0){
            print("Encode is complete. Output file is: " + fileNameOut);
            updateList(selectName + ".opus");
        } else{
            print("Encode failed");
        }

    }

    public void btnPlayClick(View v) {
        if(opusPlayer == null)
            opusPlayer = OpusPlayer.getInstance();

        String fileName = path + adapter.getItem(lvFiles.getCheckedItemPosition());
        if(Utils.getExtention(fileName).equals("opus")) {
            opusPlayer.play(fileName);
            print("start palying..." + fileName);
        }
        else {
            print("This demo only support opus file's playback.");
        }

    }

    public void btnPausePClick(View v) {
        if(opusPlayer == null)
            return;
        String fileName = path + adapter.getItem(lvFiles.getCheckedItemPosition());

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
            opusRecorder = OpusRecorder.getInstance();

        String base = "record";
        String name = "record";
        int i = 0;
        for(i = 1; i < 100; i++){
            name = base + i + ".opus";
            if(!lstFiles.contains(name))
                break;
        }
        String fileName = path + name;
        opusRecorder.startRecording(fileName);
        print("Start Recording.. Save file to: " + fileName);

        updateList(name);
    }

}
