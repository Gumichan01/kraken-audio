package com.pl.multicast.kraken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by Luxon on 04/11/2016.
 */
public class WifiBroadcast extends BroadcastReceiver{

    private WifiP2pManager p2p;
    private WifiP2pManager.Channel chan;

    public WifiBroadcast(WifiP2pManager wp2p, WifiP2pManager.Channel ch){
        super();

        p2p = wp2p;
        chan = ch;
    }

    @Override
    public void onReceive(Context context, Intent intent){

        //String action = intent.getAction();
    }

}
