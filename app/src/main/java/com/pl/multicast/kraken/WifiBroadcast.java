package com.pl.multicast.kraken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * Created by Luxon on 04/11/2016.
 */
public class WifiBroadcast extends BroadcastReceiver {

    private WifiP2pManager p2p;
    private WifiP2pManager.Channel chan;

    public WifiBroadcast(WifiP2pManager wp2p, WifiP2pManager.Channel ch) {
        super();

        p2p = wp2p;
        chan = ch;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                Log.i("WIFI-DIRECT_STATUS", "Wi-Fi P2P is activated");
            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {

                Log.i("WIFI-DIRECT_STATUS", "Wi-Fi P2P is not activated");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            Log.i("WIFI-DIRECT_STATUS", "Peer list changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            Log.i("WIFI-DIRECT_STATUS", "Connection state changed");
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            Log.i("WIFI-DIRECT_STATUS", "Connection state changed");
        }

    }

}
