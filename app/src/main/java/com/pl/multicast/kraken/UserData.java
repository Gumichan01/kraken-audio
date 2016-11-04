package com.pl.multicast.kraken;

/**
 * Created by Luxon on 04/11/2016.
 */
public class UserData {

    public static final String USER_ADDED = "USER_ADDED";
    public static final String USER_REMOVED = "USER_REMOVED";

    public final String name;
    public final state;

    public UserData(String usr, String st)
    {
        name = usr;
        state = st;
    }
}
