package com.pl.multicast.kraken;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.Arrays;

/**
 * Created by Luxon on 08/05/2017.
 */
public class KrakenAudio {

    private KrakenCache kbuffer;
    private static AudioTrack audiotrack;
    private boolean isplaying;

    public KrakenAudio() {
        audiotrack = null;
        isplaying = false;
        kbuffer = new KrakenCache();
    }

    public void putInCacheMemory(byte [] arr, int len) {

        // write into the cache memory
        kbuffer.write(arr,len);

        if(kbuffer.isFull()) {

            byte [] by = kbuffer.readAll();
            Log.i(getClass().getName(), "recv  — cache");
            // blocking write
            if(audiotrack != null) {

                if(!isplaying) {
                    Log.i(getClass().getName(), "audio  — play");
                    audiotrack.play();
                    isplaying = true;
                }
                Log.i(getClass().getName(), "audio  — write");
                audiotrack.write(by, 0, by.length);
            }
        }
    }

    public void play(int samplerate, boolean stereo, int duration){

        Log.i(getClass().getName(), "audio  — create");
        audiotrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplerate, (stereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO),
                AudioFormat.ENCODING_PCM_16BIT, (duration * samplerate),
                AudioTrack.MODE_STREAM);
    }

    public void stop() {

        Log.i(getClass().getName(), "audio  — stop");
        audiotrack.stop();
        isplaying = false;
    }

    public void clearAudio() {

        Log.i(getClass().getName(), "audio  — release");
        audiotrack.pause();
        audiotrack.flush();
    }
}
