package com.pl.multicast.kraken;

import java.util.Observable;

/**
 * Created by Luxon on 04/11/2016.
 */
public class MusicStream extends Observable {

    protected WifiBroadcast wifi;
    protected GraphActivity graph;

    MusicStream(NavDrawer nav, GraphActivity ga){
        super();
        addObserver(nav);

        wifi = new WifiBroadcast(ga);
    }

    public WifiBroadcast getWiFi()
    {
        return wifi;
    }
}
