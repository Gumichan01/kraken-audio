package com.pl.multicast.kraken;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.ArrayList;

/**
 * KrakenAudio handles audio playing
 */
public class KrakenAudio {

    public static final int DEFAULT_FREQUENCY = 440;
    private static AudioTrack audiotrack;
    private ArrayList<KrakenSample> samples;

    private boolean isplaying;
    private boolean isstereo;
    private int frequency;

    public KrakenAudio() {
        audiotrack = null;
        samples = new ArrayList<>();
        isplaying = false;
        isstereo = false;
        frequency = DEFAULT_FREQUENCY;
    }

    public void setFrequency(int freq) {

        frequency = freq;
    }

    public void configure(int samplerate, boolean stereo, int duration) {

        //Log.i(getClass().getName(), "audio  — create");

        if (audiotrack != null) {
            stop();
            clearAudio();
        }

        isstereo = stereo;
        int numsamples = samplerate * (stereo ? 2 : 1);
        audiotrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplerate, (stereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO),
                AudioFormat.ENCODING_PCM_16BIT, numsamples, AudioTrack.MODE_STREAM);

        generateSound(numsamples, duration);
    }

    public synchronized void streamData(byte[] data) {

        //Log.v(getClass().getName(), "audio  — stream");
        if (audiotrack != null) {

            if (!isplaying) {
                //Log.v(getClass().getName(), "audio  — play");
                audiotrack.play();
                isplaying = true;
            }
            //Log.v(getClass().getName(), "audio  — write");
            audiotrack.write(data, 0, data.length);
        }
    }

    public void stop() {

        //Log.i(getClass().getName(), "audio  — stop");
        if (audiotrack.getState() == AudioTrack.STATE_INITIALIZED
                && audiotrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audiotrack.stop();
            isplaying = false;
        }
    }

    public void clearAudio() {

        //Log.i(getClass().getName(), "audio  — release");

        if (audiotrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audiotrack.release();
        }
    }

    public void generateSound(int numsamples, int duration) {

        numsamples *= duration;
        double[] sample = new double[numsamples];
        byte[] generatedSnd = new byte[2 * numsamples];

        for (int i = 0; i < numsamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (audiotrack.getPlaybackRate() / frequency));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }

        samples.add(new KrakenSample(generatedSnd, duration));
        Log.i(getClass().getName(), "audio  — sound generated");
    }

    public void playGeneratedSound(UDPSender sender, boolean broadcast) {

        int t = 0;
        ArrayList<KrakenSample> l = samples;
        Log.i(getClass().getName(), "audio  — play generated sound");
        for (KrakenSample ks : l) {

            /*if (t > 0) {
                try {
                    Thread.sleep(ks.getDuration() * SECOND);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/

            streamData(ks.getData());
            t = ks.getDuration();

            if (broadcast)
                sender.putData(ks.getData());

        }
        Log.i(getClass().getName(), "audio  — play OK");
    }
}
