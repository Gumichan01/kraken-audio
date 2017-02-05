package com.pl.multicast.kraken;


import com.pl.multicast.kraken.clt.ClientDevice;
import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.datum.GroupData;

import java.net.MalformedURLException;
import java.util.List;


/**
 * Created by Luxon on 16/01/2017.
 */
public class Hackojo {

    public static final int GROUP_OP = 0;
    public static final int DEVICE_OP = 1;
    public static final int JOIN_GROUP_OP = 2;
    public static final int QUIT_GROUP_OP = 3;
    public static final int CREATE_GROUP_OP = 4;

    private String gname;
    private List<GroupData> gdata;
    private List<DeviceData> ddata;
    private ClientDevice cd;

    public Hackojo(String name, String addr, int port, int bport) throws MalformedURLException {

        gname = null;
        cd = new ClientDevice(name, addr, port, bport);
    }

    public void runOperation(int idop) {

        if (idop == GROUP_OP) {
            // get the groups
            gdata = cd.groupList();
        } else if (idop == DEVICE_OP) {
            // get the devices of a group
            ddata = cd.deviceList(gname);
        } else if (idop == JOIN_GROUP_OP) {
            // join a group
            if (cd.joinGroup(gname) == false)
                cd.createGroup(gname);
        } else if (idop == QUIT_GROUP_OP) {
            // quit a group
            cd.quitGroup(gname);
        } else if (idop == CREATE_GROUP_OP) {
            // create a groups
            if(cd.createGroup(gname) == false)
                cd.joinGroup(gname);
        }
    }

    public synchronized List<GroupData> getGroups() {

        return gdata;
    }

    public synchronized List<DeviceData> getDevices() {

        return ddata;
    }
}
