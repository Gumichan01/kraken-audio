package com.pl.multicast.kraken;

import android.app.Activity;
import android.content.DialogInterface;
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
import com.pl.multicast.kraken.datum.GroupData;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class MainActivity extends Activity implements JoinGroupDialogFragment.JoinGroupDialogListener {

    public static final String GRPNAME = "GRPNAME";
    public static final String DEVICEDATA = "DEVICEDATA";
    public static final String FRAG = "JOIN-GROUP-FRAG";
    public static final int PORT = 2408;
    public static final int BPORT = 2409;

    String usrname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

                susr += "@" + Build.MODEL;

                if (id == R.id.cgrp) {

                    EditText gtv = (EditText) findViewById(R.id.grp);

                    if (gtv == null || gtv.getText().toString().isEmpty())
                        Toast.makeText(this, "Empty string\n In order to create a group, you must specify the name",
                                Toast.LENGTH_LONG).show();
                    else {

                        String ipaddr = getIPAddress();
                        String gname = gtv.getText().toString();
                        DeviceData dd = new DeviceData(susr, ipaddr, PORT, BPORT);
                        Intent intent = new Intent(this, GraphActivity.class);

                        Log.i(this.getLocalClassName(), "group name: " + gname);
                        Log.i(this.getLocalClassName(), "device: " + dd.toString());

                        new Hackojo(dd, gname).runOperation(Hackojo.CREATE_GROUP_OP);
                        intent.putExtra(GRPNAME, gname);
                        intent.putExtra(DEVICEDATA, dd);
                        startActivity(intent);
                }

                } else if (id == R.id.jgrp) {

                    usrname = susr;     // Save the name for the next activity instance
                    Hackojo ho = new Hackojo(new DeviceData(), null);
                    Log.i(this.getLocalClassName(), "Connection to the directory server");
                    ho.runOperation(Hackojo.GROUP_OP);
                    Log.i(this.getLocalClassName(), "Generate the list of groups");
                    showDialog(ho.getGroups());

                } else {
                    Log.i(this.getLocalClassName(), "Bad view");
                }
            }
        }
    }

    private void showDialog(List<GroupData> groups) {

        JoinGroupDialogFragment fragment;

        if (groups == null || groups.isEmpty())
            fragment = JoinGroupDialogFragment.newInstance(null);
        else {

            List<String> strings = new ArrayList<>();

            for (GroupData gd : groups) {
                if (gd != null)
                    strings.add(gd.getName());
            }

            Log.i(this.getLocalClassName(), "There are a set of " + groups.size() + " group data");
            Log.i(this.getLocalClassName(), "There are " + strings.size() + " real groups");
            String[] sarray = new String[strings.size()];
            strings.toArray(sarray);
            fragment = JoinGroupDialogFragment.newInstance(sarray);
        }

        fragment.show(getFragmentManager(), FRAG);
    }

    // TODO: 06/02/2017 Put this function in a separate class (commonOperations)
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

    @Override
    public void onItemSelected(DialogInterface dialog, String gname) {

        if(dialog == null || gname == null || gname.isEmpty())
            Log.e(this.getLocalClassName(),"Cannot handle this event");
        else {

            Hackojo hackojo = null;
            DeviceData d = new DeviceData(usrname, getIPAddress(), PORT, BPORT);
            Intent intent = new Intent(this, GraphActivity.class);

            try {
                hackojo = new Hackojo(d, gname);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {

                if(hackojo != null) {

                    hackojo.runOperation(Hackojo.JOIN_GROUP_OP);
                    intent.putExtra(GRPNAME, gname);
                    intent.putExtra(DEVICEDATA, d);
                    startActivity(intent);
                }
            }
        }
    }
}
