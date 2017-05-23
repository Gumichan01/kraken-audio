package com.pl.multicast.kraken.broadcast;

import android.os.AsyncTask;
import android.util.Log;

import com.pl.multicast.kraken.GraphActivity;
import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.service.BroadcastData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * This class is reponsible of sending message using UDP
 */
public class UDPSender {

    private static final int CACHE_SZ = 1024;
    private byte[] b;
    private DatagramSocket broadcastsock;
    private BroadcastData std;

    public UDPSender(BroadcastData s) {

        std = s;
        broadcastsock = null;

        try {
            broadcastsock = new DatagramSocket();
        } catch (SocketException e) {
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    public void close() {

        if (broadcastsock != null)
            broadcastsock.close();
    }

    /**
     * Send data
     */
    public void putData(byte[] data) {
        new AsyncUDPSenderRoutine().execute(toObjects(data));
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

        // Send packet to a specific device per block of 1024 bytes
        private void sendPacket(DeviceData dev, byte[] bytes) throws IOException {

            int i = 0;
            while (i < bytes.length) {

                // TODO remove it?
                try {
                    Thread.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int len = (bytes.length - i) > CACHE_SZ ? CACHE_SZ : bytes.length - i;
                //Log.v(this.getClass().getName(), "SEND data size — " + len);

                byte[] data = Arrays.copyOfRange(bytes, i, i + len);
                DatagramPacket p = new DatagramPacket(data, data.length,
                        new InetSocketAddress(dev.getAddr(), dev.getBroadcastPort()));

                broadcastsock.send(p);
                i += len;
            }
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
                //DatagramPacket p;
                Byte[] bdata = params[0];
                ArrayList<DeviceData> listeners = std.getListeners();
                for (DeviceData dev : listeners) {

                    if (dev.getName().equals(GraphActivity.username))
                        continue;

                    /*Log.v(this.getClass().getName(), "SEND data — " + params[0] + " — to " + dev.getName() +
                            " " + dev.getAddr() + ":" + dev.getBroadcastPort());*/
                    try {
                        sendPacket(dev, toPrimitives(bdata));

                    } catch (IOException e) {
                        Log.e(this.getClass().getName(), e.getMessage());
                    }
                }
                //Log.v(this.getClass().getName(), "DONE");

            } catch (SecurityException | NullPointerException se) {

                se.printStackTrace();

            } catch (Exception u) {

                u.printStackTrace();
            }
            return null;
        }
    }
}
