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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
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

import pl.droidsonroids.gif.GifImageButton;
import top.oply.opuslib.OpusEvent;
import top.oply.opuslib.OpusService;
import top.oply.opuslib.OpusTrackInfo;
import top.oply.opuslib.Utils;


public class MainActivity extends FragmentActivity {
    private static final String TAG = MainActivity.class.getName();
    private static final String SCROLL_LIST_POSITON = "SCROLL_LIST_POSITON";
    private static final String SONG_PLAYING_POSITION = "SONG_PLAYING_POSITION";
    private static final String SONG_LIST = "SONG_LIST";
    private static final String PHASE_PLAY = "PHASE_PLAY";
    private static final String PHASE_RECORD = "PHASE_RECORD";
    private static final String PHASE_CONVERT = "PHASE_CONVERT";


    private OpusReceiver mReceiver = null;
    private ImageButton mBtnPlay = null;
    private ImageButton mBtnStop = null;
    private ImageButton mBtnRecord = null;
    private TextView mTvRecordTime = null;
    private ListView mLvSongs = null;
    private SeekBar mPlaySeekBar = null;
    private TextView mTvPosition = null;
    private TextView mTvDuration = null;
    private GifImageButton mBtnConvert = null;

    private FrgPlay frgPlay = null;
    private FrgRecord frgRecord = null;
    private FrgConvert frgConvert = null;


    private MyListViewSimAdaptor mAdapter;
    private OpusTrackInfo.AudioPlayList mSonglist;

