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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Created by Luxon on 19/01/2017.
 */
public class BroadcastService implements Runnable {

    public static final String ACK = "ACK\r\n";
    private static final String LISTEN_CMD = "LISTEN";
    private static final String STOP_CMD = "STOP";
    private static final String LISTB_CMD = "LISTB";
    private static final String LISTL_CMD = "LISTL";
    private static final String BADR = "BADR\r\n";
    private static final String FAIL = "FAIL\r\n";
    private static final String SPACE = " ";

    private static final int LISTEN_NBTOK = 2;

    private GraphActivity gactivity;
    private BroadcastData bdata;

    public BroadcastService(GraphActivity g, BroadcastData dd) {
        super();
        gactivity = g;
        bdata = dd;
    }


    public void run() {

        try {
            ServerSocket s = new ServerSocket(KrakenMisc.SERVICE_PORT);

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

                // Listen to the broadcaster (request)
                if (rstring.contains(LISTEN_CMD)) {

                    Pattern p = Pattern.compile(SPACE);
                    String[] ss = p.split(rstring);

                    if (ss.length != LISTEN_NBTOK)
                        w.write(BADR);
                    else
                        w.write(registerListener(ss[1]) ? ACK : FAIL);

                } else if (rstring.contains(STOP_CMD)) {    // Stop listening (request)

                    Pattern p = Pattern.compile(SPACE);
                    String[] ss = p.split(rstring);

                    if (ss.length != LISTEN_NBTOK)
                        w.write(BADR);
                    else
                        w.write(unregisterListener(ss[1]) ? ACK : FAIL);

                } else if (rstring.contains(LISTB_CMD)) {

                    w.write(listOfBroadcaster());

                } else if (rstring.contains(LISTL_CMD)) {

                    w.write(listOfListener());
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

        if (i < ld.size()) {

            Log.i("GROUP", ld.get(i).toString());
            dev = ld.get(i);
        } else
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

    public String listOfBroadcaster() {

        return listOfDevice(bdata.getSenders().iterator());
    }

    public String listOfListener() {

        return listOfDevice(bdata.getListeners().iterator());
    }

    private String listOfDevice(Iterator<DeviceData> it) {

        StringBuilder sb = new StringBuilder("");

        while (it.hasNext()) {

            DeviceData dd = it.next();
            sb.append(MessageParser.SRV_DDAT);
            sb.append(" ");
            sb.append(dd.toString());
            sb.append(MessageParser.EOL);
        }

        return sb.append(MessageParser.SRV_EOTR).append(MessageParser.EOL).toString();
    }
}
