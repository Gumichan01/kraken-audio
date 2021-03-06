package com.pl.multicast.kraken;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
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

import com.pl.multicast.kraken.audio.KrakenAudio;
import com.pl.multicast.kraken.audio.KrakenSample;
import com.pl.multicast.kraken.broadcast.KrakenBroadcast;
import com.pl.multicast.kraken.broadcast.KrakenReceiver;
import com.pl.multicast.kraken.common.Hackojo;
import com.pl.multicast.kraken.common.KrakenMisc;
import com.pl.multicast.kraken.common.NotifyTask;
import com.pl.multicast.kraken.datum.DeviceData;
import com.pl.multicast.kraken.parser.MessageParser;
import com.pl.multicast.kraken.service.KrakenBroadcastData;
import com.pl.multicast.kraken.service.KrakenService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


public class MixActivity extends Activity
        implements NavDrawer.NavigationDrawerCallbacks {

    public static final String SAMPLE_TAG = "SAMPLE";
    public static final String VERTEX_TAG = "VERTEX";
    public static final String LINES_TAG = "LINES";
    private static final int GRAPH_ARRAYLIST_SZ = 2;
    private static final char SHARP = '#';

    public static String username;
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
    private DeviceData device;
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
    private KrakenService bs;
    // Data
    private KrakenBroadcastData kbdata;      // Data broadcasting information

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix);

        /** Retrieve data from the main activity */
        idnav_selected = 0;
        device = getIntent().getParcelableExtra(MainActivity.DEVICEDATA);
        gname = getIntent().getStringExtra(MainActivity.GRPNAME);
        username = device.getName();
        mTitle = username;
        bdnames = null;
        rdnames = null;

        /** Load the broadcast data and the communication point */
        kbdata = new KrakenBroadcastData();

        /** Service server */
        bs = new KrakenService(this, kbdata);
        bserviceth = new Thread(bs);
        bserviceth.start();

        /** Broadcast */
        kbroadcast = new KrakenBroadcast(this, kbdata);
        kbroadcast.setAudioConfig(KrakenAudio.DEFAULT_SAMPLERATE, false);
        kbroadcast.launch();

        /** Display */
        TextView txv = (TextView) findViewById(R.id.text_rate);
        txv.setText("Rate: 0 bytes/s");

        Switch bswitch = (Switch) findViewById(R.id.switch_broad);
        Switch lswitch = (Switch) findViewById(R.id.switch_listen);
        Switch rswitch = (Switch) findViewById(R.id.switch_reverb);

        bswitch.setChecked(kbroadcast.getBroadcastOption());
        lswitch.setChecked(kbroadcast.getListenOption());
        rswitch.setChecked(false);

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
            new AsyncGraphTask(device, gname).execute(Hackojo.QUIT_GROUP_OP);
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

        String[] ld;

        if (idnav_selected == idnav_left)
            ld = bdnames;
        else if (idnav_selected == idnav_right)
            ld = rdnames;
        else {
            return;
        }

        switch (number) {
            case 1:
                mTitle = username;
                break;

            default:
                String name = ld[number - 1];
                if (name.charAt(name.length() - 1) == SHARP)
                    name = name.substring(0, name.length() - 1);

                mTitle = name;
                break;
        }

        if (mTitle.equals(username)) {

            bdnames = generateDisplayList(kbdata.getSenders());
            rdnames = generateDisplayList(kbdata.getListeners());
            navigationSenders.updateContent(bdnames);
            navigationReceivers.updateContent(rdnames);

        } else
            updateSection();
    }

    private void updateSection() {

        new SectionUpdateThread(new SectionUpdateRunnable()).start();
    }

    public void setIDNavSelected(int id) {

        idnav_selected = id;
    }


    /**
     * Generate sound
     */
    public void generateSound(View v) {

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

            kbroadcast.generateSound(samplerate, frequency, stereo, duration);
            // Log.i(this.getLocalClassName(), "" + samplerate + "/" + frequency + "/" + duration + ":" + stereo);
            Toast.makeText(getApplicationContext(), "Sound generated and registered", Toast.LENGTH_LONG).show();

        } else
            Log.e(this.getLocalClassName(), "Invalid button");
    }

    /**
     * Play sound
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

    public void displaylistOfSamples(View v) {

        if (v.getId() == R.id.button_playlist) {

            ArrayList<KrakenSample> ks = kbroadcast.getAudio().getSamples();

            if (ks == null || ks.isEmpty())
                Toast.makeText(getApplicationContext(), "No samples registered", Toast.LENGTH_LONG).show();
            else {

                Intent intent = new Intent(this, SampleActivity.class);
                intent.putParcelableArrayListExtra(SAMPLE_TAG, ks);
                startActivity(intent);
            }
        }
    }

    public void clearSamples(View v) {

        if (v.getId() == R.id.button_clear) {

            kbroadcast.getAudio().getSamples().clear();
            Toast.makeText(getApplicationContext(), "Cleared the samples", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Switch the reverberation effect
     */
    public void reverbEffect(View v) {

        if (v.getId() == R.id.switch_reverb) {

            Switch sw = (Switch) v;

            if (sw.isChecked()) {

                kbroadcast.getAudio().setReverbEffect(true);
                Toast.makeText(this, "reverb enabled", Toast.LENGTH_SHORT).show();
            } else {
                kbroadcast.getAudio().setReverbEffect(false);
                Toast.makeText(this, "reverb disabled", Toast.LENGTH_SHORT).show();
            }
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
        l.addAll(kbdata.getSenders());
        l.addAll(kbdata.getListeners());

        // Log.i(this.getLocalClassName(), "Notify every devices by " + username);
        notifyUpdateDevices(username, l.iterator());
    }

    private void notifyUpdateDevices(String username, Iterator<DeviceData> it) {

        notifyDevices(KrakenService.UPDATE, username, it);
    }

    private void notifyQuitDevices(String username, Iterator<DeviceData> it) {

        notifyDevices(KrakenService.QUIT, username, it);
    }


    private void notifyDevices(String hreq, String username, Iterator<DeviceData> it) {

        NotifyTask nt = new NotifyTask(hreq, username);
        nt.execute(it);
    }

    // Update the list of devices
    public void update(boolean first) {

        if (KrakenMisc.isNetworkAvailable(getApplicationContext())) {

            AsyncGraphTask async = new AsyncGraphTask(device, gname);
            async.setFirstUpdate(first);
            async.execute(Hackojo.DEVICE_OP);
            // Log.i(this.getLocalClassName(), "update devl");
        } else {
            // Log.e(this.getLocalClassName(), "Cannot update the group content - network unavailable");
            Toast.makeText(getApplicationContext(), R.string.gunetwork, Toast.LENGTH_LONG).show();
        }
    }

    private void updateGroupContent(Hackojo hack, boolean first) {

        List<DeviceData> br = hack.getDevices();
        List<DeviceData> ltl = kbdata.getListeners();
        List<DeviceData> lts = kbdata.getSenders();

        if (br == null) {
            Log.e(this.getLocalClassName(), "no device");
            return;
        }

        if (first) {
            for (DeviceData dev : br)
                kbdata.addBroadcaster(dev);
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

                if (!found) kbdata.addBroadcaster(d);
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
            if (kbdata.isRealBroadcaster(dnames[i]))
                dnames[i] += SHARP;
        }

        return dnames;
    }

    /**
     * Generate the list of the strings that will be displayed in the navigation drawers
     */
    private String[] generateStringList(List<String> ls) {

        List<String> slist = KrakenMisc.adaptStringList(ls, username);
        String[] snames = new String[slist.size()];
        slist.toArray(snames);
        return snames;
    }

    /**
     * Prepare the request getting the device data to listen
     */
    private DeviceData prepareRequest() {

        String slistener = mTitle;
        DeviceData d = kbdata.getBroadcasterOf(slistener);
        mTitle = username;
        // Log.i(this.getLocalClassName(), slistener + " | " + d.toString() + " | " + mTitle);
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
        KrakenReceiver recv = kbroadcast.getReceiver();

        if (id == R.id.action_listen) {

            if (!mTitle.equals(username)) {

                DeviceData d = prepareRequest();
                recv.listenRequest(d, username);

                Toast.makeText(getApplicationContext(), "You are listening to '" + d.getName() + "'",
                        Toast.LENGTH_LONG).show();

            } else
                Toast.makeText(getApplicationContext(), "You cannot listen to yourself, idiot!",
                        Toast.LENGTH_LONG).show();

            return true;

        } else if (id == R.id.action_stop) {

            if (!mTitle.equals(username))
                recv.stopRequest(prepareRequest(), username);
            else
                Toast.makeText(getApplicationContext(), "You cannot stop yourself, (o_O) idiot!",
                        Toast.LENGTH_LONG).show();

            return true;

        } else if (id == R.id.action_graph) {

            AsyncGraphTask async = new AsyncGraphTask(device, gname);
            async.execute(Hackojo.GRAPH_GET_OP);
        }

        return super.onOptionsItemSelected(item);
    }

    private void displayBroadcastGraph(List<ArrayList<String>> graph) {

        if(graph == null || graph.size() != GRAPH_ARRAYLIST_SZ) {

            Log.wtf(getLocalClassName(), "Cannot display the graph. MUST NEVER HAPPEN !!!");
            return;
        }

        Intent intent = new Intent(this, BroadcastGraphActivity.class);
        intent.putStringArrayListExtra(VERTEX_TAG, graph.get(0));
        intent.putStringArrayListExtra(LINES_TAG, graph.get(1));
        startActivity(intent);
    }

    public DeviceData getDevData() {

        return device;
    }

    public String getGroupName() {

        return gname;
    }

    ///
    /// Inner classes
    ///

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
            View rootView = inflater.inflate(R.layout.fragment_mix, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MixActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

    private class SectionUpdateThread extends Thread {

        public SectionUpdateThread(Runnable runnable) {
            super(runnable);
        }
    }

    private class SectionUpdateRunnable implements Runnable {

        private static final int SOCKET_WAIT = 1000;

        private DeviceData devd;
        private ArrayList<String> sbroadcasts;
        private ArrayList<String> slisteners;

        public SectionUpdateRunnable() {

            devd = null;
            sbroadcasts = new ArrayList<>();
            slisteners = new ArrayList<>();
        }

        private void requestDevice(String hreq) {

            String line;
            StringBuilder stbuild = new StringBuilder("");

            try {
                Socket s = new Socket(devd.getAddr(), devd.getPort());
                s.setSoTimeout(SOCKET_WAIT);

                PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));

                writer.write(hreq + " " + mTitle + MessageParser.EOL);
                writer.flush();

                while ((line = reader.readLine()) != null) {
                    stbuild.append(line).append(MessageParser.EOL);
                }

                Pattern p = Pattern.compile(MessageParser.EOL);
                String[] tokens = p.split(stbuild.toString());

                for (String str : tokens) {

                    MessageParser parser = new MessageParser(str);

                    if (parser.isWellParsed()) {

                        if (parser.getHeader().contains(MessageParser.SRV_DDAT)) {

                            if (hreq.equals(KrakenService.LISTB))
                                sbroadcasts.add(parser.getDevice());
                            else if (hreq.equals(KrakenService.LISTB))
                                slisteners.add(parser.getDevice());

                        } else if (parser.getHeader().contains(MessageParser.SRV_EOTR))
                            break;
                        else
                            continue;
                    }
                }

                s.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            devd = kbdata.getBroadcasterOf(mTitle);

            if (devd == null) { // It is not in the broadcasters
                devd = kbdata.getListenerOf(mTitle);

                if (devd == null) { // It is not in the broadcasters
                    Log.e(getClass().getName(), mTitle + " is not a broadcaster or a listener");
                    return;
                }
            }

            // Get the two lists
            requestDevice(KrakenService.LISTB);
            requestDevice(KrakenService.LISTL);

            // Update the navigation drawers
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    bdnames = generateStringList(sbroadcasts);
                    rdnames = generateStringList(slisteners);
                    navigationSenders.updateContent(bdnames);
                    navigationReceivers.updateContent(rdnames);
                }
            });
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
                // Log.i(this.getClass().getName(), "post execute - " + op + ": SUCCESS");

                if (op == DEVICE_OP) {
                    kbdata.clearBroadcasters();
                    // Log.i(this.getClass().getName(), "post execute -  devl update");
                    updateGroupContent(this, first_update);

                    if (first_update)
                        notifyDevices();

                } else if (op == QUIT_GROUP_OP) {

                    // Log.i(this.getClass().getName(), "post execute - quit the group");
                    // Log.i(this.getClass().getName(), "post execute - notify the devices (quit)");
                    notifyQuitDevices(username, kbdata.getSenders().iterator());
                    notifyQuitDevices(username, kbdata.getListeners().iterator());

                } else if (op == GRAPH_GET_OP) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            displayBroadcastGraph(paths);
                        }
                    });
                }

            } else
                Toast.makeText(getApplicationContext(), R.string.opfail, Toast.LENGTH_SHORT).show();
        }
    }

}
