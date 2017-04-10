package com.pl.multicast.kraken;

import android.os.AsyncTask;
import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;


/**
 * This class is reponsible of sending message using UDP
 */
public class UDPSender {

    // TODO: 06/02/2017 fix the issue with the handler (minor bug)

    //public static final int OBS = 42;
    private static final int DATAPCK_SIZE = 32;
    //private static Handler bshandler;
    private int j;
    private byte[] b;
    private int select;
    private DatagramSocket broadcastsock;
    private BroadcastData std;

    public UDPSender(BroadcastData s) {

        std = s;
        broadcastsock = null;
        j = 0;
        /*bshandler = new Handler() {

            public void handleMessage(Message msg) {

                Log.i(this.getClass().getName(), "msg - what: " + msg.what);
                Log.i(this.getClass().getName(), "msg - obj: " + (msg.obj != null ? msg.obj.getClass().getName() : "NULL"));

                if (msg.what == KrakenMisc.TXT_ID && msg.obj != null) {

                    Log.i(this.getClass().getName(), "msg - OK");

                    try {
                        final String text = (String) msg.obj;
                        Log.i(this.getClass().getName(), "broadcast");
                        sendText(text);
                    } catch (ClassCastException ce) {
                        Log.e(this.getClass().getName(), "msg - cannot get the text: " + ce.getMessage());
                    }
                }
            }
        };*/

        try {
            broadcastsock = new DatagramSocket();
        } catch (SocketException e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }

        b = new byte[32];
        select = 0;
        new Random().nextBytes(b);

        for (int i = 0; i < b.length; i++) {
            Log.i(this.getClass().getName(), "byte value — " + b[i]);
        }
    }

    public void close() {

        if (broadcastsock != null)
            broadcastsock.close();
    }

    void send() {

        new AsyncUDPSenderRoutine().execute(toObjects(b));
        Log.v(this.getClass().getName(), "SEND byte array");
    }

    // byte[] to Byte[]
    private Byte[] toObjects(byte[] bytesPrim) {
        Byte[] bytes = new Byte[bytesPrim.length];

        int i = 0;
        for (byte b : bytesPrim) bytes[i++] = b; // Autoboxing

        return bytes;
    }

    private class AsyncUDPSenderRoutine extends AsyncTask<Byte[], Void, Void> {

        public AsyncUDPSenderRoutine() {
            super();
        }

        private byte[] toPrimitives(Byte[] oBytes) {
            byte[] bytes = new byte[oBytes.length];

            for (int i = 0; i < oBytes.length; i++) {
                bytes[i] = oBytes[i];
            }

            return bytes;
        }

        @Override
        protected Void doInBackground(Byte[]... params) {

            if (params == null || params.length < 1) {

                Log.e(this.getClass().getName(), "Cannot send data at all - MUST NEVER HAPPEN");
                return null;
            }

            try {
                DatagramPacket p;
                Byte[] bdata = params[0];
                byte[] data = toPrimitives(bdata);
                ArrayList<DeviceData> listeners = std.getListeners();
                for (DeviceData dev : listeners) {

                    Log.i(this.getClass().getName(), "SEND data — " + params[0] + " — to " + dev.getName() +
                            " " + dev.getAddr() + ":" + dev.getBroadcastPort());
                    Log.i(this.getClass().getName(), "SEND data size — " + data.length);
                    try {
                        p = new DatagramPacket(data, data.length,
                                new InetSocketAddress(dev.getAddr(), dev.getBroadcastPort()));

                        if (broadcastsock != null) {
                            Log.i(this.getClass().getName(), "SEND — done");
                            broadcastsock.send(p);
                        }

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

            return null;
        }
    }
}
