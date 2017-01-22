package com.pl.multicast.kraken;


import java.util.List;

import clt.ClientDevice;
import datum.DeviceData;
import datum.GroupData;

/**
 * Created by Luxon on 16/01/2017.
 */
public class NetworkThread implements Runnable {

    public static final int GROUP_OP = 0;
    public static final int DEVICE_OP = 1;
    public static final int JOIN_GROUP_OP = 2;
    public static final int QUIT_GROUP_OP = 3;
    public static final int CREATE_GROUP_OP = 4;

    private int idop;
    private String gname;
    private List<GroupData> gdata;
    private List<DeviceData> ddata;
    private ClientDevice cd;

    public NetworkThread(String name, String addr, int port, int bport) {

        idop = 0;
        gname = null;
        cd = new ClientDevice(name, addr, port, bport);
    }

    public synchronized void run() {

        //synchronized(this){

        if (idop == GROUP_OP) {
            // get the groups
            gdata = cd.groupList();
        } else if (idop == DEVICE_OP) {
            // get the devices of a group
            ddata = cd.deviceList(gname);
        } else if (idop == JOIN_GROUP_OP) {
            // join a group
        } else if (idop == QUIT_GROUP_OP) {
            // quit a group
            cd.quitGroup(gname);
        } else if (idop == CREATE_GROUP_OP) {
            // create a groups
        }
        //}
    }

    public synchronized void setOp(int op) {

        idop = op;
    }

    public synchronized void setGroupName(String name) {

        if (name != null)
            gname = name;
    }

    public synchronized List<GroupData> getGroups() {

        return gdata;
    }

    public synchronized List<DeviceData> getDevices() {

        return ddata;
    }
}
