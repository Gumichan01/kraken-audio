package com.pl.multicast.kraken;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * @// TODO: 02/11/2016
 * The left drawer fragment must listen to the future MusicStreamReceiver and MusicStreamSender.
 * MusicStreamReceiver is the class responsible of receiving data from another device using WI-FI Direct™
 * MusicStreamSender is the class responsible of sending data from another device using WI-FI Direct™
 * <p/>
 * So, the left navigation drawer will be an observer of MusicStreamReceiver
 * and the right navigation drawer will be an observer of MusicStreamSender
 */

public class GraphActivity extends Activity
        implements NavDrawer.NavigationDrawerCallbacks {

    private final IntentFilter intentFilter = new IntentFilter();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavDrawer navigationSenders;
    private NavDrawer navigationReceivers;
    private MusicStreamSender msSender;
    private MusicStreamReceiver msReceiver;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private String username;
    private WifiBroadcast wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Fragment creation
        navigationSenders = (NavDrawer)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        navigationReceivers = (NavDrawer)
                getFragmentManager().findFragmentById(R.id.navigation_drawerR);

        // Set the attributes
        username = getIntent().getStringExtra(MainActivity.USRNAME) + "@" + Build.MODEL;
        mTitle = username;

        // Set up the drawer (left side)
        navigationSenders.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set up the drawer (right side)
        navigationReceivers.setUp(
                R.id.navigation_drawerR,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        /// @// TODO: 04/11/2016 Set up the broadcast receiver and the Peer-to-Peer manager (P2P)

        WifiP2pManager.Channel chan = null;
        WifiP2pManager wifip2p = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        if (wifip2p != null)
            chan = wifip2p.initialize(this, getMainLooper(), null);
        else
            Toast.makeText(this, "Wifi-direct not available", Toast.LENGTH_SHORT).show();

        wifiReceiver = new WifiBroadcast(wifip2p,chan, this);

        // Changes in the Wifi P2P status
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Changes in the list of available peers
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Changes in Wi-FI P2P connecivity
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Changes in device's details
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        msSender = new MusicStreamSender(navigationSenders, wifiReceiver);
        // Do the same thing with msReceiver
    }


    @Override
    public void onResume(){

        super.onResume();
        registerReceiver(wifiReceiver, intentFilter);
        Log.i("WIFI-DIRECT_STATUS", "resume");
    }

    @Override
    public void onPause(){

        super.onPause();
        unregisterReceiver(wifiReceiver);
        Log.i("WIFI-DIRECT_STATUS", "pause");
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = username;
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationSenders.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.graph, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((GraphActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
