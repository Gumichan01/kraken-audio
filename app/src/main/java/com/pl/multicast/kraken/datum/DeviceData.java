package com.pl.multicast.kraken.datum;

import android.os.Parcel;
import android.os.Parcelable;

import java.net.InetSocketAddress;

public class DeviceData implements Parcelable {

    public static final Creator<DeviceData> CREATOR = new Creator<DeviceData>() {
        @Override
        public DeviceData createFromParcel(Parcel in) {
            return new DeviceData(in);
        }

        @Override
        public DeviceData[] newArray(int size) {
            return new DeviceData[size];
        }
    };

    private static int SZ = 2;
    private String dname;
    private InetSocketAddress ipaddr;
    private int bport;
    private long timestamp;

    public DeviceData() {
        this("", "", 0, 0);
    }

    public DeviceData(String name, String ipaddress, int port, int bport) {

        ipaddr = new InetSocketAddress(ipaddress, port);
        this.bport = bport;
        dname = name;
        // Current time
        timestamp = System.currentTimeMillis();
    }

    protected DeviceData(Parcel in) {

        String[] strings = new String[SZ];
        int[] ints = new int[SZ];

        in.readStringArray(strings);
        in.readIntArray(ints);

        dname = strings[0];
        ipaddr = new InetSocketAddress(strings[1], ints[0]);
        bport = ints[1];
    }


    public String getName() {

        return dname;
    }

    public String getAddr() {

        return ipaddr.getAddress().getHostAddress();
    }

    public int getPort() {

        return ipaddr.getPort();
    }

    public int getBroadcastPort() {

        return bport;
    }

    public long getTimeStamp() {

        return timestamp;
    }

    public void updateTimeStamp() {

        System.out.println("timestamp before update: " + timestamp);
        timestamp = System.currentTimeMillis();
        System.out.println("timestamp after update: " + timestamp);
    }

    public String toString() {

        return dname + " " + getAddr() + " " + getPort() + " " + getBroadcastPort();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{dname, getAddr()});
        dest.writeIntArray(new int[]{getPort(), bport});

    }
}
