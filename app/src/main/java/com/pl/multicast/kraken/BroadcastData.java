package com.pl.multicast.kraken;

import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;

import java.util.ArrayList;


/**
 * Class that contains information about
 */
public class BroadcastData {

    private volatile ArrayList<DeviceData> broadcasters;
    private volatile ArrayList<DeviceData> listeners;

    public BroadcastData() {

        broadcasters = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public synchronized ArrayList<DeviceData> getSenders() {

        return broadcasters;
    }

    public synchronized void addBroadcaster(DeviceData sender) {

        Log.i(this.getClass().getName(), "Sync - added the sender: " + sender.toString());
        broadcasters.add(sender);
    }

    public synchronized void rmBroadcaster(DeviceData sender) {

        Log.i(this.getClass().getName(), "Sync - remove the sender: " + sender.toString());
        broadcasters.remove(sender);
    }

    public synchronized void clearBroadcasters() {

        Log.i(this.getClass().getName(), "Sync - clear broadcasters");
        broadcasters.clear();
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

    public synchronized DeviceData getBroadcasterOf(String s) {

        for (DeviceData d : broadcasters) {

            if (d.getName().equals(s))
                return d;
        }

        return null;
    }
}
