package com.pl.multicast.kraken;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
        boolean tosend;
        ArrayList<Socket> lsock = new ArrayList<>();

        try {
            ServerSocket srvsock = new ServerSocket(SVTPORT);
            Log.i("GROUP", "Server @" + srvsock.getInetAddress().toString() + " " + srvsock.getLocalPort());

            while (go) {

                Log.i("GROUP", "Server is waiting for new connections");
                Socket sock = srvsock.accept();
                Log.i("GROUP", "accept");

                if (go == false) {

                    Log.e("GROUP", "shut the server down");
                    srvsock.close();
                    break;
                }

                if (sock != null)
                    lsock.add(sock);

                Log.i("GROUP", "Server received connection from:\n" + sock.getInetAddress().toString() + " " + sock.getLocalPort());

                text = std.getText();

                Log.i("GROUP", "SEND");
                Log.i("GROUP", "lsock size: " + lsock.size());
                // envoi text
                for (int i = 0; i < lsock.size(); i++) {

                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(lsock.get(i).getOutputStream()));
                    Log.i("GROUP", "Server is sending '" + text + "'" + " to " + lsock.get(i).getInetAddress().toString() + ":"
                            + sock.getLocalPort());
                    writer.write(text);
                    writer.flush();
                }

                Log.i("GROUP", "DONE");

                //ptext = text;
                go = std.getRun();
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
