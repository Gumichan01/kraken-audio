package com.pl.multicast.kraken;

import android.os.AsyncTask;
import android.util.Log;

import com.pl.multicast.kraken.common.KrakenMisc;
import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.parser.MessageParser;

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
public class UDPReceiver {

    private static final int DATAPCK_SIZE = 32;
    private static final int RECV_TIMEOUT = 2000;

    private BroadcastData std;
    private GraphActivity graph;
    private boolean launched;
    private Thread thread;

    public UDPReceiver(GraphActivity g, BroadcastData b) {

        std = b;
        launched = false;
        graph = g;
        thread = null;
    }

    public void launch() {

        if (launched)
            return;

        launched = true;

        thread = new Thread(new Runnable() {

            private void printByte(final byte[] bytes){

                graph.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //graph.receiveText(rstring);
                        graph.receiveByte(bytes[0]);
                    }
                });
            }

            @Override
            public void run() {
                DatagramSocket udpsock = null;

                try {
                    udpsock = new DatagramSocket(KrakenMisc.BROADCAST_PORT);
                    udpsock.setSoTimeout(RECV_TIMEOUT);

                    byte[] data = new byte[DATAPCK_SIZE];
                    DatagramPacket p = new DatagramPacket(data, data.length);

                    while (true) {

                        if (udpsock == null) {
                            break;
                        }

                        for (int i = 0; i < data.length; i++) {
                            data[i] = ' ';
                        }

                        try {
                            udpsock.receive(p);

                            Log.i(this.getClass().getName(), "UDP receiver - (packet) length: " + p.getLength());
                            byte[] b = p.getData();

                            if (b == null)
                                Log.i(this.getClass().getName(), "UDP receiver - null bytes");
                            else {

                                Log.i(this.getClass().getName(), "UDP receiver - bytes length: " + b.length);
                                Log.i(this.getClass().getName(), "UDP receiver - content");
                                for (byte by : b)
                                    Log.i(this.getClass().getName(), "UDP receiver - " + by);

                                Log.i(this.getClass().getName(), "UDP receiver - END content");
                                printByte(b);
                            }

                        } catch (IOException e) {
                            Log.v(this.getClass().getName(), "UDP receiver - " + e.getMessage());
                        }

                        if (Thread.currentThread().isInterrupted()) {

                            Log.i(this.getClass().getName(), "UDP receiver - Interrupted");
                            udpsock.close();
                            break;
                        }
                    }

                    Log.i(this.getClass().getName(), "UDP receiver - End of thread");

                } catch (SocketException e) {
                    Log.i(this.getClass().getName(), "UDP receiver - No UDP socket created");
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
        Log.i(this.getClass().getName(), "UDP receiver - listen request");
        Log.i(this.getClass().getName(), "UDP receiver - " + d.toString());
        sendMessageRequest(d, BroadcastService.LISTEN + " " + usrname + MessageParser.EOL);
    }

    public void stopRequest(final DeviceData d, final String usrname) {

        Log.i(this.getClass().getName(), "UDP receiver - stop request");
        sendMessageRequest(d, BroadcastService.STOP + " " + usrname + MessageParser.EOL);
    }


    private void sendMessageRequest(final DeviceData dev, final String req) {
        new ASyncUDPReceiveRequest(dev, req).execute();
    }


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
                Log.i(this.getClass().getName(), "UDP receiver - connection to " + dev.getAddr() + ":" + dev.getPort());
                Socket s = new Socket(dev.getAddr(), dev.getPort());

                PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                writer.write(request);
                writer.flush();

                String rstring = reader.readLine();
                Log.i(this.getClass().getName(), "UDP receiver - msg: " + rstring);
                s.close();
                return rstring.contains(BroadcastService.ACK);

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public void onPostExecute(Boolean result) {

            if (result) {
                Log.i(this.getClass().getName(), "post execute - " + request + " - SUCCESS");

                if (request.contains(BroadcastService.LISTEN)) {
                    UDPReceiver.this.std.addRealBroadcaster(dev.getName());
                    updateGraphActivity();

                } else if (request.contains(BroadcastService.STOP)) {
                    UDPReceiver.this.std.rmRealBroadcaster(dev.getName());
                    updateGraphActivity();
                }

            } else
                Log.e(this.getClass().getName(), "post execute - " + request + " - FAILURE");
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
