package com.pl.multicast.kraken;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.pl.multicast.kraken.common.KrakenMisc;
import com.pl.multicast.kraken.common.NotifyTask;
import com.pl.multicast.kraken.datum.DeviceData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class GraphActivity extends Activity
        implements NavDrawer.NavigationDrawerCallbacks {

    public static String username;
    private static ArrayList<String> ltext = new ArrayList<>();
    // Broadcast
    KrakenBroadcast kbroadcast;
    /**
     * Fragment managing the behaviours, interactions and presentation of the navigation drawer.
     */
    private NavDrawer navigationSenders;
    private NavDrawer navigationReceivers;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private String mTitle;
    private String gname;
    private DeviceData d;
    private int idnav_left;
    private int idnav_right;
    private int idnav_selected;
    /**
     * Used to store the device display lists
     */
    private String[] bdnames;      // list of the broadcaster devices displayed on the screen
    private String[] rdnames;      // list of the receiver devices displayed on the screen
    // Thread
    private Thread bserviceth;      // broadcast service thread
    private BroadcastService bs;
    // Data
    private BroadcastData std;      // Data broadcasting information

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        /** Retrieve data from the main activity */
        idnav_selected = 0;
        d = getIntent().getParcelableExtra(MainActivity.DEVICEDATA);
        gname = getIntent().getStringExtra(MainActivity.GRPNAME);
        username = d.getName();
        mTitle = username;
        bdnames = null;
        rdnames = null;

        /** Load the broadcast data and the communication point */
        std = new BroadcastData();

        /** Service server */
        bs = new BroadcastService(this, std);
        bserviceth = new Thread(bs);
        bserviceth.start();

        /** Broadcast */
        kbroadcast = new KrakenBroadcast(this, std);
        kbroadcast.launch();

        /** Display */
        TextView txv = (TextView) findViewById(R.id.text_rate);
        txv.setText("Rate: 0 bytes/s");

        Switch bswitch = (Switch) findViewById(R.id.switch_broad);
        Switch lswitch = (Switch) findViewById(R.id.switch_listen);

        bswitch.setChecked(kbroadcast.getBroadcastOption());
        lswitch.setChecked(kbroadcast.getListenOption());

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

        idnav_left = navigationSenders.getId();
        idnav_right = navigationReceivers.getId();

        /** Update the broadcast devices */
        update(true);
    }

    @Override
    public void onRestart() {

        super.onRestart();
        Log.i(this.getLocalClassName(), "Restart the activity");
    }

    @Override
    public void onResume() {

        super.onResume();
        Log.i(this.getLocalClassName(), "Resume the activity");
    }

    @Override
    public void onPause() {

        super.onPause();
        Log.i(this.getLocalClassName(), "Pause the activity");
    }

    @Override
    public void onStop() {

        super.onStop();
        Log.i(this.getLocalClassName(), "Stop the activity");
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.i(this.getLocalClassName(), "Destroy the activity");
        bserviceth.interrupt();
        kbroadcast.stop();

        if (KrakenMisc.isNetworkAvailable(getApplicationContext()))
            new AsyncGraphTask(d, gname).execute(Hackojo.QUIT_GROUP_OP);
        else
            Log.e(this.getLocalClassName(), "Cannot quit the group - network unavailable");
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
        Log.i(this.getLocalClassName(), "onSectionAttached - id navigation left: " + idnav_left);
        Log.i(this.getLocalClassName(), "onSectionAttached - id navigation right: " + idnav_right);
        Log.i(this.getLocalClassName(), "onSectionAttached - id navigation selected: " + idnav_selected);

        if(idnav_selected == idnav_left) {
            ld = std.getSenders();
        } else if(idnav_selected == idnav_right) {
            ld = std.getListeners();
        } else {
            Log.wtf(getLocalClassName(), "Unknown value. It should NEVER happen!");
        }

        switch (number) {
            case 1:
                mTitle = username;
                break;

            default:
                mTitle = ld.get(number - 1).getName();
                break;
        }
    }

    public void setIDNavSelected(int id) {

        idnav_selected = id;
    }

    public void configureAudio(View v) {

        if (v.getId() == R.id.button_config) {

            EditText rate_edit = (EditText) findViewById(R.id.edit_rate);
            EditText freq_edit = (EditText) findViewById(R.id.edit_freq);
            EditText duration_edit = (EditText) findViewById(R.id.edit_duration);
            CheckBox stereo_box = (CheckBox) findViewById(R.id.box_stereo);

            boolean stereo;
            int samplerate;
            int frequency;
            int duration;

            if (rate_edit.getText().toString().isEmpty())
                samplerate = Integer.parseInt(rate_edit.getHint().toString());
            else
                samplerate = Integer.parseInt(rate_edit.getText().toString());

            if (freq_edit.getText().toString().isEmpty())
                frequency = Integer.parseInt(freq_edit.getHint().toString());
            else
                frequency = Integer.parseInt(freq_edit.getText().toString());

            if (duration_edit.getText().toString().isEmpty())
                duration = Integer.parseInt(duration_edit.getHint().toString());
            else
                duration = Integer.parseInt(duration_edit.getText().toString());

            stereo = stereo_box.isChecked();

            kbroadcast.setAudioConfig(samplerate, frequency, stereo, duration);
            Log.i(this.getLocalClassName(), "" + samplerate + "/" + frequency + "/" + duration + ":" + stereo);

        } else
            Log.e(this.getLocalClassName(), "Invalid button");
    }

    /**
     * Generate and play sound
     */
    public void play(View v) {

        if (v.getId() == R.id.button_gp) {

            Log.i(this.getLocalClassName(), "generate and play sound");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    kbroadcast.playGeneratedSound();
                }
            }).start();
        }
    }


    /**
     * Switch the option that lets you broadcast data or not
     */
    public void switchBroadcastOption(View v) {

        Log.i(this.getLocalClassName(), "activate broadcast option");

        if (v.getId() == R.id.switch_broad) {

            Switch sw = (Switch) v;

            if (sw.isChecked()) {

                kbroadcast.setBroadcastOption(true);
                Toast.makeText(this, "Broadcast enabled", Toast.LENGTH_SHORT).show();
            } else {
                kbroadcast.setBroadcastOption(false);
                Toast.makeText(this, "Broadcast disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Switch the option that lets you activate or unactivate audio playing
     */
    public void switchListenOption(View v) {

        Log.i(this.getLocalClassName(), "activate listen option");

        if (v.getId() == R.id.switch_listen) {

            Switch sw = (Switch) v;

            if (sw.isChecked()) {

                kbroadcast.setListenOption(true);
                Toast.makeText(this, "Listen enabled", Toast.LENGTH_SHORT).show();
            } else {
                kbroadcast.setListenOption(false);
                Toast.makeText(this, "Listen disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void displayRate(long nbytes) {

        TextView txv = (TextView) findViewById(R.id.text_rate);
        StringBuilder stbuild = new StringBuilder("Rate: ");

        stbuild.append(nbytes).append(" bytes/s");
        txv.setText(stbuild.toString());
        txv.setVisibility(View.VISIBLE);
    }

    // Notify devices - a new device joined the group
    public void notifyDevices() {

        List<DeviceData> l = new ArrayList<>();
        l.addAll(std.getSenders());
        l.addAll(std.getListeners());

        Log.i(this.getLocalClassName(), "Notify every devices by " + username);
        notifyUpdateDevices(username, l.iterator());
    }

    private void notifyUpdateDevices(String username, Iterator<DeviceData> it) {

        notifyDevices(BroadcastService.UPDATE, username, it);
    }

    private void notifyQuitDevices(String username, Iterator<DeviceData> it) {

        notifyDevices(BroadcastService.QUIT, username, it);
    }


    private void notifyDevices(String hreq, String username, Iterator<DeviceData> it) {

        NotifyTask nt = new NotifyTask(hreq, username);
        nt.execute(it);
    }

    // Update the list of devices
    public void update(boolean first) {

        if (KrakenMisc.isNetworkAvailable(getApplicationContext())) {

            AsyncGraphTask async = new AsyncGraphTask(d, gname);
            async.setFirstUpdate(first);
            async.execute(Hackojo.DEVICE_OP);
            Log.i(this.getLocalClassName(), "update devl");
        } else {
            Log.e(this.getLocalClassName(), "Cannot update the group content - network unavailable");
            Toast.makeText(getApplicationContext(), R.string.gunetwork, Toast.LENGTH_LONG).show();
        }
    }

    private void updateGroupContent(Hackojo hack, boolean first) {

        List<DeviceData> br = hack.getDevices();
        List<DeviceData> ltl = std.getListeners();
        List<DeviceData> lts = std.getSenders();

        if (br == null) {
            Log.e(this.getLocalClassName(), "no device");
            return;
        }

        if (first) {
            for (DeviceData dev : br)
                std.addBroadcaster(dev);
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

                if (!found) std.addBroadcaster(d);
            }
        }

        bdnames = generateDisplayList(lts);
        rdnames = generateDisplayList(ltl);
        navigationSenders.updateContent(bdnames);
        navigationReceivers.updateContent(rdnames);
    }

    /**
     * Generate the list of the devices that will be displayed in the navigation drawers
     */
    private String[] generateDisplayList(List<DeviceData> l) {

        List<DeviceData> dlist = KrakenMisc.adaptList(l, username);
        String[] dnames = new String[dlist.size()];

        for (int i = 0; i < dnames.length; i++) {

            dnames[i] = dlist.get(i).getName();
            if (std.isRealBroadcaster(dnames[i]))
                dnames[i] += "#";
        }

        Log.i(this.getLocalClassName(), "display list ↓");
        for (String s : dnames) Log.i(this.getLocalClassName(), s + " ");
        Log.i(this.getLocalClassName(), "display list ↑");

        return dnames;
    }

    /**
     * Prepare the request getting the device data to listen
     */
    private DeviceData prepareRequest() {

        String slistener = mTitle;
        DeviceData d = std.getBroadcasterOf(slistener);
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
     * Option Items — Specify what will happen when the user selects a button ("Listen" or "Stop" or "Path²")
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        UDPReceiver recv = kbroadcast.getReceiver();

        if (id == R.id.action_listen) {

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

        } else if (id == R.id.action_graph) {

            AsyncGraphTask async = new AsyncGraphTask(d, gname);
            async.execute(Hackojo.GRAPH_GET_OP);
        }

        return super.onOptionsItemSelected(item);
    }

    ///
    /// Inner classes
    ///

    public DeviceData getDevData() {

        return d;
    }

    public String getGroupName() {

        return gname;
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

    /**
     * AsyncGraphTask is a specialized Hackojo class (asynchronous task in the graph activity)
     */
    private class AsyncGraphTask extends Hackojo {

        private boolean first_update;

        public AsyncGraphTask(DeviceData ddata, String gn) {
            super(ddata, gn);
        }

        public void setFirstUpdate(boolean first) {

            first_update = first;
        }

        @Override
        public void onCancelled(Boolean result) {

            Log.i(this.getClass().getName(), "Operation cancelled - " + op);
            Toast.makeText(getApplicationContext(), R.string.opfail, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPostExecute(Boolean result) {

            if (result) {
                Log.i(this.getClass().getName(), "post execute - " + op + ": SUCCESS");

                if (op == DEVICE_OP) {
                    std.clearBroadcasters();
                    Log.i(this.getClass().getName(), "post execute -  devl update");
                    updateGroupContent(this, first_update);

                    if (first_update)
                        notifyDevices();

                } else if (op == QUIT_GROUP_OP) {

                    Log.i(this.getClass().getName(), "post execute - quit the group");
                    Log.i(this.getClass().getName(), "post execute - notify the devices (quit)");
                    notifyQuitDevices(username, std.getSenders().iterator());
                    notifyQuitDevices(username, std.getListeners().iterator());
                } else if (op == GRAPH_GET_OP) {

                    String s = "";
                    for (ArrayList<String> ar : paths)
                        s += ar.toString() + "\n";

                    Toast.makeText(GraphActivity.this, s, Toast.LENGTH_LONG).show();
                }

            } else
                Toast.makeText(getApplicationContext(), R.string.opfail, Toast.LENGTH_SHORT).show();
        }
    }

}
