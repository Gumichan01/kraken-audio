package com.pl.multicast.kraken;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Luxon on 18/01/2017.
 */
public class ServerThread extends Thread {

    private static final int SVTPORT = 2409;
    private static String svthost = "";
    private boolean running;
    private String text;
    private boolean send_text;

    public ServerThread() {

        super();
        svthost = clt.ClientDevice.SVHOST;
        running = true;
        send_text = true;
    }

    public void run() {

        boolean go;
        boolean tosend;

        try {
            ServerSocket srvsock = new ServerSocket(SVTPORT);
            Log.i("GROUP", "Server @" + srvsock.getInetAddress().toString() + " " + srvsock.getLocalPort());
            srvsock.setSoTimeout(16000);

            synchronized (this) {
                go = running;
            }

            while (go) {

                Log.i("GROUP", "Server is waiting for new connections" );
                Socket sock = srvsock.accept();

                if (sock == null) {

                    Log.e("GROUP", "null socket");
                    go = false;
                    srvsock.close();
                    continue;
                }

                Log.i("GROUP", "Server received connection from:\n" + sock.getInetAddress().toString() + " " + sock.getLocalPort());

                synchronized (this) {
                    tosend = send_text;
                }

                if (tosend) {

                    // envoi text
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                    Log.i("GROUP", "Server is sending a message");
                    writer.write("Hello world !");
                    writer.flush();
                    Log.i("GROUP", "DONE");
                    send_text = false;
                }

                sock.close();

                synchronized (this) {
                    go = running;
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

    public synchronized void stopServer() {

        Log.i("GROUP","SHUT down the server");
        running = false;
    }

    public synchronized void sendText(String s) {

        text = s;
        send_text = true;
    }

}
