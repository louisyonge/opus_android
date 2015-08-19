package top.oply.oplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.util.NoSuchElementException;

import pl.droidsonroids.gif.GifImageButton;
import top.oply.opuslib.OpusService;
import top.oply.opuslib.OpusTrackInfo;
import top.oply.opuslib.Utils;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FrgConvert#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FrgConvert extends Fragment implements View.OnClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "Title";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RelativeLayout rlConfigLayout;
    private ImageButton btnImportWav;
    private ImageButton btnConvertConfig;
    private GifImageButton btnConvert;
    private Button btnEncType;
    private Button btnEncComp;
    private Button btnEncBitrate;
    private Button btnEncFramesize;

    private ListView lvConfig;

    private MainActivity ma = null;


    public static final String TAG = FrgRecord.class.getName();
    private static final int REQUEST_CODE = 123;
    private static final String CHOOSEN_CONFIG_BTN = "CHOOSEN_CONFIG_BTN";
    private static final String IS_WAV_IMPORTED = "IS_WAV_IMPORTED";
    private static final String WAV_FILE_PATH = "WAV_FILE_PATH";
    private static final String IS_CONFIG_BTN_CLITCKED = "IS_CONFIG_BTN_CLITCKED";
    private static final String CONVERT_PARAM = "CONVERT_PARAM";
    private Bundle mFregState;
    private ConvertParam mConvParam = new ConvertParam();

    private String mWavFile = "";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FrgConvert.
     */
    // TODO: Rename and change types and number of parameters
    public static FrgConvert newInstance(String param1, String param2) {
        FrgConvert fragment = new FrgConvert();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FrgConvert() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mFregState = savedInstanceState.getBundle(TAG);
            mConvParam = (ConvertParam)(mFregState.getSerializable(CONVERT_PARAM));
            mWavFile = mFregState.get(WAV_FILE_PATH).toString();
        } else {
            mFregState = new Bundle();
            mFregState.putBoolean(IS_CONFIG_BTN_CLITCKED, false);
            mFregState.putBoolean(IS_WAV_IMPORTED, false);
            mFregState.putInt(CHOOSEN_CONFIG_BTN, R.id.btnEncBitRate);
            initConvertParam();
            mFregState.putSerializable(CONVERT_PARAM, mConvParam);
            mFregState.putString(WAV_FILE_PATH, mWavFile);
        }
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_frg_convert, container, false);
        if(ma == null) {
            throw new NoSuchElementException();
        } else {
            ma.initConverUI(v);
        }
        initUI(v);
        return v;
    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnImportWav:
                showChooser();
                break;
            case R.id.btnConverConfig:
                boolean isConfigClicked = !mFregState.getBoolean(IS_CONFIG_BTN_CLITCKED);
                mFregState.putBoolean(IS_CONFIG_BTN_CLITCKED, isConfigClicked);

                if(isConfigClicked) {
                    changeVisiblity(mFregState);
                    hilightEncBtn(mFregState.getInt(CHOOSEN_CONFIG_BTN));


                } else {
                    changeVisiblity(mFregState);
                }

                break;
            case R.id.btnEncBitRate:
                hilightEncBtn(id);
                break;
            case R.id.btnEncComp:
                hilightEncBtn(id);
                break;
            case R.id.btnEncFrameSize:
                hilightEncBtn(id);
                break;
            case R.id.btnEncType:
                hilightEncBtn(id);
                break;
            case R.id.configListV:
                break;
            case R.id.btnConvert:
                onConvertClick();
                break;
            default:
                break;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        mFregState.putString(WAV_FILE_PATH, mWavFile);
        mFregState.putSerializable(CONVERT_PARAM, mConvParam);
        outState.putBundle(TAG, mFregState);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ma = ((MainActivity)activity);
    }

    public void onResume() {
        changeVisiblity(mFregState);
        hilightEncBtn(mFregState.getInt(CHOOSEN_CONFIG_BTN));
        super.onResume();
    }

    @Override
    public void onDetach() {
        ma = null;
        super.onDetach();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(getActivity().getApplicationContext(), uri);
                            mWavFile = path;
                            String msg = getString(R.string.cfg_enc_WAV_file) + path;
                            ((TextView)getView().findViewById(R.id.tvWavFilePath)).setText(msg);
                        } catch (Exception e) {
                            Utils.printE(TAG, e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(target, getString(R.string.msg_choose_file));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            Utils.printE(TAG, e);
        }
    }
    private void onConvertClick() {
        if(mWavFile.isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(),getString(R.string.msg_err_convert_no_input), Toast.LENGTH_SHORT).show();
            return;
        }

        if(!Utils.isWAVFile(mWavFile)) {
            Toast.makeText(getActivity().getApplicationContext(),getString(R.string.msg_err_convert_not_wav), Toast.LENGTH_SHORT).show();
            return;
        }
        String output = OpusTrackInfo.getInstance().getAValidFileName(Utils.getFileName(mWavFile));
        String opting = mConvParam.getFinalSelections();
        OpusService.encode(getActivity().getApplicationContext(),mWavFile, output,opting);
    }

    private void hilightEncBtn(int sourceID) {
        View v = getView();
        if(v != null)
            hilightEncBtn(v, sourceID);
    }
    private void hilightEncBtn(View v, int sourceID) {
        int prev = mFregState.getInt(CHOOSEN_CONFIG_BTN);
        v.findViewById(prev).setBackgroundResource(R.color.none);

        mFregState.putInt(CHOOSEN_CONFIG_BTN, sourceID);
        v.findViewById(sourceID).setBackgroundResource(R.color.mchoosen);

        changeListViewData(sourceID);
    }

    private void changeVisiblity(Bundle b) {
        int visible = View.INVISIBLE;
        if(b.getBoolean(IS_CONFIG_BTN_CLITCKED, false)) {
            visible = View.VISIBLE;
            btnConvertConfig.setImageResource(R.mipmap.icon_convert_config_pressed);
        } else {
            btnConvertConfig.setImageResource(R.mipmap.icon_convert_config);
        }
        btnEncType.setVisibility(visible);
        btnEncComp.setVisibility(visible);
        btnEncBitrate.setVisibility(visible);
        btnEncFramesize.setVisibility(visible);
        lvConfig.setVisibility(visible);
    }

    private void initUI(View v) {
        rlConfigLayout =(RelativeLayout)v.findViewById(R.id.configLayout);
        btnImportWav = (ImageButton)v.findViewById(R.id.btnImportWav);
        btnConvertConfig = (ImageButton)v.findViewById(R.id.btnConverConfig);
        btnConvert = (GifImageButton)v.findViewById(R.id.btnConvert);
        btnEncType = (Button)v.findViewById(R.id.btnEncType);
        btnEncComp = (Button)v.findViewById(R.id.btnEncComp);
        btnEncBitrate = (Button)v.findViewById(R.id.btnEncBitRate);
        btnEncFramesize = (Button)v.findViewById(R.id.btnEncFrameSize);
        lvConfig = (ListView)v.findViewById(R.id.configListV);
        lvConfig.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        rlConfigLayout.setOnClickListener(this);
        btnImportWav.setOnClickListener(this);
        btnConvert.setOnClickListener(this);
        btnConvertConfig.setOnClickListener(this);
        btnEncType.setOnClickListener(this);
        btnEncComp.setOnClickListener(this);
        btnEncBitrate.setOnClickListener(this);
        btnEncFramesize.setOnClickListener(this);
        lvConfig.setOnItemClickListener(new MyItemClickListener());

        if (!mWavFile.isEmpty()) {
            String msg = getString(R.string.cfg_enc_WAV_file) + mWavFile;
            ((TextView)v.findViewById(R.id.tvWavFilePath)).setText(msg);
        }

    }

    private void changeListViewData(int id) {
        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                android.R.layout.simple_list_item_single_choice, mConvParam.getValues(id)) ;
        lvConfig.setAdapter(mAdapter);
        int index = mConvParam.getSelectedIndex(id);
        lvConfig.setSelection(index);
        lvConfig.setItemChecked(index, true);
        mAdapter.notifyDataSetChanged();
    }


    private void initConvertParam() {
        String[] rates =
                {" 6"," 16",
                " 32"," 64",
                " 128"," 178",
                " 256"," 320",
                " 384"," 512"}  ;
        String[] comps = {" 0"," 1"," 2"," 3"," 4"," 5"," 6"," 7"," 8"," 9"," 10"};
        String[]  frames = {" 2.5"," 5"," 10"," 20"," 40"," 60"};
        String[] types = {" --vbr", " --cvbr", " --hard-cbr"};
        mConvParam.add(R.id.btnEncBitRate, " --bitrate", rates);
        mConvParam.add(R.id.btnEncType,"", types);
        mConvParam.add(R.id.btnEncComp," --comp", comps);
        mConvParam.add(R.id.btnEncFrameSize," --framesize", frames);

        mConvParam.select(R.id.btnEncType, 0);
        mConvParam.select(R.id.btnEncBitRate, 3);
        mConvParam.select(R.id.btnEncComp, 10);
        mConvParam.select(R.id.btnEncFrameSize, 3);
    }
    private class MyItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            int ind = mFregState.getInt(CHOOSEN_CONFIG_BTN);
            mConvParam.select(ind,(int)id);
        }
    }
}
