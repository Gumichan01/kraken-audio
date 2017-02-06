package com.pl.multicast.kraken;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.pl.multicast.kraken.datum.DeviceData;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


public class MainActivity extends Activity {

    public static final String GRPNAME = "GRPNAME";
    public static final String DEVICEDATA = "DEVICEDATA";
    public static final String FRAG = "JOIN-GROUP-FRAG";

    private JoinGroupDialogFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragment = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void mix(View v) throws MalformedURLException {

        int id = v.getId();
        EditText tv = (EditText) findViewById(R.id.usr);

        if (tv == null)
            Log.e(this.getLocalClassName(), "Internal error - usr: no edit text");
        else {

            String susr = tv.getText().toString();

            if (susr.isEmpty())
                Toast.makeText(this, "Empty string", Toast.LENGTH_SHORT).show();
            else {

                if (id == R.id.cgrp) {

                    EditText gtv = (EditText) findViewById(R.id.grp);

                    if (gtv == null || gtv.getText().toString().isEmpty())
                        Toast.makeText(this, "Empty string\n In order to create a group, you must specify the name",
                                Toast.LENGTH_LONG).show();
                    else {

                        susr += "@" + Build.MODEL;
                        String ipaddr = getIPAddress();
                        String gname = gtv.getText().toString();
                        DeviceData dd = new DeviceData(susr, ipaddr, 2408, 2409);
                        Intent intent = new Intent(this, GraphActivity.class);

                        Log.i(this.getLocalClassName(), "group name: " + gname);
                        Log.i(this.getLocalClassName(), "device: " + dd.toString());

                        new Hackojo(dd, gname).runOperation(Hackojo.CREATE_GROUP_OP);
                        intent.putExtra(GRPNAME, gname);
                        intent.putExtra(DEVICEDATA, dd);
                        startActivity(intent);
                    }

                } else if (id == R.id.jgrp) {

                    // TODO: 05/02/2017 join a group in the server
                    Log.i(this.getLocalClassName(), "dialog");
                    fragment = new JoinGroupDialogFragment();
                    fragment.show(getFragmentManager(), FRAG);

                } else {
                    Log.i(this.getLocalClassName(), "Bad view");
                }
            }
        }
    }

    private String getIPAddress() {
        String ip = null;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                if (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    ip = addr.getHostAddress();
                    Log.i(this.getLocalClassName(), iface.getDisplayName() + " " + ip);
                    break;
                }
            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } finally {
            return ip;
        }

    }

}
