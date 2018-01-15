package com.gionee.catchchick.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;

/**
 * Created by louis on 17-12-21.
 */

public class SoundUtils {

    private static final String TAG = "SoundUtils";
    private static SoundPool gameSoundPool;
    private static SoundPool backgroundSoundPool;
    private static int[] rawRes;
    private static int[] soundIDs;
    private static int[] playID;


    /**
     * Gets the Background SoundPool
     * @return
     */
    public static SoundPool getBackgroundSoundPool(){
        if (backgroundSoundPool == null) {
            if (Build.VERSION.SDK_INT >= 21) {
                SoundPool.Builder builder = new SoundPool.Builder();
                builder.setMaxStreams(1);
                AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
                attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
                builder.setAudioAttributes(attrBuilder.build());
                backgroundSoundPool = builder.build();
            } else {
                backgroundSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
            }
            return backgroundSoundPool;
        } else {
            return backgroundSoundPool;
        }
    }


    /**
     * Gets the game SoundPool
     * @return
     */
    public static SoundPool getGameSoundPool() {
        if (gameSoundPool == null) {
            if (Build.VERSION.SDK_INT >= 21) {
                SoundPool.Builder builder = new SoundPool.Builder();
                builder.setMaxStreams(1);
                AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
                attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
                builder.setAudioAttributes(attrBuilder.build());
                gameSoundPool = builder.build();
            } else {
                gameSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
            }
            return gameSoundPool;
        } else {
            return gameSoundPool;
        }
    }

    /**
     * Init the resources
     * @param rawIds
     */
    public static void initSource(int[] rawIds) {
        rawRes = new int[rawIds.length];
        for (int i = 0; i < rawIds.length; i++) {
            rawRes[i] = rawIds[i];
        }
        soundIDs = new int[rawRes.length];
        playID = new int[rawRes.length];
    }


    /**
     * Loads the background sound
     * @param soundPool
     * @param context
     */
    public static void loadBackgroundSoundPool(SoundPool soundPool,Context context){
        if (rawRes.length == 0) {
            return;
        }

        soundIDs[0] = soundPool.load(context,rawRes[0],1);
        Log.d(TAG, "loadGameSoundPool: soundIDs[" + 0 + "] = " + soundIDs[0]);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d(TAG, "onLoadComplete: load background resources success");
            }
        });
    }


    /**
     * Loads the game sound
     * @param soundPool
     * @param context
     */
    public static void loadGameSoundPool(SoundPool soundPool, Context context) {
        if (rawRes.length == 0) {
            return;
        }

        soundIDs[1] = soundPool.load(context,rawRes[1],1);
        Log.d(TAG, "loadGameSoundPool: soundIDs[" + 1 + "] = " + soundIDs[1]);

        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d(TAG, "onLoadComplete: load resources success");
            }
        });
    }


    /**
     * Plays the background sound
     * @param soundPool
     * @return
     */
    public static int playBackgroundSound(SoundPool soundPool) {
        playID[0] = soundPool.play(soundIDs[0],1,1,0,-1,1);
        return playID[0];
    }


    /**
     * Plays the game sound
     * @param soundPool
     * @return
     */
    public static int playGameSound(SoundPool soundPool) {
        playID[1] = soundPool.play(soundIDs[1],1,1,0,1,1);
        return playID[1];
    }


    /**
     * Stops the sound by ID
     * @param soundPool
     * @param playID
     */
    public static void stopSound(SoundPool soundPool, int playID) {
        Log.d(TAG, "stopSound: playID = " + playID);
        soundPool.stop(playID);
    }
}
