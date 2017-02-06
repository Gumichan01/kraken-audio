package com.pl.multicast.kraken;

import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;

import java.util.ArrayList;



/**
 * Created by Luxon on 19/01/2017.
 */
public class BroadcastData {

    private volatile boolean running; // TODO: 06/02/2017 Remove this variable
    private volatile String text;
    private volatile boolean send_text;
    private volatile ArrayList<DeviceData> senders;
    private volatile ArrayList<DeviceData> listeners;

    public BroadcastData() {

        text = "TEST";
        running = true;
        send_text = false;
        senders = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public synchronized boolean getRun() {

        return running;
    }

    public synchronized void setRun(boolean r) {

        Log.i("GROUP", "Sync - running");
        running = r;
    }

    public synchronized String getText() {

        return text;
    }

    public synchronized void setText(String t) {

        Log.i("GROUP", "Sync - set text to send");
        text = t;
    }

    public synchronized ArrayList<DeviceData> getSenders() {

        return senders;
    }

    public synchronized void addSender(DeviceData sender) {

        Log.i("GROUP", "Sync - added the sender: " + sender.toString());
        senders.add(sender);
    }

    public synchronized void rmSender(DeviceData sender) {

        Log.i("GROUP", "Sync - remove the sender: " + sender.toString());
        senders.remove(sender);
    }

    public synchronized ArrayList<DeviceData> getListeners() {

        return listeners;
    }

    public synchronized void addListener(DeviceData listener) {

        Log.i("GROUP", "Sync - added the listener: " + listener.toString());
        listeners.add(listener);
    }

    public synchronized void rmListener(DeviceData listener) {

        Log.i("GROUP", "Sync - remove the listener: " + listener.toString());
        listeners.remove(listener);
    }

    public synchronized void stopServer() {

        Log.i("GROUP", "Sync - SHUT down the server");
        running = false;
    }


}
