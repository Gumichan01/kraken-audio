package com.pl.multicast.kraken;


import java.util.List;

import datum.*;
import clt.*;

/**
 * Created by Luxon on 16/01/2017.
 */
public class NetworkThread extends Thread {

    private int idop;
    private String gname;
    private List<GroupData> gdata;
    private List<DeviceData> ddata;
    private ClientDevice cd;

    public NetworkThread(String name, String addr, int port, int bport){

        idop = 0;
        gname = null;
        cd = new ClientDevice(name, addr, port, bport);
    }

    public synchronized void run(){

        //synchronized(this){

            if(idop == 0){
                // get the groups
                gdata = cd.groupList();

            } else if(idop == 1){
                // get the devices of a group
                ddata = cd.deviceList(gname);
            } else if(idop == 2){
                // join a group
            } else if(idop == 3){
                // quit a group
                cd.quitGroup(gname);
            } else if(idop == 4){
                // create a groups
            }
        //}
    }

    public synchronized void setOp(int op){

        idop = op;
    }

    public synchronized void setGroupName(String name){

        if(name != null)
            gname = name;
    }

    public synchronized List<GroupData> getGroups(){

        return gdata;
    }

    public synchronized List<DeviceData> getDevices(){

        return ddata;
    }
}
