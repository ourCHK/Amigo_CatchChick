package com.gionee.catchchick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.util.LruCache;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {

    private SurfaceHolder mSurfaceHolder;
    private SurfaceView mSurfaceView;
    private Resources mResources;
    private Canvas mCanvas;
    private Rect mRect;
    private Rect mChickRect;
    private Bitmap mBitmap;
    private int[] mChickId;
    private int mPressure;


    private String mPackageName;

    private boolean isDown;
    private boolean isUp;

    private SparseIntArray inTask;

    private int mWidth;
    private int mHeight;

    private LruCache<String, Bitmap> mLruCache;
    private ThreadPoolExecutor executor;

    private MediaPlayer mBgMusic;
    private MediaPlayer mCrow;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        init();

        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isDown = true;
                        isUp = false;
                        mCrow.start();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int i = mPressure;
                                while (isDown) {
                                    if (i < 101) {
                                        setPressure(i);
                                        if (mPressure <= 80) {
//                                         SystemClock.sleep(50);
                                        }
                                        i++;
                                    }
                                }
                            }
                        }).start();
                        break;
                    case MotionEvent.ACTION_UP:
                        isUp = true;
                        isDown = false;
                        mCrow.pause();
                        mCrow.seekTo(0);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int i = mPressure;
                                while (isUp) {
                                    if (mPressure > 0) {
                                        setPressure(i);
//                                        float ratio = (float) mPressure / i;
//                                        if (ratio <= 0) {
//                                            ratio = 1;
//                                        }
                                        i--;
//                                        long time = (long) (5 * ratio);
//                                        SystemClock.sleep(time);
                                    }
                                }
                            }
                        }).start();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }


    /**
     * Init the data and view
     */
    private void init() {
        //SurfaceHolder
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(callback);

        //prepare for the chickId
        mPackageName = this.getPackageName();
        mResources = getResources();

        //the finger is down or up
        isDown = false;
        isUp = false;

        //whether the bitmap is adding...
        inTask = new SparseIntArray();

        //init the chickId
        mChickId = new int[101];
        for (int i = 0; i < 101; i++) {
            mChickId[i] = getBitmapId(i);
            inTask.put(i, 0);
        }

        //init the LruCache
        int maxSize = (int) (Runtime.getRuntime().maxMemory()) / 1024;
        mLruCache = new LruCache<String, Bitmap>(maxSize * 2 / 3) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        //init the ThreadPoolExecutor and prepare the cache
        executor = new ThreadPoolExecutor(10, 15, 10, TimeUnit.MINUTES,
                new LinkedBlockingDeque<Runnable>(10));

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 7; i++) {
                    inTask.put(i, 1);
                    MyTask task = new MyTask(i);
                    executor.execute(task);
                }
            }
        }).start();

        //background music
        mCrow = MediaPlayer.create(this, R.raw.crow1);
        mBgMusic = MediaPlayer.create(this, R.raw.music_bg);
        mBgMusic.setVolume(0.6f, 0.6f);
        mBgMusic.setLooping(true);
        mBgMusic.start();
    }


    /**
     * Gets the bitmap by ID
     *
     * @param bitmapId
     * @return
     */
    private Bitmap getBitmap(int bitmapId) {
        Drawable drawable = mResources.getDrawable(bitmapId, null);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        return bitmap;
    }


    /**
     * Sets the pressure and prepares the cache
     *
     * @param pressure
     */
    private void setPressure(int pressure) {
        if (pressure <= 100 && pressure >= 0) {
            mPressure = pressure;
        } else if (pressure > 100) {
            mPressure = 100;
        } else if (pressure < 0) {
            mPressure = 0;
        }

        //remove the old cache
        if (pressure - 7 >= 0 && isDown) {
            removeBitmapFromMemoryCache(pressure - 7);
        }
        if (pressure + 7 <= 100 && isUp) {
            removeBitmapFromMemoryCache(pressure + 7);
        }

        //prepare the cache
        if (isDown) {
            for (int i = 4; i <= 7; i++) {
                if (pressure + i < 101 && inTask.get(pressure + i) == 0) {
                    inTask.put(pressure + i, 1);
                    MyTask task = new MyTask(pressure + i);
                    executor.execute(task);
                }
            }
        } else {
            for (int i = 4; i <= 7; i++) {
                if (pressure - i >= 0 && inTask.get(pressure - i) == 0) {
                    inTask.put(pressure - i, 1);
                    MyTask task = new MyTask(pressure - i);
                    executor.execute(task);
                }
            }
        }
        drawBitmap(mChickId[mPressure], mPressure);

    }


    /**
     * Gets the bitmap ID
     *
     * @param pressure
     * @return
     */
    private int getBitmapId(int pressure) {
        String name = "chick_";
        if (pressure < 10) {
            name = name + ("00") + pressure;
        } else if (pressure < 100 && pressure >= 10) {
            name = name + "0" + pressure;
        } else {
            name = name + pressure;
        }
        int bitmapId = mResources.getIdentifier(name, "mipmap", mPackageName);
        return bitmapId;
    }


    /**
     * Draws the bitmap by ID
     *
     * @param bitmapId
     * @param pressure
     */
    private synchronized void drawBitmap(int bitmapId, int pressure) {

        long time = System.currentTimeMillis();
        Log.d("pressure", "pressure : " + pressure);

        mBitmap = loadBitmap(bitmapId);

        Log.d("get", "get_time: " + (System.currentTimeMillis() - time) + " pressure: " + pressure);
        long time2 = System.currentTimeMillis();
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawBitmap(mBitmap, mChickRect, mRect, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCanvas != null) {
                mBitmap = null;
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                Log.d("draw", "draw_time: " + (System.currentTimeMillis() - time2) + " pressure: " + pressure);
            }
        }
        Log.d("total", "total: " + (System.currentTimeMillis() - time) + " pressure: " + pressure);
    }


    /**
     * Adds the bitmap to cache
     *
     * @param key
     * @param bitmap
     */
    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }


    /**
     * Removes the bitmap from cache by pressure
     *
     * @param pressure
     */
    private void removeBitmapFromMemoryCache(int pressure) {
        String key = String.valueOf(mChickId[pressure]);
        mLruCache.remove(key);
    }


    /**
     * Gets the bitmap form cache by key
     *
     * @param key
     * @return
     */
    private Bitmap getBitmapFromMemCache(String key) {
        return mLruCache.get(key);
    }


    /**
     * Loads the bitmap by ID
     *
     * @param bitmapId
     * @return
     */
    private Bitmap loadBitmap(int bitmapId) {
        String key = String.valueOf(bitmapId);
        Bitmap bitmap = getBitmapFromMemCache(key);
        if (bitmap != null) {
            return bitmap;
        } else {
            bitmap = getBitmap(bitmapId);
            return bitmap;
        }
    }


    /**
     * Class for addBitmapToMemoryCache running in multithreading
     */
    class MyTask implements Runnable {
        private int pressure;

        public MyTask(int pressure) {
            this.pressure = pressure;
        }

        @Override
        public void run() {
            String key = String.valueOf(mChickId[pressure]);
            Bitmap bitmap = getBitmap(mChickId[pressure]);
            addBitmapToMemoryCache(key, bitmap);
            inTask.put(pressure, 0);
        }
    }


    /**
     * Callback for SurfaceView
     */
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            drawBitmap(mChickId[0], 0);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mWidth = width;
            mHeight = height;
            mRect = new Rect(0, 0, mWidth, mHeight);
            mChickRect = new Rect(0, 0, mWidth, mHeight);
            drawBitmap(mChickId[0], 0);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };


    @Override
    protected void onResume() {
        Log.d("onResume", "onResume: ");
        super.onResume();
        mBgMusic.start();
    }

    @Override
    protected void onPause() {
        Log.d("onPause", "onPause: ");
        super.onPause();
        mBgMusic.pause();
        mCrow.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBgMusic.stop();
        mCrow.stop();
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