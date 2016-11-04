package com.pl.multicast.kraken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;

/**
 * Created by Luxon on 04/11/2016.
 */
public class WifiBroadcast extends BroadcastReceiver{

    private WifiP2pManager p2p;
    private WifiP2pManager.Channel chan;
    private GraphActivity graph;

    public WifiBroadcast(GraphActivity ga){
        super();
        p2p = (WifiP2pManager) graph.getSystemService(Context.WIFI_P2P_SERVICE);
        chan = p2p.initialize(graph, graph.getMainLooper(), null);
    }

    @Override
    public void onReceive(Context context, Intent intent){

    }

}
