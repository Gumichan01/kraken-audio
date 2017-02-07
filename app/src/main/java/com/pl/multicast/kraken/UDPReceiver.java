package com.pl.multicast.kraken;

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

    private BroadcastData std;
    private Thread receiver;
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
            @Override
            public void run() {
                DatagramSocket udpsock = null;

                try {
                    udpsock = new DatagramSocket(KrakenMisc.BROADCAST_PORT);
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

                        if(Thread.currentThread().isInterrupted()){

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

    public void listenRequest(final DeviceData d, final String usrname){
        Log.i(this.getClass().getName(), "UDP receiver - listen request");
        Log.i(this.getClass().getName(), "UDP receiver - " + d.toString());
        sendMessageRequest(d, BroadcastService.LISTEN + " " + usrname + MessageParser.EOL);
    }

    public void stopRequest(final DeviceData d, final String usrname){

        Log.i(this.getClass().getName(), "UDP receiver - stop request");
        sendMessageRequest(d, BroadcastService.STOP_CMD + " " +  usrname + MessageParser.EOL);
    }

    private void sendMessageRequest(final DeviceData d, final String str) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                String rstring = "";

                try {
                    Log.i(this.getClass().getName(), "UDP receiver - connection to " + d.getAddr() + ":" + d.getPort());
                    Socket s = new Socket(d.getAddr(), d.getPort());

                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    writer.write(str);
                    writer.flush();

                    rstring = reader.readLine();
                    Log.i(this.getClass().getName(), "UDP receiver - msg: " + rstring);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
