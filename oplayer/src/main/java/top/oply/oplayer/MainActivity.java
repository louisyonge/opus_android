package top.oply.oplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import top.oply.opuslib.OpusEvent;
import top.oply.opuslib.OpusService;
import top.oply.opuslib.OpusTrackInfo;
import top.oply.opuslib.Utils;


public class MainActivity extends FragmentActivity {
    private String TAG = MainActivity.class.getName();

    private OpusReceiver mReceiver = null;
    private ImageButton mBtnPlay = null;
    private ImageButton mBtnStop = null;
    private ImageButton mBtnRecord = null;
    private TextView mTvRecordTime = null;
    private ListView mLvSongs = null;
    private SeekBar mPlaySeekBar = null;
    private TextView mTvPosition = null;
    private TextView mTvDuration = null;

    private FrgPlay frgPlay = null;
    private FrgRecord frgRecord = null;
    private FrgConvert frgConvert = null;


    private SimpleAdapter mAdapter;
    private List<Map<String, Object>> mSonglist = new ArrayList<Map<String, Object>>(20);
    //selected song's positon in mSonglist
    private volatile int listPosition = -1;
    private int listVisiblePosition = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        frgPlay = FrgPlay.newInstance(getString(R.string.frg_play), "");
        frgRecord = FrgRecord.newInstance(getString(R.string.frg_record), "");
        frgConvert = FrgConvert.newInstance(getString(R.string.frg_convert), "");

        initUI();
        initBroadcast();

