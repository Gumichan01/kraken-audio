package com.pl.multicast.kraken;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Luxon on 04/11/2016.
 */
public class WifiBroadcast extends BroadcastReceiver implements WifiP2pManager.ConnectionInfoListener {

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

                if (peers.isEmpty())
                    Log.i(TAG, "No peers");
                else
                    Log.i(TAG, "You have " + peers.size() + " peers");

            }
        };

        p2p.discoverPeers(chan, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "discoverPeers() → SUCCESS");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.i(TAG, "discoverPeers() → FAILURE");
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

            if (p2p != null)
                p2p.requestPeers(chan, plisten);

            Log.i(TAG, "Peer list changed");


        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            Log.i(TAG, "Connection state changed");

            if(p2p != null){

                NetworkInfo netInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(netInfo.isConnected()){

                    Log.i(TAG, "CONNECTED");
                    p2p.requestConnectionInfo(chan,this);
                }
                else {
                    Log.i(TAG, "NOT CONNECTED");
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            Log.i(TAG, "Device");
            WifiP2pDevice wd = (WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (wd != null)
                Log.i(TAG, wd.deviceName + " " + wd.deviceAddress);
        }

    }

    // Connection to a device
    public void connect() {

        // for this example, we just test the connection with the first device
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);

        WifiP2pConfig p2pconfig = new WifiP2pConfig();
        p2pconfig.deviceAddress = device.deviceAddress;
        p2pconfig.wps.setup = WpsInfo.PBC;

        p2p.connect(chan, p2pconfig, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "connect() → SUCCESS");
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG, "connect() → FAILURE");
                Toast.makeText(graph, "Connection failed. Retry", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onConnectionInfoAvailable(final WifiP2pInfo info){

        //InetAddress groupOwnerAddress = info.groupOwnerAddress;

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {

            /// Serveur
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.

        } else if (info.groupFormed) {

            /// Client
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
        }


    }
}
