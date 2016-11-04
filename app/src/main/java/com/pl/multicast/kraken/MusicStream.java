package com.pl.multicast.kraken;

import android.net.wifi.p2p.WifiP2pManager;

import java.util.Observable;

/**
 * Created by Luxon on 04/11/2016.
 */
public class MusicStream extends Observable {

    protected WifiBroadcast wifi;

    MusicStream(NavDrawer nav, WifiP2pManager wp2p, WifiP2pManager.Channel ch) {
        super();
        addObserver(nav);

        wifi = new WifiBroadcast(wp2p, ch);
    }

    public WifiBroadcast getWiFi() {
        return wifi;
    }
}