    private int listScrollPosition = -1;
    private boolean mPhaseBtnPlay = false;
    private boolean mPhaseBtnRecord = false;
    private boolean mPhaseBtnConvert = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        int songPlayingPosition = -1;
        if(savedInstanceState == null) {
            mSonglist = new OpusTrackInfo.AudioPlayList();

        } else {
            listScrollPosition = savedInstanceState.getInt(SCROLL_LIST_POSITON);
            mSonglist = (OpusTrackInfo.AudioPlayList)
                    (savedInstanceState.getSerializable(SONG_LIST));
            mPhaseBtnConvert = savedInstanceState.getBoolean(PHASE_CONVERT);
            mPhaseBtnPlay = savedInstanceState.getBoolean(PHASE_PLAY);
            mPhaseBtnRecord = savedInstanceState.getBoolean(PHASE_RECORD);
            songPlayingPosition = savedInstanceState.getInt(SONG_PLAYING_POSITION);
        }
        mAdapter = new MyListViewSimAdaptor(getApplicationContext(), mSonglist.getList(), R.layout.playlist_view,
                new String[]{OpusTrackInfo.TITLE_TITLE, OpusTrackInfo.TITLE_DURATION,
                        OpusTrackInfo.TITLE_IMG, OpusTrackInfo.TITLE_ABS_PATH, OpusTrackInfo.TITLE_IS_CHECKED},
                new int[]{R.id.title, R.id.duration, R.id.img, R.id.absPath, R.id.isChecked});
        mAdapter.setHilighedItemPosition(songPlayingPosition);
        initUI();
        initBroadcast();
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SCROLL_LIST_POSITON, listScrollPosition);
        outState.putInt(SONG_PLAYING_POSITION, mAdapter.getHilighedItemPosition());
        outState.putSerializable(SONG_LIST, mSonglist);
        outState.putBoolean(PHASE_CONVERT, mPhaseBtnConvert);
        outState.putBoolean(PHASE_RECORD, mPhaseBtnRecord);
        outState.putBoolean(PHASE_PLAY, mPhaseBtnPlay);
        super.onSaveInstanceState(outState);
    }

    public void onResume() {
        super.onResume();
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
        changeBtnRecordStatus(mPhaseBtnRecord);
    }

    public void initConverUI(View v) {
        mBtnConvert = (GifImageButton)v.findViewById(R.id.btnConvert);
        changeBtnConvertStatus(mPhaseBtnConvert);
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



        mLvSongs.setAdapter(mAdapter);

        mLvSongs.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mLvSongs.setOnItemClickListener(new MySongListClickListener());
        mLvSongs.setOnScrollListener(new MySongListScronllListener());
        if(listScrollPosition != -1)
            mLvSongs.setSelection(listScrollPosition);
        //only to start service
        OpusService.getTrackInfo(getApplicationContext());
        changeBtnPlayStatus(mPhaseBtnPlay);
    }

    private void initUI() {
        //Set the pager with an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        ArrayList<Fragment> fragments = new ArrayList<Fragment>();

        frgPlay = FrgPlay.newInstance(getString(R.string.frg_play), "");
        frgRecord = FrgRecord.newInstance(getString(R.string.frg_record), "");
        frgConvert = FrgConvert.newInstance(getString(R.string.frg_convert), "");

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


    public boolean isSongListEmpty() {

        if (mSonglist.size() == 0) {
            Toast.makeText(this, getString(R.string.msg_err_playlist_empty), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    public void onBtnPlayClick(View view) {
        if(isSongListEmpty())
            return;
        Map<String, Object> m = mAdapter.getHilightedItem();
        if(m != null) {
            String filaName = m.get(OpusTrackInfo.TITLE_ABS_PATH).toString();
            OpusService.toggle(getApplicationContext(), filaName);
        }
    }

    public void onBtnPrevClick(View view) {
        if(mAdapter.hilighItemByOffset(-1)) {
            Map<String, Object> m = mAdapter.getHilightedItem();
            if(m != null) {
                String filaName = m.get(OpusTrackInfo.TITLE_ABS_PATH).toString();
                OpusService.play(getApplicationContext(), filaName);
            }
        }
    }

    public void onBtnNextClick(View view) {
        if(mAdapter.hilighItemByOffset(1)) {
            Map<String, Object> m = mAdapter.getHilightedItem();
            if(m != null) {
                String filaName = m.get(OpusTrackInfo.TITLE_ABS_PATH).toString();
                OpusService.play(getApplicationContext(), filaName);
            }
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
//                Toast.makeText(getApplicationContext(), "position is:" + position + " id: " + id, Toast.LENGTH_SHORT).show();
                mAdapter.hilightItem(position);
                String filaName = mSonglist.getList().get(position).get(OpusTrackInfo.TITLE_ABS_PATH).toString();
                OpusService.play(getApplicationContext(), filaName);
            }
        }

    public class MyListViewSimAdaptor extends SimpleAdapter {
        private int lastHilighedItemPosition = -1;
        private LayoutInflater mInflater;

        public MyListViewSimAdaptor(Context context, List<Map<String, Object>> data,
                                  int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setHilighedItemPosition(int position) {
            lastHilighedItemPosition = position;
        }

        public int getHilighedItemPosition() {
            return lastHilighedItemPosition;
        }

        public boolean hilighItemByOffset(int offset) {
            return hilightItem(lastHilighedItemPosition + offset);
        }

        public boolean hilightItem(int position) {
            try {
                Map<String, Object> tmp;
                if(lastHilighedItemPosition >=0 && lastHilighedItemPosition < getCount()) {
                    tmp = (Map<String, Object>)getItem(lastHilighedItemPosition);
                    tmp.put(OpusTrackInfo.TITLE_IS_CHECKED,false);
                }
                if(position >=0 && position < getCount()){
                    tmp = (Map<String, Object>)getItem(position);
                    tmp.put(OpusTrackInfo.TITLE_IS_CHECKED,true);
                    lastHilighedItemPosition = position;
                    notifyDataSetChanged();
                    return true;
                }
            } catch (Exception e) {
                Utils.printE(TAG, e);
                return false;
            }
            return false;
        }

        public Map<String, Object> getHilightedItem() {
            if(lastHilighedItemPosition < 0 && getCount() >0) {
                hilightItem(0);
                return (Map<String, Object>)(getItem(0));
            }
            if(lastHilighedItemPosition < 0 || lastHilighedItemPosition >= getCount())
                return null;
            return (Map<String, Object>)(getItem(lastHilighedItemPosition));
        }

        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder viewHolder = null;
//            if (convertView == null) {
//                viewHolder = new ViewHolder();
//                convertView = mInflater.inflate(R.layout.playlist_view, null);
//                viewHolder.checkBox = (CheckBox)convertView.findViewById(R.id.isChecked);
//                convertView.setTag(viewHolder);
//            } else {
//                viewHolder = (ViewHolder) convertView.getTag();
//            }
//
//            return convertView;
            return super.getView(position, convertView, parent);
        }

        class ViewHolder{
            public CheckBox checkBox = null;
        }
    }

    class MySongListScronllListener implements AbsListView.OnScrollListener {
        public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
        }
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if(scrollState== AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                listScrollPosition = mLvSongs.getFirstVisiblePosition();
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

    private void changeBtnConvertStatus(boolean b) {


        if (b) { //converting
            Animation rotate = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.converting);
//            mBtnConvert.setImageResource(R.mipmap.icon_converting);
            mBtnConvert.setImageResource(R.mipmap.icon_converting);
            mBtnConvert.startAnimation(rotate);
            mBtnConvert.setClickable(false);

        } else { //idle
            Animation anim = mBtnConvert.getAnimation();
            if (anim != null) {
                mBtnConvert.getAnimation().cancel();
                mBtnConvert.clearAnimation();
            }
            mBtnConvert.setImageResource(R.drawable.btn_convert_selector);
            mBtnConvert.setClickable(true);
        }
        mPhaseBtnConvert = b;

    }
    private void changeBtnPlayStatus(boolean b) {
        if (b) { //Playing
            mBtnPlay.setImageResource(R.drawable.btn_pause_selector);

        } else { //idle
            mBtnPlay.setImageResource(R.drawable.btn_play_selector);
        }
        mPhaseBtnPlay = b;

    }

    private void changeBtnRecordStatus(boolean b) {
        if (b) { //Playing
            mBtnRecord.setImageResource(R.drawable.btn_stop_recording_selector);

        } else { //idle
            mBtnRecord.setImageResource(R.drawable.btn_record_selector);
        }
        mPhaseBtnConvert = b;

    }

    class OpusReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            int type = bundle.getInt(OpusEvent.EVENT_TYPE, 0);
            switch (type) {
                case OpusEvent.CONVERT_FINISHED:
                    String msg = bundle.getString(OpusEvent.EVENT_MSG);
                    Toast.makeText(getApplicationContext(),getString(R.string.msg_convert_succ) + msg
                            , Toast.LENGTH_LONG).show();
                    changeBtnConvertStatus(false);
                    break;
                case OpusEvent.CONVERT_FAILED:
                    Toast.makeText(getApplicationContext(),getString(R.string.msg_err_convert_failed)
                            , Toast.LENGTH_SHORT).show();
                    changeBtnConvertStatus(false);
                    break;
                case OpusEvent.CONVERT_STARTED:
                    changeBtnConvertStatus(true);
                    break;
                case OpusEvent.RECORD_FAILED:
                    changeBtnRecordStatus(false);
                    Toast.makeText(getApplicationContext(),getString(R.string.msg_err_record_failed)
                            , Toast.LENGTH_SHORT).show();
                    break;
                case OpusEvent.RECORD_FINISHED:
                    changeBtnRecordStatus(false);
                    msg = bundle.getString(OpusEvent.EVENT_MSG);
                    Toast.makeText(getApplicationContext(),getString(R.string.msg_record_succ)
                            + msg, Toast.LENGTH_LONG).show();
                    break;
                case OpusEvent.RECORD_STARTED:
                    changeBtnRecordStatus(true);
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
                    mTvPosition.setText(t.getTime());
                    t.setTimeInSecond(duration);
                    mTvDuration.setText(t.getTime());
                    if(duration != 0) {
                        int progress = (int) (100 * position / duration);
                        mPlaySeekBar.setProgress(progress);
                    }
                    break;
                case OpusEvent.PLAY_GET_AUDIO_TRACK_INFO:
                    List<Map<String, Object>> songlst = ((OpusTrackInfo.AudioPlayList) (bundle
                            .getSerializable(OpusEvent.EVENT_PLAY_TRACK_INFO))).getList();
                    mSonglist.clear();
                    for (Map<String, Object> map : songlst) {
                        //TODO this is a test
                        if (map.get(OpusTrackInfo.TITLE_IMG).equals(0)) {
                            map.put(OpusTrackInfo.TITLE_IMG, R.drawable.default_music_icon);
                            mSonglist.add(map);
                        }
                    }
                    mAdapter.hilightItem(mAdapter.getHilighedItemPosition());
                    mAdapter.notifyDataSetChanged();
                    break;
                case OpusEvent.PLAYING_FAILED:
                    changeBtnPlayStatus(false);
                    break;
                case OpusEvent.PLAYING_FINISHED:
                    changeBtnPlayStatus(false);
                    mTvPosition.setText(new Utils.AudioTime().getTime());
                    mPlaySeekBar.setProgress(0);
                    break;
                case OpusEvent.PLAYING_PAUSED:
                    changeBtnPlayStatus(false);
                    break;
                case OpusEvent.PLAYING_STARTED:
                    changeBtnPlayStatus(true);
                    break;
                default:
                    Log.d(TAG, intent.toString() + "Invalid request,discarded");
                    break;
            }
        }
    }

}


