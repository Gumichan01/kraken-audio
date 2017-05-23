package com.pl.multicast.kraken.broadcast;

import android.util.Log;

import com.pl.multicast.kraken.MixActivity;
import com.pl.multicast.kraken.audio.KrakenAudio;
import com.pl.multicast.kraken.service.BroadcastData;

/**
 * KrakenBroadcast handles the broadcast part of the application, that is to say:
 * <p/>
 * - receiving data
 * - sending data
 * - forwarding data
 * - caching received data
 * - handling the audio player
 */
public class KrakenBroadcast {

    private UDPSender sender;
    private UDPReceiver receiver;
    private KrakenAudio audio;
    private KrakenCache kbuffer;
    private volatile boolean broad_opt;
    private volatile boolean listen_opt;

    public KrakenBroadcast(MixActivity g, BroadcastData bd) {

        sender = new UDPSender(bd);
        receiver = new UDPReceiver(g, bd, this);
        audio = new KrakenAudio();
        kbuffer = new KrakenCache();
        broad_opt = true;
        listen_opt = true;
    }

    public void launch() {

        // launch receiver
        receiver.launch();
    }

    public void stop() {

        Log.i(getClass().getName(), "kraken broadcast — stop");
        sender.close();
        receiver.stop();
        audio.stop();
        audio.clearAudio();
        kbuffer.clear();
    }

    public void setAudioConfig(int samplerate, boolean stereo) {

        int numsamples = samplerate * (stereo ? 2 : 1);
        audio.configAudioTrack(samplerate, stereo, numsamples);
    }

    public void generateSound(int samplerate, int frequency, boolean stereo, int duration) {

        audio.setFrequency(frequency);
        audio.generateSound(samplerate, stereo, duration);
    }

    /**
     * Put data in cache memory
     */
    public void putInCacheMemory(byte[] arr, int len) {

        // write into the cache memory
        kbuffer.write(arr, len);
        handleCacheMemory();
    }

    /**
     * Handle data from the cache memory
     */
    private void handleCacheMemory() {

        if (kbuffer.isFull()) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    byte[] by = kbuffer.readAll();

                    if (listen_opt)
                        audio.streamData(by);

                    if (broad_opt)
                        sender.putData(by);
                }
            }).start();
        }
    }

    public void playGeneratedSound() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                audio.playGeneratedSound(sender, listen_opt, broad_opt);
            }
        }).start();
    }

    public boolean getBroadcastOption() {
        return broad_opt;
    }

    public void setBroadcastOption(boolean broadcast) {
        broad_opt = broadcast;
    }

    public boolean getListenOption() {
        return listen_opt;
    }

    public void setListenOption(boolean listen) {
        listen_opt = listen;
    }

    public UDPReceiver getReceiver() {
        return receiver;
    }

    public KrakenAudio getAudio() {

        return audio;
    }

}
