package com.pl.multicast.kraken;

import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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


    // Commands
    public static final String LISTEN_CMD = "LISTEN\r\n";
    public static final String STOP_CMD = "STOP\r\n";
    public static final String LISTB_CMD = "LISTB\r\n";
    public static final String LISTL_CMD = "LISTL\r\n";
    // Keyword
    public static final String LISTEN = "LISTEN";
    public static final String STOP = "STOP";
    // Result
    public static final String ACK_RES = "ACK\r\n";
    public static final String BADR_RES = "BADR\r\n";
    public static final String FAIL_RES = "FAIL\r\n";
    private static final String LISTB = "LIST";
    private static final String LISTL = "LISTL";
    // Seperator
    private static final String SPACE = " ";

    private static final int LISTEN_NBTOK = 2;
    private static final int SRV_DELAY = 8000;

    private GraphActivity gactivity;
    private BroadcastData bdata;
    private UDPSender broadcaster;


    public BroadcastService(GraphActivity g, BroadcastData dd) {
        super();
        gactivity = g;
        bdata = dd;
        broadcaster = new UDPSender(dd);
    }

    public synchronized Handler getThreadHandler() {

        return broadcaster.getHandler();
    }

    public void run() {

        // Launch the service server
        try {
            ServerSocket s = new ServerSocket(KrakenMisc.SERVICE_PORT);
            s.setSoTimeout(SRV_DELAY);

            Log.i(this.getClass().getName(), "Service - Server launched");

            while (true) {

                Socket sock = null;

                try {
                    sock = s.accept();
                } catch (Exception e) {
                    Log.v(this.getClass().getName(), "Service - Timeout");
                }

                if (Thread.currentThread().isInterrupted()) {

                    if (sock != null)
                        sock.close();

                    s.close();
                    break;
                }

                if (sock == null)
                    continue;

                Log.i(this.getClass().getName(), "Service - Connection from @" + sock.getInetAddress().getHostAddress() + ":" + sock.getPort());
                PrintWriter w = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                BufferedReader r = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                String rstring = r.readLine();
                Log.i(this.getClass().getName(), "Service - received this message: " + rstring);

                // Listen to the broadcaster OR stop listening (request)
                if (rstring.contains(LISTEN) || rstring.contains(STOP)) {

                    Log.i(this.getClass().getName(), "LISTEN OR STOP");
                    w.write(basicResponse(rstring));
                    uiUpdateWithoutConnection();

                } else if (rstring.contains(LISTB)) {

                    Log.i(this.getClass().getName(), "LISTB");
                    w.write(listOfBroadcaster());
                    uiUpdate();

                } else if (rstring.contains(LISTL)) {

                    Log.i(this.getClass().getName(), "LISTL");
                    w.write(listOfListener());
                    uiUpdate();
                } else
                    Log.i(this.getClass().getName(), "error");

                w.flush();
                Log.i(this.getClass().getName(), "Service - Close the client socket");
                sock.close();
            }

            Log.i(this.getClass().getName(), "Service - Server down");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    private void uiUpdate() {

        gactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gactivity.update(false);
            }
        });
    }

    private void uiUpdateWithoutConnection() {

        gactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gactivity.updateWithoutConnection(false);
            }
        });
    }

    private boolean registerListener(String s) {

        final DeviceData dev;
        List<DeviceData> ld = bdata.getSenders();
        int i = 0;

        while (i < ld.size() && !ld.get(i).getName().equals(s))
            i++;

        if (i < ld.size()) {

            Log.i(this.getClass().getName(), ld.get(i).toString());
            dev = ld.get(i);
        } else
            return false;

        bdata.rmSender(dev);
        bdata.addListener(dev);
        Log.i(this.getClass().getName(), "Service - Register listener: ok");
        gactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gactivity.getApplicationContext(), "\"" + dev.getName() + "\" is listening to you", Toast.LENGTH_LONG).show();
            }
        });
        return true;
    }

    private boolean unregisterListener(String s) {

        final DeviceData dev;
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
        Log.i(this.getClass().getName(), "Service - Unregister register listener: ok");

        gactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gactivity.getApplicationContext(), "\"" + dev.getName() + "\" stopped listening to you", Toast.LENGTH_LONG).show();
            }
        });
        return true;
    }

    private String basicResponse(String rstring) {

        Pattern p = Pattern.compile(SPACE);
        String[] ss = p.split(rstring);

        if (ss.length != LISTEN_NBTOK)
            return BADR_RES;
        else {
            if (rstring.contains(LISTEN))
                return (registerListener(ss[1]) ? ACK_RES : FAIL_RES);
            else
                return (unregisterListener(ss[1]) ? ACK_RES : FAIL_RES);
        }
    }


    private String listOfBroadcaster() {

        return listOfDevice(bdata.getSenders().iterator());
    }

    private String listOfListener() {

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
