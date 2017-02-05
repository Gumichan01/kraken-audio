package com.pl.multicast.kraken;

import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;


/**
 * Created by Luxon on 18/01/2017.
 */
public class UDPSender extends Thread {

    private static final int SVTPORT = 2409;
    private static String svthost = "";
    private boolean running;
    private String text;
    private String ptext;
    private BroadcastData std;

    public UDPSender(BroadcastData s) {

        super();
        std = s;
        svthost = null;//clt.ClientDevice.SVHOST;

    }

    public void run() {

        String ptext = "";
        boolean tosend;

        try {
            DatagramSocket srvsock = new DatagramSocket();
            Log.i("GROUP", "Server @" + InetAddress.getLocalHost().toString());

            while (std.getRun()) {

                byte[] data;
                DatagramPacket p;

                text = std.getText();
                data = text.getBytes();
                ArrayList<DeviceData> listeners = std.getListeners();

                if (!text.equals(ptext)) {

                    for (DeviceData dev : listeners) {

                        Log.i("GROUP", "SEND data to " + dev.getName());
                        try {
                            p = new DatagramPacket(data, data.length,
                                    new InetSocketAddress(dev.getAddr(), dev.getBroadcastPort()));
                            srvsock.send(p);
                        } catch (SocketException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ptext = text;
                        Log.i("GROUP", "DONE");
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            Log.i("GROUP", "shut the server down");

        } catch (IOException e) {

            e.printStackTrace();

        } catch (SecurityException | NullPointerException se) {

            se.printStackTrace();

        } catch (Exception u) {

            u.printStackTrace();
        }
    }
}
