package com.pl.multicast.kraken;

import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;

import java.util.ArrayList;


/**
 * Class that contains information about
 */
public class BroadcastData {

    private volatile ArrayList<DeviceData> senders;
    private volatile ArrayList<DeviceData> listeners;

    public BroadcastData() {

        senders = new ArrayList<>();
        listeners = new ArrayList<>();
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
}
