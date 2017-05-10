package com.pl.multicast.kraken;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * Created by Luxon on 08/05/2017.
 */
public class KrakenAudio {

    private static AudioTrack audiotrack;
    private KrakenCache kbuffer;
    private boolean isplaying;

    public KrakenAudio() {
        audiotrack = null;
        isplaying = false;
        kbuffer = new KrakenCache();
    }

    public void configure(int samplerate, boolean stereo, int duration) {

        Log.i(getClass().getName(), "audio  — create");

        if (audiotrack != null) {
            stop();
            clearAudio();
        }

        audiotrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplerate, (stereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO),
                AudioFormat.ENCODING_PCM_16BIT, (duration * samplerate),
                AudioTrack.MODE_STREAM);
    }

    public void streamData(byte[] data) {

        Log.v(getClass().getName(), "audio  — stream");
        if (audiotrack != null) {

            if (!isplaying) {
                Log.v(getClass().getName(), "audio  — play");
                audiotrack.play();
                isplaying = true;
            }
            Log.v(getClass().getName(), "audio  — write");
            audiotrack.write(data, 0, data.length);
        }
    }

    public void stop() {

        Log.i(getClass().getName(), "audio  — stop");
        if (audiotrack.getState() == AudioTrack.STATE_INITIALIZED
                && audiotrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audiotrack.stop();
            isplaying = false;
        }
    }

    public void clearAudio() {

        Log.i(getClass().getName(), "audio  — release");

        if (audiotrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audiotrack.release();
        }
    }
}
