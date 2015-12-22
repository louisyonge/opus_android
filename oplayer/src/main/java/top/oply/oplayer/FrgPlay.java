package top.oply.oplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.NoSuchElementException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FrgPlay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FrgPlay extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "Title";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MainActivity ma = null;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FrgPlay.
     */
    // TODO: Rename and change types and number of parameters
    public static FrgPlay newInstance(String param1, String param2) {
        FrgPlay fragment = new FrgPlay();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FrgPlay() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
//    public void onBtnPlayClick(View view) {
//        ma.onBtnPlayClick(view);
//    }
//    public void onBtnStopClick(View view) {
//        ma.onBtnStopClick(view);
//    }
//    public void onBtnPrevClick(View view) {
//
//    }
//
//    public void onBtnNextClick(View view) {
//
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_frg_play, container, false);
        if(ma == null) {
            throw new NoSuchElementException();
        } else {
            ma.initPlayUI(v);
        }
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ma = ((MainActivity)activity);
    }

    @Override
    public void onDetach() {
        ma = null;
        super.onDetach();
    }

}
