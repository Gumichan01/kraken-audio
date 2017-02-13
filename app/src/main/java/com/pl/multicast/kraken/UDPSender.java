package com.pl.multicast.kraken;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.pl.multicast.kraken.common.KrakenMisc;
import com.pl.multicast.kraken.datum.DeviceData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;


/**
 * This class is reponsible of sending message using UDP
 */
public class UDPSender {

    // TODO: 06/02/2017 fix the issue with the handler (minor bug)

    public static final int OBS = 42;
    private static Handler bshandler;
    DatagramSocket broadcastsock;
    private BroadcastData std;

    public UDPSender(BroadcastData s) {

        std = s;
        broadcastsock = null;
        bshandler = new Handler() {

            public void handleMessage(Message msg) {

                Log.i(this.getClass().getName(), "msg - what: " + msg.what);
                Log.i(this.getClass().getName(), "msg - obj: " + (msg.obj != null ? msg.obj.getClass().getName() : "NULL"));

                if (msg.what == KrakenMisc.TXT_ID && msg.obj != null) {

                    Log.i(this.getClass().getName(), "msg - OK");

                    try {
                        final String text = (String) msg.obj;
                        Log.i(this.getClass().getName(), "broadcast");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendText(text);
                            }
                        }).start();


                    } catch (ClassCastException ce) {
                        Log.e(this.getClass().getName(), "msg - cannot get the text: " + ce.getMessage());
                    }
                }
            }
        };

        try {
            broadcastsock = new DatagramSocket();
        } catch (SocketException e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    public Handler getHandler() {

        return bshandler;
    }

    // TODO: 13/02/2017 Remove the thread creation in sendText
    private void sendText(final String text) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramPacket p;
                    byte[] data = text.getBytes();
                    ArrayList<DeviceData> listeners = std.getListeners();

                    for (DeviceData dev : listeners) {

                        Log.i(this.getClass().getName(), "SEND data to " + dev.getName());
                        try {
                            p = new DatagramPacket(data, data.length,
                                    new InetSocketAddress(dev.getAddr(), dev.getBroadcastPort()));

                            if (broadcastsock != null)
                                broadcastsock.send(p);

                        } catch (IOException e) {
                            Log.e(this.getClass().getName(), e.getMessage());
                        }
                    }
                    Log.i(this.getClass().getName(), "DONE");

                } catch (SecurityException | NullPointerException se) {

                    se.printStackTrace();

                } catch (Exception u) {

                    u.printStackTrace();
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
