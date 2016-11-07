package com.pl.multicast.kraken;

import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by Luxon on 04/11/2016.
 * <p/>
 * Linked with the list of readers
 *
 * @// TODO: 04/11/2016 Establish the research to find any device
 * @// TODO: 04/11/2016 For each found device update every observers
 * (this assume that the observers have been added)
 */
public class MusicStreamReceiver extends MusicStream {

    public MusicStreamReceiver(NavDrawer nav, WifiBroadcast wb) {

        super(nav, wb);
    }

}
