package com.pl.multicast.kraken;

import android.util.Log;

/**
 * KrakenBroadcast handles the broadcast part of the application, that is to say:
 *
 *  - receiving data
 *  - sending data
 *  - forwarding data
 *  - caching received data
 *  - handling the audio player
 *
 */
public class KrakenBroadcast {

    private UDPSender sender;
    private UDPReceiver receiver;
    private KrakenAudio audio;
    private KrakenCache kbuffer;

    public KrakenBroadcast(GraphActivity g, BroadcastData bd) {

        sender = new UDPSender(bd);
        receiver = new UDPReceiver(g, bd, this);
        audio = new KrakenAudio();
        kbuffer = new KrakenCache();
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
            Log.i(getClass().getName(), "kraken broadcast  — cache");
            audio.streamData(by);
            /// TODO forward to UDPSender
            sender.putData(by);
        }
    }


    public UDPReceiver getReceiver() {
        return receiver;
    }
}
