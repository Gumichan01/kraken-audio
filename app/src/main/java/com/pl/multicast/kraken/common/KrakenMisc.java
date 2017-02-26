package com.pl.multicast.kraken.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.pl.multicast.kraken.BroadcastService;
import com.pl.multicast.kraken.datum.DeviceData;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * Class that contains miscellaneous variables and functions
 */
public class KrakenMisc {

    public static final int SERVICE_PORT = 2408;
    public static final int BROADCAST_PORT = 2409;
    public static final int TXT_ID = 1024;
    private static final String KRAKEN_MISC = "common.KrakenMisc";

    public static String getIPAddress() {

        final String PERCENT = "%";
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

                    // In some device using ipv6, a '%' character followed by
                    // the name of the interface can be contained
                    if (ip.contains(PERCENT))
                        ip = ip.substring(0, ip.indexOf(PERCENT));

                    Log.i(KRAKEN_MISC, iface.getDisplayName() + " " + ip);
                    break;
                }
            }

        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        return ip;
    }

    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Function used in GraphActivity
    public static List<DeviceData> adaptList(List<DeviceData> ld, String username) {

        Iterator<DeviceData> it = ld.iterator();
        DeviceData dev = null;

        while (it.hasNext()) {
            DeviceData dd = it.next();
            Log.i(KRAKEN_MISC, dd.toString());
            if (dd.getName().equals(username)) dev = dd;
        }

        if (dev != null) ld.remove(dev);
        ld.add(0, new DeviceData(username, "", 0, 0));
        return ld;
    }
}
