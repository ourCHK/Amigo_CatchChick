package com.gionee.catchchick;

import android.app.Activity;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.gionee.catchchick.source.BgSrc;
import com.gionee.catchchick.utils.SoundUtils;
import com.gionee.catchchick.widget.FrameAnimation;


public class MainActivity extends Activity {


    private FrameAnimation mFrameAnimation;

    private final String TAG = MainActivity.class.getSimpleName();


    private int currentIndex;
    private int gamePlayID;
    private SoundPool gameSoundPool;
    private SoundPool backgroundSoundPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFrameAnimation = (FrameAnimation)findViewById(R.id.frame_animation);

        initAnimation();
    }


    /**
     * Init the SoundPool
     */
    private void initSoundPool() {
        SoundUtils.initSource(BgSrc.rawIds);
        backgroundSoundPool = SoundUtils.getBackgroundSoundPool();
        gameSoundPool = SoundUtils.getGameSoundPool();
        SoundUtils.loadBackgroundSoundPool(backgroundSoundPool,this);
        SoundUtils.loadGameSoundPool(gameSoundPool, this);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                gamePlayID = SoundUtils.playGameSound(gameSoundPool);
                // TODO 当 currentIndex = 100 的时候停止播放音频
                mFrameAnimation.setFlag(FrameAnimation.FLAG_PLAY_IN_ORDER);
                break;
            case MotionEvent.ACTION_UP:
                mFrameAnimation.setFlag(FrameAnimation.FLAG_PLAY_IN_REVERSE_ORDER);

                SoundUtils.stopSound(gameSoundPool,gamePlayID);

                break;
        }
        return super.onTouchEvent(event);
    }


    private void initAnimation() {
        mFrameAnimation.setBitmapResoursID(BgSrc.srcId);
        mFrameAnimation.setFlag(FrameAnimation.FLAG_INIT);
        mFrameAnimation.setGapTime(150);
    }


    /**
     * Init the SoundPool and play the background music
     */
    @Override
    protected void onResume() {
        super.onResume();
        initSoundPool();
        SoundUtils.playBackgroundSound(backgroundSoundPool);
    }


    /**
     * Releases the resources
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        backgroundSoundPool.release();
        gameSoundPool.release();
    }


    /**
     * Sets the activity with an immersive experience
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
