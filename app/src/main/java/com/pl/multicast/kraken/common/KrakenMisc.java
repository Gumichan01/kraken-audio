package com.pl.multicast.kraken.common;

import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Class that contains miscellaneous variables and function
 */
public class KrakenMisc {

    private static final String KRAKEN_MISC = "common.KrakenMisc";
    public static final int SERVICE_PORT = 2408;
    public static final int BROADCAST_PORT = 2409;

    public static String getIPAddress() {
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
                    Log.i(KRAKEN_MISC, iface.getDisplayName() + " " + ip);
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
