package com.pl.multicast.kraken;


import android.os.AsyncTask;
import android.util.Log;

import com.pl.multicast.kraken.clt.ClientDevice;
import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.datum.GroupData;
import com.pl.multicast.kraken.parser.MessageParser;

import java.net.MalformedURLException;
import java.util.List;


/**
 * Created by Luxon on 16/01/2017.
 */
public class Hackojo extends AsyncTask<Integer, Integer, Boolean> {

    public static final int INVALID_OP = -1;
    public static final int GROUP_OP = 0;
    public static final int DEVICE_OP = 1;
    public static final int JOIN_GROUP_OP = 2;
    public static final int QUIT_GROUP_OP = 3;
    public static final int CREATE_GROUP_OP = 4;
    public static final int GRAPH_LINK_OP = 5;
    public static final int GRAPH_UNLINK_OP = 6;
    public static final int GRAPH_GET_OP = 7;
    int op;
    private String gname;
    private List<GroupData> gdata;
    private List<DeviceData> ddata;
    private ClientDevice cd;
    private String dest;

    public Hackojo(DeviceData ddata, String gn) {

        gname = gn;
        try {
            cd = new ClientDevice(ddata.getName(), ddata.getAddr(), ddata.getPort(), ddata.getBroadcastPort());
        } catch (MalformedURLException e) {
            Log.e(this.getClass().getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public synchronized List<GroupData> getGroups() {

        return gdata;
    }

    public synchronized List<DeviceData> getDevices() {

        return ddata;
    }

    public synchronized void setDestForGraph(String d) {

        if(d == null)
            Log.i(getClass().getName(),"setDestForGraph — null");
        else
            dest = d;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {

        boolean status;
        int idop = (params != null && params.length > 0 ? params[0] : INVALID_OP);
        op = idop;

        switch (idop) {

            case GROUP_OP:
                // get the groups
                publishProgress(null);
                gdata = cd.groupList();
                status = gdata != null;
                break;

            case DEVICE_OP:
                // get the devices of a group
                ddata = cd.deviceList(gname);
                status = ddata != null;
                break;

            case JOIN_GROUP_OP:
                // join a group
                if (!cd.joinGroup(gname)) {
                    Log.e(this.getClass().getName(), "Cannot join " + gname);
                    status = cd.createGroup(gname);
                } else
                    status = true;
                break;

            case QUIT_GROUP_OP:
                // quit a group
                status = cd.quitGroup(gname);
                break;

            case CREATE_GROUP_OP:
                // create a groups
                if (!cd.createGroup(gname)) {
                    Log.e(this.getClass().getName(), "Cannot create " + gname);
                    status = cd.joinGroup(gname);
                } else
                    status = true;
                break;

            case GRAPH_LINK_OP:
                // set a new link
                if(!cd.updateGraph(MessageParser.ARROW, dest)) {
                    Log.e(this.getClass().getName(), "Cannot link with " + dest);
                    status = false;
                }
                else
                    status = true;
                break;

            case GRAPH_UNLINK_OP:
                // set a new link
                if(!cd.updateGraph(MessageParser.CROSS, dest)) {
                    Log.e(this.getClass().getName(), "Cannot unlink with " + dest);
                    status = false;
                }
                else
                    status = true;
                break;


            default:
                Log.e(this.getClass().getName(), "Invalid operation identifier");
                status = false;
                break;
        }
        return status;
    }
}