        //TODO something wrong
        //Get Track list
        //    OpusService.getTrackInfo(getApplicationContext(), "");
    }

    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initBroadcast() {
        //register a broadcast
        mReceiver = new OpusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(OpusEvent.ACTION_OPUS_UI_RECEIVER);
        registerReceiver(mReceiver, filter);
    }

    public void initRecordUI(View v) {
        mBtnRecord = (ImageButton) v.findViewById(R.id.btnRecord);
        mTvRecordTime = (TextView) v.findViewById(R.id.tvRecordTime);
    }

    public void initConverUI(View v) {

    }

    public void initPlayUI(View v) {


        mBtnPlay = (ImageButton) v.findViewById(R.id.btnPlay);
        mLvSongs = (ListView) v.findViewById(R.id.lvSongs);
        mTvPosition = (TextView) v.findViewById(R.id.tvPosition);
        mTvDuration = (TextView) v.findViewById(R.id.tvDuration);

        //init seekBar
        mPlaySeekBar = (SeekBar) v.findViewById(R.id.seekBar);
        mPlaySeekBar.setOnSeekBarChangeListener(new MySeekBarChangeListener());
        mPlaySeekBar.setMax(100);

//        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//        String listFilePath = sdcardPath + "/OPlayer/" + "playlist.lst";
//        if (Utils.isFileExist(listFilePath))
//            mSonglist = ((OpusTrackInfo.AudioPlayList)Utils.readObj(listFilePath)).getList();
        mAdapter = new SimpleAdapter(getApplicationContext(), mSonglist, R.layout.playlist_view,
                new String[]{OpusTrackInfo.TITLE_TITLE, OpusTrackInfo.TITLE_DURATION,
                        OpusTrackInfo.TITLE_IMG, OpusTrackInfo.TITLE_ABS_PATH},
                new int[]{R.id.title, R.id.duration, R.id.img, R.id.absPath});
        mLvSongs.setAdapter(mAdapter);
        mLvSongs.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mLvSongs.setOnItemClickListener(new MySongListClickListener());
        mLvSongs.setOnScrollListener(new MySongListScronllListener());
        if(listVisiblePosition != -1)
            mLvSongs.setSelection(listVisiblePosition);
        if(listPosition != -1)
            mLvSongs.getChildAt(listPosition).setSelected(true);
        OpusService.getTrackInfo(this, "");
    }

    private void initUI() {
        //Set the pager with an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        FrgPlay frgPlay = FrgPlay.newInstance("Play", "?");
        FrgRecord frgRecord = FrgRecord.newInstance("Record", "?");
        FrgConvert frgConvert = FrgConvert.newInstance("Convert", "?");
        fragments.add(frgRecord);
        fragments.add(frgPlay);
        fragments.add(frgConvert);

        PageViewAdaptor pageAdaptor = new PageViewAdaptor(getSupportFragmentManager(), fragments);
        pager.setAdapter(pageAdaptor);

        pager.setCurrentItem(1);
        //Bind the title indicator to the adapter
        TabPageIndicator tabIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        tabIndicator.setViewPager(pager);
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//
//        menu.add(Menu.NONE, Menu.FIRST + 1, 1, getResources().getString(R.string.action_about));
//        menu.add(Menu.NONE, Menu.FIRST + 2, 2, getResources().getString(R.string.action_licenses));
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Toast.makeText(this, "settings is clicked", Toast.LENGTH_LONG).show();
//                break;
//            case Menu.FIRST + 1:
//                break;
//            case Menu.FIRST + 2:
//            default:
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    public boolean isSongListEmpty() {

        if (mSonglist.size() == 0) {
            Toast.makeText(this, getString(R.string.err_playlist_empty), Toast.LENGTH_SHORT).show();
            return true;
        }
        if (listPosition == -1) {
            listPosition++;
            mLvSongs.getChildAt(listPosition).setSelected(true);
        }
        return false;
    }
    public void onBtnPlayClick(View view) {
        if(isSongListEmpty())
            return;
        String filaName = mSonglist.get(listPosition).get(OpusTrackInfo.TITLE_ABS_PATH).toString();
        OpusService.toggle(getApplicationContext(), filaName);
    }

    public void onBtnPrevClick(View view) {
        if(isSongListEmpty())
            return;
        if (listPosition > 0) {
            listPosition--;
            mLvSongs.getChildAt(listPosition + 1).setSelected(false);
            mLvSongs.getChildAt(listPosition).setSelected(true);
            String filaName = mSonglist.get(listPosition).get(OpusTrackInfo.TITLE_ABS_PATH).toString();
            OpusService.play(getApplicationContext(), filaName);

        }
    }

    public void onBtnNextClick(View view) {
        if(isSongListEmpty())
            return;
        if (listPosition < mSonglist.size() -2) {
            listPosition++;
            mLvSongs.setSelection(listPosition -5);
            mLvSongs.getChildAt(listPosition - 1).setSelected(false);
            mLvSongs.getChildAt(listPosition).setSelected(true);
            String filaName = mSonglist.get(listPosition).get(OpusTrackInfo.TITLE_ABS_PATH).toString();
//            OpusService.play(getApplicationContext(), filaName);

            mLvSongs.getChildAt(listPosition - 1).setActivated(false);
            mLvSongs.getChildAt(listPosition).setActivated(true);



        }
    }

    public void onBtnStopClick(View view) {
        OpusService.stopPlaying(getApplicationContext());
    }

    public void onBtnRecordClick(View v) {
        String filaName = "";
        OpusService.recordToggle(getApplicationContext(), filaName);
    }


        class MySongListClickListener implements AdapterView.OnItemClickListener {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPosition = position;
//                Toast.makeText(getApplicationContext(), "position is:" + position + " id: " + id, Toast.LENGTH_SHORT).show();
                view.setSelected(true);view.setActivated(true);
                String filaName = mSonglist.get(listPosition).get(OpusTrackInfo.TITLE_ABS_PATH).toString();
                OpusService.play(getApplicationContext(), filaName);
            }
        }

        class MySongListScronllListener implements AbsListView.OnScrollListener {
            public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
            }
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    listVisiblePosition = mLvSongs.getFirstVisiblePosition();  //ListPos记录当前可见的List顶端的一行的位置
                }
            }
        }

    class MySeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float scale = ((float) progress) / mPlaySeekBar.getMax();
                    OpusService.seekFile(getApplicationContext(), scale);
                }
            }
        }

        class OpusReceiver extends BroadcastReceiver {

            public void onReceive(Context context, Intent intent) {

                Bundle bundle = intent.getExtras();
                int type = bundle.getInt(OpusEvent.EVENT_TYPE, 0);
                switch (type) {
                    case OpusEvent.CONVERT_FINISHED:

                        break;
                    case OpusEvent.CONVERT_FAILED:

                        break;
                    case OpusEvent.CONVERT_STARTED:

                        break;
                    case OpusEvent.RECORD_FAILED:
                        mBtnRecord.setImageResource(R.drawable.btn_record);
                        break;
                    case OpusEvent.RECORD_FINISHED:
                        mBtnRecord.setImageResource(R.drawable.btn_record);
                        break;
                    case OpusEvent.RECORD_STARTED:
                        mBtnRecord.setImageResource(R.drawable.btn_stop_recording);
                        break;
                    case OpusEvent.RECORD_PROGRESS_UPDATE:
                        String time = bundle.getString(OpusEvent.EVENT_RECORD_PROGRESS);
                        mTvRecordTime.setText(time);
                        break;
                    case OpusEvent.PLAY_PROGRESS_UPDATE:
                        long position = bundle.getLong(OpusEvent.EVENT_PLAY_PROGRESS_POSITION);
                        long duration = bundle.getLong(OpusEvent.EVENT_PLAY_DURATION);
                        Utils.AudioTime t = new Utils.AudioTime();
                        t.setTimeInSecond(position);
//                        mTvPosition.setText(t.getTime());
//                        t.setTimeInSecond(duration);
//                        mTvDuration.setText(t.getTime());
//                        int progress = (int) (100 * position / duration);
//                        mPlaySeekBar.setProgress(progress);
                        break;
                    case OpusEvent.PLAY_GET_AUDIO_TRACK_INFO:
                        List<Map<String, Object>> songlst = ((OpusTrackInfo.AudioPlayList) (bundle
                                .getSerializable(OpusEvent.EVENT_PLAY_TRACK_INFO))).getList();
                        mSonglist.clear();
                        for (Map<String, Object> map : songlst) {
                            if (map.get(OpusTrackInfo.TITLE_IMG).equals(0)) {
                                map.put(OpusTrackInfo.TITLE_IMG, R.drawable.default_music_icon);
                                mSonglist.add(map);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        break;
                    case OpusEvent.PLAYING_FAILED:
                        mBtnPlay.setImageResource(R.drawable.btn_play);
                        break;
                    case OpusEvent.PLAYING_FINISHED:
                        mBtnPlay.setImageResource(R.drawable.btn_play);
                        mTvPosition.setText(new Utils.AudioTime().getTime());
                        mPlaySeekBar.setProgress(0);
                        break;
                    case OpusEvent.PLAYING_PAUSED:
                        mBtnPlay.setImageResource(R.drawable.btn_play);
                        break;
                    case OpusEvent.PLAYING_STARTED:
                        mBtnPlay.setImageResource(R.drawable.btn_pause);
                        break;
                    default:
                        Log.d(TAG, intent.toString() + "Invalid request,discarded");
                        break;
                }
            }
        }
    }


