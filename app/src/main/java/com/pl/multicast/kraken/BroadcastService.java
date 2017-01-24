package com.pl.multicast.kraken;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.regex.Pattern;

import datum.DeviceData;

/**
 * Created by Luxon on 19/01/2017.
 */
public class BroadcastService implements Runnable {

    private static final String LISTEN_CMD = "LISTEN";
    private static final String STOP_CMD = "STOP";
    private static final String ACK = "ACK\r\n";
    private static final String BADR = "BADR\r\n";
    private static final String FAIL = "FAIL\r\n";
    private static final String SPACE = " ";

    private static final int LISTEN_NBTOK = 2;

    private GraphActivity gactivity;
    private BroadcastData bdata;

    public BroadcastService(GraphActivity g,BroadcastData dd) {
        super();
        gactivity = g;
        bdata = dd;
    }


    public void run() {

        try {
            ServerSocket s = new ServerSocket(2408);

            Log.i("GROUP", "Service - Server launched");

            while (bdata.getRun()) {

                Socket sock = s.accept();

                if (sock == null)
                    break;

                Log.i("GROUP", "Service - Connection from @" + sock.getInetAddress().getHostAddress() + ":" + sock.getPort());
                PrintWriter w = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                BufferedReader r = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                String rstring = r.readLine();
                Log.i("GROUP", "Service - received this message: " + rstring);

                if (rstring.contains(LISTEN_CMD)) {

                    Pattern p = Pattern.compile(SPACE);
                    String[] ss = p.split(rstring);

                    if (ss.length != LISTEN_NBTOK)
                        w.write(BADR);
                    else
                        w.write(registerListener(ss[1]) ? ACK : FAIL);

                } else if (rstring.contains(STOP_CMD)) {

                    Pattern p = Pattern.compile(SPACE);
                    String[] ss = p.split(rstring);

                    if (ss.length != LISTEN_NBTOK)
                        w.write(BADR);
                    else
                        w.write(unregisterListener(ss[1]) ? ACK : FAIL);
                }

                gactivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gactivity.update();
                    }
                });

                w.flush();
                Log.i("GROUP", "Service - Close the client socket");
                sock.close();
            }

            s.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public boolean registerListener(String s) {

        DeviceData dev = null;
        List<DeviceData> ld = bdata.getSenders();
        int i = 0;

        while (i < ld.size() && !ld.get(i).getName().equals(s))
            i++;

        if (i < ld.size())
            dev = ld.get(i);
        else
            return false;

        bdata.rmSender(dev);
        bdata.addListener(dev);
        Log.i("GROUP", "Service - Register listener: ok");
        return true;
    }

    public boolean unregisterListener(String s) {

        DeviceData dev = null;
        List<DeviceData> ld = bdata.getListeners();
        int i = 0;

        while (i < ld.size() && !ld.get(i).getName().equals(s))
            i++;

        if (i < ld.size())
            dev = ld.get(i);
        else
            return false;

        bdata.rmListener(dev);
        bdata.addSender(dev);
        Log.i("GROUP", "Service - Unregister register listener: ok");
        return true;
    }
}
