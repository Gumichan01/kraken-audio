package com.pl.multicast.kraken.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.pl.multicast.kraken.broadcast.KrakenSender;

import java.util.ArrayList;

/**
 * KrakenAudio handles audio playing
 */
public class KrakenAudio {

    public static final int DEFAULT_FREQUENCY = 440;
    public static final int DEFAULT_SAMPLERATE = 8000;
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

    public void generateSound(int samplerate, boolean stereo, int duration) {

        int numsamples = samplerate * (stereo ? 2 : 1);
        generateSample(numsamples, duration);
    }

    public void configAudioTrack(int samplerate, boolean stereo, int numsamples) {

        if (audiotrack != null) {
            stop();
            clearAudio();
        }

        audiotrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                samplerate, (stereo ? AudioFormat.CHANNEL_OUT_STEREO : AudioFormat.CHANNEL_OUT_MONO),
                AudioFormat.ENCODING_PCM_16BIT, numsamples, AudioTrack.MODE_STREAM);
    }

    public synchronized void streamData(byte[] data) {

        if (audiotrack != null) {

            if (!isplaying) {

                try {

                    audiotrack.play();
                    isplaying = true;

                } catch (Exception e) {
                    Log.wtf(getClass().getName(), "error — " + e.getMessage());
                }

            }
            audiotrack.write(data, 0, data.length);
        }
    }

    public void stop() {

        if (audiotrack.getState() == AudioTrack.STATE_INITIALIZED
                && audiotrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audiotrack.stop();
            isplaying = false;
        }
    }

    public void clearAudio() {

        if (audiotrack.getState() == AudioTrack.STATE_INITIALIZED) {
            audiotrack.release();
        }
    }

    /**
     * Generate a new sample (sound) and store it in the list of samples
     */
    private void generateSample(int numsamples, int duration) {

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

        samples.add(new KrakenSample(generatedSnd, duration, frequency));
        Log.i(getClass().getName(), "audio  — sound generated");
    }

    /**
     * Play all of the samples of the list
     */
    public void playGeneratedSound(KrakenSender sender, boolean listen, boolean broadcast) {

        ArrayList<KrakenSample> l = samples;
        for (KrakenSample ks : l) {

            if (listen)
                streamData(ks.getData());

            if (broadcast)
                sender.putData(ks.getData());
        }
    }


    public ArrayList<KrakenSample> getSamples() {

        return samples;
    }
}
