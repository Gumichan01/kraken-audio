package com.pl.multicast.kraken;

import android.util.Log;

/**
 * Created by Luxon on 19/01/2017.
 */
public class ServerThreadData {

    private volatile boolean running;
    private volatile String text;
    private volatile boolean send_text;

    public ServerThreadData() {

        text = "TEST";
        running = true;
        send_text = false;
    }

    public synchronized boolean getRun() {

        return running;
    }

    public synchronized void setRun(boolean r) {

        Log.i("GROUP", "Sync - running");
        running  = r;
    }

    public synchronized String getText() {

        return text;
    }

    public synchronized void setText(String t) {

        Log.i("GROUP", "Sync - send text");
        text = t;
    }

    public synchronized void stopServer() {

        Log.i("GROUP", "Sync - SHUT down the server");
        running = false;
    }


}
