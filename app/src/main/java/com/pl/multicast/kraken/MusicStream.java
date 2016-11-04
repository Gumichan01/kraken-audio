package com.pl.multicast.kraken;

import java.util.Observable;

/**
 * Created by Luxon on 04/11/2016.
 */
public class MusicStream extends Observable {

    protected WifiBroadcast wifi;

    MusicStream(NavDrawer nav){
        super();
        addObserver(nav);

        wifi = new WifiBroadcast();
    }

    public WifiBroadcast getWiFi()
    {
        return wifi;
    }
}
