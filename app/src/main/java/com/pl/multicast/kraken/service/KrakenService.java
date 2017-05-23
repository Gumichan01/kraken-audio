package com.pl.multicast.kraken.service;

import android.util.Log;
import android.widget.Toast;

import com.pl.multicast.kraken.MixActivity;
import com.pl.multicast.kraken.common.Hackojo;
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
public class KrakenService implements Runnable {

    // Keyword
    public static final String LISTEN = "LISTEN";
    public static final String STOP = "STOP";
    public static final String UPDATE = "UPDATE";
    public static final String QUIT = "QUIT";
    public static final String ACK = "ACK";
    // Result
    public static final String ACK_RES = "ACK\r\n";
    public static final String BADR_RES = "BADR\r\n";
    public static final String FAIL_RES = "FAIL\r\n";
    public static final String LISTB = "LISTB";
    public static final String LISTL = "LISTL";
    public static final String LISTR = "LISTR";
    // Separator
    private static final String SPACE = " ";

    private static final int LISTEN_NBTOK = 2;
    private static final int SRV_DELAY = 8000;

    private DeviceData d;
    private MixActivity gactivity;
    private KrakenBroadcastData bdata;


    public KrakenService(MixActivity g, KrakenBroadcastData dd) {
        super();
        gactivity = g;
        d = g.getDevData();
        bdata = dd;
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

                if (sock == null) {
                    // Notitifier IAMH in a separate thread
                    new Hackojo(d, "").execute(Hackojo.IAM_HERE_OP);
                    continue;
                }

                Log.i(this.getClass().getName(), "Service - Connection from @" + sock.getInetAddress().getHostAddress() + ":" + sock.getPort());
                PrintWriter w = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                BufferedReader r = new BufferedReader(new InputStreamReader(sock.getInputStream()));

                boolean toupdate = false;
                String rstring = r.readLine();
                Log.i(this.getClass().getName(), "Service - received this message: " + rstring);

                // Listen to the broadcaster OR stop listening (request)
                if (rstring.contains(LISTEN) || rstring.contains(STOP) || rstring.contains(UPDATE)
                        || rstring.contains(QUIT)) {

                    w.write(basicResponse(rstring));
                    toupdate = true;

                } else if (rstring.contains(LISTB)) {

                    Log.i(this.getClass().getName(), LISTB);
                    w.write(listOfBroadcaster());
                    toupdate = false;

                } else if (rstring.contains(LISTL)) {

                    Log.i(this.getClass().getName(), LISTL);
                    w.write(listOfListener());
                    toupdate = false;
                } else if (rstring.contains(LISTR)) {

                    Log.i(this.getClass().getName(), LISTR);
                    w.write(listOfRealBroadcaster());
                    toupdate = false;
                } else
                    Log.i(this.getClass().getName(), "error");

                w.flush();
                Log.i(this.getClass().getName(), "Service - Close the client socket");
                sock.close();

                if (toupdate)
                    uiUpdate();
            }

            Log.i(this.getClass().getName(), "Service - Server down");

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    private void newDevice(final String devname) {

        gactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gactivity.getApplicationContext(), "\"" + devname + "\" joined the group", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void rmDevice(final String devname) {

        gactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(gactivity.getApplicationContext(), "\"" + devname + "\" left the group", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uiUpdate() {

        gactivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(this.getClass().getName(), "Service - update");
                gactivity.update(false);
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

        bdata.rmBroadcaster(dev);
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
        bdata.addBroadcaster(dev);
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
            if (rstring.contains(LISTEN)) {
                boolean b = registerListener(ss[1]);
                if (b) {

                    Hackojo h = new Hackojo(gactivity.getDevData(), gactivity.getGroupName());
                    h.setDestForGraph(ss[1]);
                    h.execute(Hackojo.GRAPH_LINK_OP);
                    return ACK_RES;

                } else
                    return FAIL_RES;

            } else if (rstring.contains(STOP)) {

                boolean b = unregisterListener(ss[1]);

                if (b) {

                    Hackojo h = new Hackojo(gactivity.getDevData(), gactivity.getGroupName());
                    h.setDestForGraph(ss[1]);
                    h.execute(Hackojo.GRAPH_UNLINK_OP);
                    return ACK_RES;

                } else
                    return FAIL_RES;

            } else if (rstring.contains(UPDATE))
                newDevice(ss[1]);
            else if (rstring.contains(QUIT))
                rmDevice(ss[1]);

            return ACK_RES;
        }
    }


    private String listOfBroadcaster() {

        return listOfDevice(bdata.getSenders().iterator());
    }

    private String listOfListener() {

        return listOfDevice(bdata.getListeners().iterator());
    }

    private String listOfRealBroadcaster() {

        return listOfDevice(bdata.getRealBroadcasters().iterator());
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
