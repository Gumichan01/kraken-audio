package com.pl.multicast.kraken;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by Luxon on 18/01/2017.
 */
public class ServerThread extends Thread {

    private static final int SVTPORT = 2409;
    private static String svthost = "";
    private boolean running;
    private String text;
    private String ptext;
    private ServerThreadData std;

    public ServerThread(ServerThreadData s) {

        super();
        std = s;
        svthost = clt.ClientDevice.SVHOST;

    }

    public void run() {

        boolean go = true;
        String ptext = "";
        boolean tosend;

        try {
            DatagramSocket srvsock = new DatagramSocket();
            Log.i("GROUP", "Server @" + InetAddress.getLocalHost().toString());

            while (go) {

                byte [] data;
                DatagramPacket p;

                if (go == false) {

                    Log.e("GROUP", "shut the server down");
                    srvsock.close();
                    break;
                }

                text = std.getText();
                data = text.getBytes();

                if(!text.equals(ptext)){

                    Log.i("GROUP", "SEND");
                    try {
                        p = new DatagramPacket(data, data.length,
                                new InetSocketAddress("192.168.1.22", 2409));
                        srvsock.send(p);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ptext = text;
                    Log.i("GROUP", "DONE");
                }

                go = std.getRun();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }

        } catch (IOException e) {

            e.printStackTrace();

        } catch (SecurityException | NullPointerException se) {

            se.printStackTrace();

        } catch (Exception u) {

            u.printStackTrace();
        }
    }
}
