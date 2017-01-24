package com.pl.multicast.kraken;

import datum.DeviceData;

/**
 * Created by kenny on 24/01/17.
 */
public class UDPReceiver {
    private Thread receiver;
    private BroadcastData std;
    private boolean launched;

    public UDPReceiver(BroadcastData b){
        std = b;
        launched = false;
    }

    public void LaunchedReceiver(DeviceData d){

    }

    public void SendMessage(String s){
    }
}
