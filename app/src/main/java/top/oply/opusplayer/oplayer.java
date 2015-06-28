package top.oply.opusplayer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import top.oply.opuslib.OpusTool;
import top.oply.opusplayer.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class oplayer extends Activity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = false;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_oplayer);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public void btnDecClick(View view){
        TextView tv;
        tv = (TextView)findViewById(R.id.mainLog);

        String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
        String fileName = SDPATH + "test.opus";
        File f = new File(fileName);
        if (!f.exists()){
            String str = Utils.CurTime() + ": " + fileName + " is not exist, please put it there." + "\n" ;
            tv.setText(tv.getText() + str);
        }
        String fileNameOut = fileName + ".wav";

        tv.setText(tv.getText() + "Start decoding...\n");
        OpusTool oTool = new OpusTool();
        Log.d("encode:", oTool.nativeGetString());
        int result = oTool.decode(fileName,fileNameOut, null);
        if (result == 0){
            String str = Utils.CurTime() + ": " + "Decode is complete. Output file is: " + fileNameOut + "\n" ;
            tv.setText(tv.getText() + str);
        } else{
            String str = Utils.CurTime() + ": " + "Decode failed." + "\n" ;
            tv.setText(tv.getText() + str);
        }
    }

    public void btnEncClick(View view){
        TextView tv;
        tv = (TextView)findViewById(R.id.mainLog);

        String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
        String fileName = SDPATH + "test.wav";
        File f = new File(fileName);
        if (!f.exists()){
            String str = Utils.CurTime() + ": " + fileName + " is not exist, please put it there." + "\n" ;
            tv.setText(tv.getText() + str);
        }
        String fileNameOut = fileName + ".opus";

        tv.setText(tv.getText() + "Start encoding...\n");
        OpusTool oTool = new OpusTool();
        Log.d("encode:", oTool.nativeGetString());
        int result = oTool.encode(fileName,fileNameOut, null);
        if (result == 0){
            String str = Utils.CurTime() + ": " + "Encode is complete. Output file is: " + fileNameOut + "\n" ;
            tv.setText(tv.getText() + str);
        } else{
            String str = Utils.CurTime() + ": " + "Encode failed." + "\n" ;
            tv.setText(tv.getText() + str);
        }

    }

}
