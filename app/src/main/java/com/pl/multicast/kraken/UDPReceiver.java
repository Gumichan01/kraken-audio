package com.pl.multicast.kraken;

import android.util.Log;

import com.pl.multicast.kraken.common.KrakenMisc;
import com.pl.multicast.kraken.datum.DeviceData;

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

    private BroadcastData std;
    private Thread receiver;
    private GraphActivity graph;
    private boolean launched;

    public UDPReceiver(GraphActivity g, BroadcastData b) {
        std = b;
        launched = false;
        graph = g;
    }

    public void launchReceiver() {

        if (launched)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket udpsock = null;

                try {
                    udpsock = new DatagramSocket(KrakenMisc.BROADCAST_PORT);
                    byte[] data = new byte[DATAPCK_SIZE];
                    DatagramPacket p = new DatagramPacket(data, data.length);

                    while (true) {

                        // TODO: 07/02/2017 Check if the thread where the code is executed has been interrupted
                        if (udpsock == null) {
                            break;
                        }

                        for (int i = 0; i < data.length; i++) {
                            data[i] = '#';
                        }

                        try {
                            udpsock.receive(p);
                            final String rstring = new String(p.getData());
                            Log.i(this.getClass().getName(), "UDP receiver - " + rstring);

                            graph.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    graph.receiveText(rstring);
                                }
                            });

                        } catch (IOException e) {
                            Log.e(this.getClass().getName(), "UDP receiver - No UDP socket created");
                        }
                    }

                } catch (SocketException e) {
                    Log.i(this.getClass().getName(), "UDP receiver - No UDP socket created");
                    Log.e(this.getClass().getName(), "UDP receiver - " + e.getMessage());
                } finally {

                    if (udpsock != null)
                        udpsock.close();
                }
            }
        }).start();
    }

    public void sendMessage(final DeviceData d, final String str) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(this.getClass().getName(), "UDP receiver - connection to " + d.getAddr() + ":" + d.getPort());
                    Socket s = new Socket(d.getAddr(), d.getPort());

                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    writer.write(str);
                    writer.flush();

                    String rstring = reader.readLine();
                    Log.i(this.getClass().getName(), "UDP receiver - msg: " + rstring);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
