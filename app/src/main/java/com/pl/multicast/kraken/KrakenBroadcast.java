package com.pl.multicast.kraken;

import android.util.Log;

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
    private boolean broad_opt;
    private boolean listen_opt;

    public KrakenBroadcast(GraphActivity g, BroadcastData bd) {

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

    public void setAudioConfig(int samplerate, boolean stereo, int duration) {

        Log.i(getClass().getName(), "kraken audio  — rate/stereo/duration: " + samplerate + "/" + stereo + "/" + duration);
        audio.configure(samplerate, stereo, duration);
    }

    public void putInCacheMemory(byte[] arr, int len) {

        // write into the cache memory
        kbuffer.write(arr, len);

        if (kbuffer.isFull()) {

            byte[] by = kbuffer.readAll();

            if (listen_opt)
                audio.streamData(by);

            if (broad_opt)
                sender.putData(by);
        }
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
}
