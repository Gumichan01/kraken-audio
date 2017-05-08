package com.pl.multicast.kraken;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by Luxon on 08/05/2017.
 */
public class KrakenAudio {

    public static final int DATAPCK_SIZE = 1024;
    private KrakenCache kbuffer;
    private AudioTrack audiotrack;

    public KrakenAudio() {
        audiotrack = null;
        kbuffer = new KrakenCache();
    }

    public void putInCacheMemory(byte [] arr, int len) {

        Log.i(getClass().getName(), "cache memory");
        kbuffer.write(arr,len);

        if(kbuffer.isFull() || !kbuffer.isEmpty()) {

            byte [] by = kbuffer.read(DATAPCK_SIZE);
            System.out.println("recv  — cache");
            for (byte b : by) {
                Log.i(getClass().getName(), "" + b);
            }
            System.out.println("recv  — cache end");
        }

        // blocking write
        if(audiotrack != null)
            audiotrack.write(arr, 0, len);
    }

    public void play(int samplerate, boolean stereo, int duration){

        audiotrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplerate, (stereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO),
                AudioFormat.ENCODING_PCM_16BIT, (duration * samplerate),
                AudioTrack.MODE_STREAM);

        new Thread(new Runnable() {
            @Override
            public void run() {
                audiotrack.play();
            }
        }).start();
    }

    public void stop() {

        audiotrack.stop();
    }

    public void clearAudio() {

        audiotrack.pause();
        audiotrack.flush();
    }
}
