package com.gionee.catchchick.widget;

/**
 * Created by louis on 18-1-6.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.gionee.catchchick.utils.BitmapLoader;

public class FrameAnimation extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mSurfaceHolder;

    private boolean mIsThreadRunning = true;
    public static boolean mIsDestroy = false;

    private int[] mBitmapResourceIds;
    private int totalCount;
    private Canvas mCanvas;
    private Bitmap mBitmap;


    private int mCurrentIndex;
    private int mGapTime = 200;
    public static final int FLAG_PLAY_IN_ORDER = 0;
    public static final int FLAG_PLAY_IN_REVERSE_ORDER = 1;
    public static final int FLAG_INIT = 2;
    private int flag = FLAG_PLAY_IN_ORDER;

    private final String TAG = FrameAnimation.class.getSimpleName();
    private OnFrameFinishedListener mOnFrameFinishedListener;
    private AnimThread animThread;
    private BitmapFactory.Options options;
    private boolean isSetDrawing = true;

    private BitmapLoader mBitmapLoader;
    private Context mContext;

    public FrameAnimation(Context context) {
        this(context, null);
        init(context);
    }

    public FrameAnimation(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public FrameAnimation(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context);

    }

    private void init(Context context) {
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);

        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);
        options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        mContext = context;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mBitmapLoader = BitmapLoader.getLoaderInstance();
        start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mBitmapLoader.destroy();
    }


    public int getCurrentIndex() {
        return mCurrentIndex;
    }


    public void setCurrentIndex(int index) {
        this.mCurrentIndex = index;
    }

    public void stopThread() {
        mIsThreadRunning = false;
        try {
            animThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void start() {
        if (!mIsDestroy) {
            mIsThreadRunning = true;
            animThread = new AnimThread();
            animThread.start();
        } else {
            try {
                throw new Exception("IllegalArgumentException:Are you sure the SurfaceHolder is not destroyed");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class AnimThread extends Thread {
        @Override
        public void run() {
            super.run();

            if (mOnFrameFinishedListener != null) {
                mOnFrameFinishedListener.onStart();
            }

            while (mIsThreadRunning) {
                Log.d(TAG, "mCurrentIndex========" + mCurrentIndex);
                drawView();
                try {
//                    Thread.sleep(mCurrentIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mOnFrameFinishedListener != null) {
                mOnFrameFinishedListener.onStop();
            }

        }
    }


    public void setFlag(int flag) {
        this.flag = flag;
    }


    /**
     * Draws the view
     */
    private void drawView() {
        if (mBitmapResourceIds == null) {
            Log.e("frameview", "the bitmapsrcIDs is null");
            mIsThreadRunning = false;
            return;
        }

        if (mSurfaceHolder != null) {
            mCanvas = mSurfaceHolder.lockCanvas();
        }
        try {
            if (mSurfaceHolder != null && mCanvas != null) {

                mCanvas.drawColor(Color.WHITE);
                long start = System.currentTimeMillis();
                mBitmap = mBitmapLoader.loadResources(mContext, mCurrentIndex);
                Log.d("timec", "drawView: cost " + (System.currentTimeMillis() - start));
                Matrix mMatrix = new Matrix();
                mMatrix.postScale((float) getWidth() / mBitmap.getWidth(),
                        (float) getHeight() / mBitmap.getHeight());
                long l = SystemClock.currentThreadTimeMillis();

                mCanvas.drawBitmap(mBitmap, mMatrix, null);
//                Log.d(TAG, "=========" + (SystemClock.currentThreadTimeMillis() - l));

                if (mBitmap != null) {
                    if (mBitmap.isRecycled()) {
                        mBitmap.recycle();
                        mBitmap = null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            switch (flag) {
                case FLAG_PLAY_IN_ORDER:
                    mCurrentIndex++;
                    if (mCurrentIndex == 100) {
//                        Log.d(TAG, "=========" + (SystemClock.currentThreadTimeMillis() - l1));
                    }
                    break;
                case FLAG_PLAY_IN_REVERSE_ORDER:
                    mCurrentIndex--;

                    break;
                case FLAG_INIT:
                    mCurrentIndex = 0;
                    break;
            }


            if (mCurrentIndex >= totalCount) {
                mCurrentIndex = totalCount - 1;
            }
            if (mCurrentIndex < 0) {
                mCurrentIndex = 0;
            }

            if (mCanvas != null) {
                if (mSurfaceHolder != null) {
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }

            if (mBitmap != null) {
                if (mBitmap.isRecycled()) {
                    mBitmap.recycle();
                    mBitmap = null;
                }
            }
        }
    }

    /**
     * Sets the bitmap ID
     *
     * @param bitmapResourceIds
     */
    public void setBitmapResoursID(int[] bitmapResourceIds) {
        this.mBitmapResourceIds = bitmapResourceIds;
        totalCount = bitmapResourceIds.length;
        for (int i = 0; i < 3; i++) {

        }
    }


    /**
     * Sets the time of gap
     */
    public void setGapTime(int gapTime) {
        this.mGapTime = gapTime;
    }

    /**
     * Stops the animation
     */
    public void stop() {
        mIsThreadRunning = false;
    }

    /**
     * Resumes the animation
     */
    public void reStart() {
        mIsThreadRunning = true;
    }

    /**
     * Sets the listener of animation
     */
    public void setOnFrameFinisedListener(OnFrameFinishedListener onFrameFinishedListener) {
        this.mOnFrameFinishedListener = onFrameFinishedListener;
    }

    /**
     * Interface of animation listener
     *
     * @author qike
     */
    public interface OnFrameFinishedListener {

        void onStart();

        void onStop();
    }

    /**
     * Stops the thread
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsThreadRunning = false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
