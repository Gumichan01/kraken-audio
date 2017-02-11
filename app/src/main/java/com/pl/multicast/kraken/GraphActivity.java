package com.pl.multicast.kraken;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.pl.multicast.kraken.common.KrakenMisc;
import com.pl.multicast.kraken.datum.DeviceData;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GraphActivity extends Activity
        implements NavDrawer.NavigationDrawerCallbacks {

    private static ArrayList<String> ltext = new ArrayList<>();
    // Communication point
    Hackojo hack;
    /**
     * Fragment managing the behaviours, interactions and presentation of the navigation drawer.
     */
    private NavDrawer navigationSenders;
    private NavDrawer navigationReceivers;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private String mTitle;
    private String username;
    private String gname;
    // Thread
    private Thread bserviceth;    // broadcast service
    private BroadcastService bs;
    // Receiver
    private UDPReceiver recv;
    // Data
    private BroadcastData std;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Retrieve data from the main activity
        DeviceData d = getIntent().getParcelableExtra(MainActivity.DEVICEDATA);
        gname = getIntent().getStringExtra(MainActivity.GRPNAME);
        username = d.getName();
        mTitle = username;

        // Load the broadcast data
        std = new BroadcastData();

        /** Service server */
        bs = new BroadcastService(this, std);
        bserviceth = new Thread(bs);
        bserviceth.start();

        try {
            hack = new Hackojo(d, gname);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        /** Receiver */
        recv = new UDPReceiver(this, std);
        recv.launch();

        /** Fragment creation */
        navigationSenders = (NavDrawer)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationReceivers = (NavDrawer)
                getFragmentManager().findFragmentById(R.id.navigation_drawerR);

        // Set up the drawer (left side)
        navigationSenders.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        // Set up the drawer (right side)
        navigationReceivers.setUp(
                R.id.navigation_drawerR,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        navigationSenders.updateContent(new String[]{username});
        navigationReceivers.updateContent(new String[]{username});

        /** Update the broadcast devices */
        update(true);
        notifyDevices();

        /// ONLY FOR TESTING THE BROADCAST
        /*
        UDPReceiver udpr = new UDPReceiver(this, std);
        udpr.launchReceiver();*/
    }


    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onStop() {

        super.onStop();
        Log.i(this.getLocalClassName(), "Stop the activity");
        bserviceth.interrupt();
        if (recv != null)
            recv.stop();
        hack.runOperation(Hackojo.QUIT_GROUP_OP);
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

        List<DeviceData> ld = std.getSenders();

        Log.i(this.getLocalClassName(), "onSectionAttached - " + number);

        switch (number) {
            case 1:
                mTitle = username;
                break;

            default:
                mTitle = ld.get(number - 1).getName();
                break;
        }
    }

    // Broadcast the text
    public void broadcastText(View v) {

        EditText edt = (EditText) findViewById(R.id.txtsend);
        String s = edt.getText().toString();
        Log.i(this.getLocalClassName(), "Send the following text: " + s);
        // Send message to the thread responsible of broadcasting the message
        Message m = new Message();
        m.what = KrakenMisc.TXT_ID;
        m.obj = s;
        bs.getThreadHandler().sendMessage(m);
        Log.i(this.getLocalClassName(), "Send text END");

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    // Update the list view
    public void receiveText(String text) {

        ListView lstv = (ListView) findViewById(R.id.txtrecv);
        ltext.add(text);
        lstv.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.text_recv, ltext));
        lstv.setVisibility(View.VISIBLE);
        Log.i(this.getLocalClassName(), "List updated. Added the following text: " + text);
    }

    public void notifyDevices() {

        List<DeviceData> l = new ArrayList<>();
        l.addAll(std.getSenders());
        l.addAll(std.getListeners());

        KrakenMisc.notifyDevices(username, l.iterator());
    }

    // Update the list of devices
    public void update(boolean first) {

        hack.runOperation(Hackojo.DEVICE_OP);
        std.clearSenders();
        updateWithoutConnection(first);
    }

    public void updateWithoutConnection(boolean first) {

        List<DeviceData> br = hack.getDevices();
        List<DeviceData> ltl = std.getListeners();
        List<DeviceData> lts = std.getSenders();

        if (br == null) {
            Log.i(this.getLocalClassName(), "no device");
            return;
        }

        if (first) {
            for (DeviceData dev : br)
                std.addSender(dev);
        } else {

            // Remove the devices that are listeners
            Iterator<DeviceData> it = br.iterator();

            while (it.hasNext()) {

                boolean found = false;
                boolean islistener = false;
                DeviceData d = it.next();

                for (DeviceData dev : ltl) {
                    if (dev.getName().equals(d.getName())) {
                        it.remove();
                        islistener = true;
                        break;
                    }
                }

                if (islistener) continue;

                // Add new broadcasters
                for (DeviceData dev : lts) {
                    if (dev.getName().equals(d.getName())) {
                        found = true;
                        break;
                    }
                }

                if (!found) std.addSender(d);
            }
        }

        navigationReceivers.updateContent(KrakenMisc.adaptList(ltl, username).toArray());
        navigationSenders.updateContent(KrakenMisc.adaptList(lts, username).toArray());
    }


    private DeviceData prepareRequest() {

        String slistener = new String(mTitle);
        DeviceData d = std.getSenderOf(slistener);
        mTitle = username;
        Log.i(this.getLocalClassName(), slistener + " | " + d.toString() + " | " + mTitle);
        return d;
    }

    /**
     * Action Bar
     */
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


    /**
     * Option Items
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            Log.i(this.getLocalClassName(), "update action");

            // TODO: 07/02/2017 The update has to be done in another thread (the graphic part in th UI)
            update(false);
            Toast.makeText(getApplicationContext(), R.string.msg_updating, Toast.LENGTH_LONG).show();
            return true;

        } else if (id == R.id.action_listen) {
            Log.i(this.getLocalClassName(), "listen action");
            if (!mTitle.equals(username)) {
                DeviceData d = prepareRequest();
                recv.listenRequest(d, username);
                Log.i(this.getLocalClassName(), "listening to " + d.getName());
                Toast.makeText(getApplicationContext(), "You are listening to '" + d.getName() + "'",
                        Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getApplicationContext(), "You cannot listen to yourself, idiot!",
                        Toast.LENGTH_LONG).show();


            return true;

        } else if (id == R.id.action_stop) {
            Log.i(this.getLocalClassName(), "stop action");
            if (!mTitle.equals(username)) {
                recv.stopRequest(prepareRequest(), username);
            } else
                Toast.makeText(getApplicationContext(), "You cannot stop yourself, (o_O) idiot!",
                        Toast.LENGTH_LONG).show();
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
