package com.pl.multicast.kraken;

import java.util.Observable;

/**
 * Created by Luxon on 04/11/2016.
 *
 *  Linked with the list of senders
 *
 *  @// TODO: 04/11/2016 Establish the research to find any device
 *  @// TODO: 04/11/2016 For each found device update every observers (this assume that the observers have been added)
 *
 */
public class MusicStreamSender extends MusicStream {

    private WifiBroadcast wifi;


    public MusicStreamSender(NavDrawer nav, GraphActivity ga){

        super(nav,ga);
        /// @// TODO: 04/11/2016 Add the navigation drawer to the list of observers
        /// @// TODO: 04/11/2016 Create the WiFiBroadcast
    }
}
