package com.pl.multicast.kraken;


import android.os.AsyncTask;
import android.util.Log;

import com.pl.multicast.kraken.clt.ClientDevice;
import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.datum.GroupData;

import java.net.MalformedURLException;
import java.util.List;


/**
 * Created by Luxon on 16/01/2017.
 */
public class Hackojo extends AsyncTask<Integer,Integer,Boolean> {

    public static final int INVALID_OP = -1;
    public static final int GROUP_OP = 0;
    public static final int DEVICE_OP = 1;
    public static final int JOIN_GROUP_OP = 2;
    public static final int QUIT_GROUP_OP = 3;
    public static final int CREATE_GROUP_OP = 4;

    private String gname;
    private List<GroupData> gdata;
    private List<DeviceData> ddata;
    private ClientDevice cd;
    int op;

    public Hackojo(DeviceData ddata, String gn) {

        gname = gn;
        try {
            cd = new ClientDevice(ddata.getName(), ddata.getAddr(), ddata.getPort(), ddata.getBroadcastPort());
        } catch (MalformedURLException e) {
            Log.e(this.getClass().getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // TODO: 13/02/2017 Communication with the hackojo server in an asynchronous task

    @Deprecated
    public void runOperation(final int idop) {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (idop == GROUP_OP) {
                    // get the groups
                    gdata = cd.groupList();
                } else if (idop == DEVICE_OP) {
                    // get the devices of a group
                    ddata = cd.deviceList(gname);
                } else if (idop == JOIN_GROUP_OP) {
                    // join a group
                    if (cd.joinGroup(gname) == false) {
                        Log.e(this.getClass().getName(),"Cannot join " + gname);
                        cd.createGroup(gname);
                    }
                } else if (idop == QUIT_GROUP_OP) {
                    // quit a group
                    cd.quitGroup(gname);
                } else if (idop == CREATE_GROUP_OP) {
                    // create a groups
                    if (cd.createGroup(gname) == false){
                        Log.e(this.getClass().getName(),"Cannot create " + gname);
                        cd.joinGroup(gname);
                    }
                }
            }
        });

        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<GroupData> getGroups() {

        return gdata;
    }

    public synchronized List<DeviceData> getDevices() {

        return ddata;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {

        boolean status;
        int idop = (params != null && params.length > 0 ? params[0] : INVALID_OP);
        op = idop;

        if(idop == INVALID_OP) {
            Log.e(this.getClass().getName(),"Invalid operation identifier" );
            return false;
        }

        if (idop == GROUP_OP) {
            // get the groups
            gdata = cd.groupList();
            status = gdata != null;
        } else if (idop == DEVICE_OP) {
            // get the devices of a group
            ddata = cd.deviceList(gname);
            status = ddata != null;
        } else if (idop == JOIN_GROUP_OP) {
            // join a group
            if (!cd.joinGroup(gname)) {
                Log.e(this.getClass().getName(),"Cannot join " + gname);
                status = cd.createGroup(gname);
            } else
                status = true;
        } else if (idop == QUIT_GROUP_OP) {
            // quit a group
            status = cd.quitGroup(gname);
        } else if (idop == CREATE_GROUP_OP) {
            // create a groups
            if (!cd.createGroup(gname)){
                Log.e(this.getClass().getName(),"Cannot create " + gname);
                status = cd.joinGroup(gname);
            } else
                status = true;
        }
        else{
            Log.e(this.getClass().getName(),"Invalid operation identifier" );
            status = false;
        }

        return status;
    }

}
