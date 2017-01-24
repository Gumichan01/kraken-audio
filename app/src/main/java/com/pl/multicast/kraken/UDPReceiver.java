package com.pl.multicast.kraken;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import datum.DeviceData;

/**
 * Created by kenny on 24/01/17.
 */
public class UDPReceiver {

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
        /// @// TODO: 24/01/2017 Receive the stream

        if (launched)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket udpsock = null;

                try {
                    udpsock = new DatagramSocket(2409);
                    byte[] data = new byte[16];
                    DatagramPacket p = new DatagramPacket(data, data.length);

                    while (std.getRun()) {

                        if (udpsock == null) {
                            break;
                        }

                        for (int i = 0; i < data.length; i++) {
                            data[i] = '#';
                        }

                        try {
                            udpsock.receive(p);
                            final String rstring = new String(p.getData());
                            Log.i("GROUP", "UDP receiver - " + rstring);

                            graph.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    graph.receiveText(rstring);
                                }
                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (SocketException e) {
                    Log.i("GROUP", "UDP receiver - No UDP socket created");
                    Log.i("GROUP", "UDP receiver - " + e.getMessage());
                    e.printStackTrace();
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
                    Log.i("GROUP", "UDP receiver - connection to " + d.getAddr() + ":" + d.getPort());
                    Socket s = new Socket(d.getAddr(), d.getPort());

                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    writer.write(str);
                    writer.flush();

                    String rstring = reader.readLine();
                    Log.i("GROUP", "UDP receiver - msg: " + rstring);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
