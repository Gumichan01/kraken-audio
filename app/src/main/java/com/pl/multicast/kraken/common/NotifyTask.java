package com.pl.multicast.kraken.common;

import android.os.AsyncTask;
import android.util.Log;

import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.parser.MessageParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;

/**
 * Created by Luxon on 11/02/2017.
 */
public class NotifyTask extends AsyncTask<Iterator<DeviceData>, Integer, Void> {

    private String hreq;
    private String uname;

    public NotifyTask(String hreq, String username) {
        super();

        this.hreq = hreq;
        uname = username;
    }

    @Override
    protected Void doInBackground(Iterator<DeviceData>... params) {

        Log.i(this.getClass().getName(), "Notify - background task - " + hreq);
        Iterator<DeviceData> it = params[0];

        while (it.hasNext()) {

            DeviceData d = it.next();

            if (d.getName().equals(uname))
                continue;

            try {

                Log.i(this.getClass().getName(), "Notify - " + d.getName() + " " + d.getAddr() + ":" + d.getPort());
                Socket s = new Socket(d.getAddr(), d.getPort());
                s.setSoTimeout(1000);

                PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
                Log.i(this.getClass().getName(), "Notify - msg sent: " + hreq + " " + uname + MessageParser.EOL);
                writer.write(hreq + " " + uname + MessageParser.EOL);
                writer.flush();

                String rstring = reader.readLine();
                Log.i(this.getClass().getName(), "Notify - msg received: " + rstring);
                s.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.i(this.getClass().getName(), "Notify - background task - " + hreq + " - #END");

        return null;
    }
}
