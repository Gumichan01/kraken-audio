package com.pl.multicast.kraken;

import android.util.Log;

/**
 * Created by Luxon on 08/05/2017.
 */
public class KrakenBroadcast {

    private UDPSender sender;
    private UDPReceiver receiver;
    private KrakenAudio audio;
    private KrakenCache kbuffer;

    public KrakenBroadcast(GraphActivity g, BroadcastData bd) {

        sender = new UDPSender(bd);
        receiver = new UDPReceiver(g,bd);
        audio = new KrakenAudio();
        kbuffer = new KrakenCache();
    }

    public void launch() {

        // launch sender
        new Thread(new Runnable() {
            @Override
            public void run() {
                sender.send();
            }
        }).start();

        // launch receiver
        receiver.launch();
    }

    public void stop() {

        Log.i(getClass().getName(), "kraken broadcast — stop");
        sender.close();
        receiver.stop();
    }

    public UDPReceiver getReceiver() {
        return receiver;
    }
}
