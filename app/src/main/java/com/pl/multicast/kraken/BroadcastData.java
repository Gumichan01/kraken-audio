package com.pl.multicast.kraken;

import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;

import java.util.ArrayList;


/**
 * Class that contains information about
 */
public class BroadcastData {

    // TODO: 06/02/2017 Refactorize this class (kenny)

    @Deprecated
    private volatile boolean running;
    @Deprecated
    private volatile String text;
    @Deprecated
    private volatile boolean send_text;
    private volatile ArrayList<DeviceData> senders;
    private volatile ArrayList<DeviceData> listeners;

    public BroadcastData() {

        text = "#";
        running = true;
        send_text = false;
        senders = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    @Deprecated
    public synchronized String getText() {

        return text;
    }

    @Deprecated
    public synchronized void setText(String t) {

        Log.i(this.getClass().getName(), "Sync - set text to send");
        text = t;
    }

    public synchronized ArrayList<DeviceData> getSenders() {

        return senders;
    }

    public synchronized void addSender(DeviceData sender) {

        Log.i(this.getClass().getName(), "Sync - added the sender: " + sender.toString());
        senders.add(sender);
    }

    public synchronized void rmSender(DeviceData sender) {

        Log.i(this.getClass().getName(), "Sync - remove the sender: " + sender.toString());
        senders.remove(sender);
    }

    public synchronized void clearSenders() {

        Log.i(this.getClass().getName(), "Sync - clear senders");
        senders.clear();
    }

    public synchronized void clearListeners() {

        Log.i(this.getClass().getName(), "Sync - clear listeners");
        listeners.clear();
    }

    public synchronized ArrayList<DeviceData> getListeners() {

        return listeners;
    }

    public synchronized void addListener(DeviceData listener) {

        Log.i(this.getClass().getName(), "Sync - added the listener: " + listener.toString());
        listeners.add(listener);
    }

    public synchronized void rmListener(DeviceData listener) {

        Log.i(this.getClass().getName(), "Sync - remove the listener: " + listener.toString());
        listeners.remove(listener);
    }

    public synchronized DeviceData getSenderOf(String s) {

        for (DeviceData d : senders) {

            if (d.getName().equals(s))
                return d;
        }

        return null;
    }

    @Deprecated
    public synchronized void stopServer() {

        Log.i(this.getClass().getName(), "Sync - SHUT down the server");
        running = false;
    }


}
