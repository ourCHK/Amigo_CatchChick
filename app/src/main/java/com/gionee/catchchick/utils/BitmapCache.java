package com.gionee.catchchick.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.util.SparseIntArray;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static android.content.ContentValues.TAG;

/**
 * File Description:
 * Author:weisc
 * Create Date:18-1-10
 * Change List:
 */

public class BitmapCache {
    private final static Set<SoftReference<Bitmap>> recycleBitmap = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
    private final LruCache<Integer, Bitmap> cache;
    private final SparseIntArray removeInFuture = new SparseIntArray();
    private final SparseIntArray testMap = new SparseIntArray();

    private int bitmapNum;


    public int getBitmapNum() {
        return bitmapNum;
    }

    public void setBitmapNum(int bitmapNum) {
        this.bitmapNum = bitmapNum;
    }


    public BitmapCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory * 4 / 5;
        cache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected void entryRemoved(boolean evicted, Integer key,
                                        Bitmap oldValue, Bitmap newValue) {
                recycleBitmap.add
                        (new SoftReference<>(oldValue));
                testMap.delete(key);
            }

            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

    }


    public synchronized void put(int key, Bitmap bitmap) {
        int needRemoveCount = removeInFuture.get(key, 0);
        if (needRemoveCount == 0) {
            cache.put(key, bitmap);
            testMap.put(key, key);
        } else {
            if (--needRemoveCount == 0) {
                removeInFuture.delete(key);
            } else {
                removeInFuture.put(key, needRemoveCount);
            }
        }
        notify();
    }


    /**
     * Gets bitmap from cache by key
     * @param key
     * @return
     */
    public synchronized Bitmap get(int key) {
        Bitmap bitmap;
        while ((cache.get(key)) == null) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        bitmap = cache.get(key);
        notify();
        return bitmap;
    }


    /**
     * Removes the bitmap from cache by key
     * @param key
     */
    public synchronized void remove(int key) {
        if (!contain(key)) {
            int oldValue = removeInFuture.get(key, 0);
            removeInFuture.put(key, oldValue + 1);
            return;
        }
        cache.remove(key);
    }


    /**
     * Clear the cache
     */
    public void clear() {
        cache.evictAll();

    }


    /**
     *Determines whether include in cache by key
     * @param key
     * @return
     */
    public boolean contain(int key) {
        return !(cache.get(key) == null);
    }


    /**
     * Decodes the bitmap from InputStream
     * @param inputStream
     * @return
     */
    public static Bitmap decodeBitmapFromStream(InputStream inputStream) {
        long start = System.currentTimeMillis();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inPreferredConfig = Bitmap.Config.ALPHA_8;
        addInBitmapOptions(options);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        Log.d(TAG, "decode From Stream" + (System.currentTimeMillis() - start));
        return bitmap;
    }


    /**
     * Gets the bitmap from Resources
     * @return
     */
    private static Bitmap getBitmapFromReusableSet() {
        Bitmap bitmap = null;
        if (recycleBitmap != null && !recycleBitmap.isEmpty()) {
            synchronized (recycleBitmap) {
                final Iterator<SoftReference<Bitmap>> iterator = recycleBitmap.iterator();
                Bitmap item;
                while (iterator.hasNext()) {
                    item = iterator.next().get();

                    if (null != item && item.isMutable()) {
                        bitmap = item;
                        iterator.remove();
                        break;
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }


    /**
     * Adds the bitmap options
     * @param options
     */
    private static void addInBitmapOptions(BitmapFactory.Options options) {
        options.inMutable = true;
        Bitmap inBitmap = getBitmapFromReusableSet();
        if (inBitmap != null) {
            options.inBitmap = inBitmap;
        }
    }
}
