package com.pl.multicast.kraken;

import android.bluetooth.BluetoothClass;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

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

    public void launchedReceiver(){

    }

    public void sendMessage(DeviceData d, String str){
        char[] buf = new char[1024];
        try {
            Log.i("Group","connection to " + d.getAddr() + ":" + d.getPort());
            Socket s = new Socket(d.getAddr(), d.getPort());

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            writer.write(str);
            writer.flush();

            String rstring = reader.readLine();
            Log.i("Group","msg: " + rstring);
            //if(rstring.equals(BroadcastService.ACK))

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
