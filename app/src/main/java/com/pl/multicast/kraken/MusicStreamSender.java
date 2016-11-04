package com.pl.multicast.kraken;

import android.net.wifi.p2p.WifiP2pManager;

/**
 * Created by Luxon on 04/11/2016.
 * <p/>
 * Linked with the list of senders
 *
 * @// TODO: 04/11/2016 Establish the research to find any device
 * @// TODO: 04/11/2016 For each found device update every observers (this assume that the observers have been added)
 */
public class MusicStreamSender extends MusicStream {

    private WifiBroadcast wifi;


    public MusicStreamSender(NavDrawer nav, WifiP2pManager wp2p, WifiP2pManager.Channel ch) {

        super(nav, wp2p, ch);
    }
}
