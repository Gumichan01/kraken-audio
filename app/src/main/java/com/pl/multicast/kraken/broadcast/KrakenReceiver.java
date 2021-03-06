package com.pl.multicast.kraken.broadcast;

import android.os.AsyncTask;
import android.util.Log;

import com.pl.multicast.kraken.MixActivity;
import com.pl.multicast.kraken.common.KrakenMisc;
import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.parser.MessageParser;
import com.pl.multicast.kraken.service.KrakenBroadcastData;
import com.pl.multicast.kraken.service.KrakenService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;


/**
 * Created by kenny on 24/01/17.
 */
public class KrakenReceiver {

    private static final int DATAPCK_SIZE = 1024;
    private static final int RECV_TIMEOUT = 1000;
    private static final int TIME_UPDATE = 1000;

    private KrakenBroadcastData std;
    private MixActivity graph;
    private KrakenBroadcast kbroad;
    private boolean launched;
    private Thread thread;

    public KrakenReceiver(MixActivity g, KrakenBroadcastData b, KrakenBroadcast k) {

        std = b;
        graph = g;
        kbroad = k;
        thread = null;
        launched = false;
    }

    public void launch() {

        if (launched)
            return;

        launched = true;

        thread = new Thread(new Runnable() {

            private volatile long nbytes;
            private long t;

            private void rate() {
                if ((System.currentTimeMillis() - t) > TIME_UPDATE) {

                    graph.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            graph.displayRate(nbytes);
                        }
                    });
                    // Log.i(getClass().getName(), nbytes + " bytes/s");
                    t = System.currentTimeMillis();
                    nbytes = 0;
                }
            }

            @Override
            public void run() {
                DatagramSocket udpsock = null;
                // Log.i(this.getClass().getName(), "UDP receiver - launch");

                try {
                    udpsock = new DatagramSocket(KrakenMisc.BROADCAST_PORT);
                    udpsock.setSoTimeout(RECV_TIMEOUT);

                    byte[] data = new byte[DATAPCK_SIZE];
                    DatagramPacket p = new DatagramPacket(data, data.length);
                    t = System.currentTimeMillis();

                    while (true) {

                        if (udpsock == null) {
                            break;
                        }

                        for (int i = 0; i < data.length; i++) {
                            data[i] = ' ';
                        }

                        try {
                            udpsock.receive(p);

                            // Log.i(this.getClass().getName(), "UDP receiver - (packet) length: " + p.getLength());
                            byte[] b = p.getData();

                            if (b == null)
                                Log.v(this.getClass().getName(), "UDP receiver - null bytes");
                            else {

                                nbytes += p.getLength();
                                rate();
                                kbroad.putInCacheMemory(p.getData(), p.getLength());
                            }

                        } catch (IOException e) {
                            Log.v(this.getClass().getName(), "UDP receiver - " + e.getMessage());
                        }

                        if (Thread.currentThread().isInterrupted()) {

                            Log.e(this.getClass().getName(), "UDP receiver - Interrupted");
                            udpsock.close();
                            break;
                        }
                    }

                    // Log.i(this.getClass().getName(), "UDP receiver - End of thread");

                } catch (SocketException e) {
                    // Log.i(this.getClass().getName(), "UDP receiver - No UDP socket created");
                    Log.e(this.getClass().getName(), "UDP receiver - " + e.getMessage());
                } finally {

                    if (udpsock != null)
                        udpsock.close();
                }
            }
        });
        thread.start();
    }

    public void stop() {

        thread.interrupt();
    }

    public void listenRequest(final DeviceData d, final String usrname) {
        // Log.i(this.getClass().getName(), "UDP receiver - listen request");
        // Log.i(this.getClass().getName(), "UDP receiver - " + d.toString());
        sendMessageRequest(d, KrakenService.LISTEN + " " + usrname + MessageParser.EOL);
    }

    public void stopRequest(final DeviceData d, final String usrname) {

        // Log.i(this.getClass().getName(), "UDP receiver - stop request");
        sendMessageRequest(d, KrakenService.STOP + " " + usrname + MessageParser.EOL);
    }


    private void sendMessageRequest(final DeviceData dev, final String req) {
        new ASyncUDPReceiveRequest(dev, req).execute();
    }


    // Thread that sends requests for receiving messages
    private class ASyncUDPReceiveRequest extends AsyncTask<Void, Void, Boolean> {

        private DeviceData dev;
        private String request;

        public ASyncUDPReceiveRequest(DeviceData d, String req) {

            super();
            dev = d;
            request = req;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Log.i(this.getClass().getName(), "UDP receiver - connection to " + dev.getName());
                // Log.i(this.getClass().getName(), "UDP receiver - information - " + dev.getAddr() + ":" + dev.getPort());
                Socket s = new Socket(dev.getAddr(), dev.getPort());
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                writer.write(request);
                writer.flush();

                String rstring = reader.readLine();
                //Log.i(this.getClass().getName(), "UDP receiver - msg: " + rstring);
                s.close();
                return rstring.contains(KrakenService.ACK);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean result) {

            if (result) {
                // Log.i(this.getClass().getName(), "post execute - " + request + " - SUCCESS");

                if (request.contains(KrakenService.LISTEN)) {
                    KrakenReceiver.this.std.addRealBroadcaster(dev.getName());
                    updateGraphActivity();

                } else if (request.contains(KrakenService.STOP)) {
                    KrakenReceiver.this.std.rmRealBroadcaster(dev.getName());
                    updateGraphActivity();
                }

            }
        }

        private void updateGraphActivity() {

            graph.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    graph.update(false);
                }
            });
        }
    }
}
