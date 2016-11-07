package com.pl.multicast.kraken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Luxon on 04/11/2016.
 */
public class WifiBroadcast extends BroadcastReceiver {

    public static final String TAG = "WIFI-DIRECT_STATUS";
    private WifiP2pManager p2p;
    private WifiP2pManager.Channel chan;
    private GraphActivity graph;
    private List peers = new ArrayList();
    private WifiP2pManager.PeerListListener plisten;

    public WifiBroadcast(WifiP2pManager wp2p, WifiP2pManager.Channel ch, GraphActivity g) {
        super();

        p2p = wp2p;
        chan = ch;
        graph = g;
        plisten = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                peers.clear();
                peers.addAll(peerList.getDeviceList());

                if(peers.isEmpty())
                    Log.i(TAG,"No peers");
                else
                    Log.i(TAG,"You have " + peers.size() + " peers");

            }
        };

        p2p.discoverPeers(chan, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "SUCCESS");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.i(TAG, "FAIL");
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                Log.i(TAG, "Wi-Fi P2P is activated");

            } else if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {

                Log.i(TAG, "Wi-Fi P2P is not activated");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            if(p2p != null)
                p2p.requestPeers(chan,plisten);

            Log.i(TAG, "Peer list changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            Log.i(TAG, "Connection state changed");


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            Log.i(TAG, "Device");
            WifiP2pDevice wd = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (wd != null)
                Log.i(TAG, wd.deviceName + " " + wd.deviceAddress);
            /// @// TODO: 07/11/2016 Send the result to the activity (mac address)
        }

    }

}
