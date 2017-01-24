package com.pl.multicast.kraken;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import datum.DeviceData;
import datum.GroupData;


public class GraphActivity extends Activity
        implements NavDrawer.NavigationDrawerCallbacks {

    private static ArrayList<String> ltext = new ArrayList<>();
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavDrawer navigationSenders;
    private NavDrawer navigationReceivers;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private String mTitle;
    private String username;

    // Threads
    private RepositoryConnection nt;
    private UDPSender st;
    private Thread bservice;    // broadcast service

    // Data
    private BroadcastData std;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Set the attributes
        username = getIntent().getStringExtra(MainActivity.USRNAME) + "@" + Build.MODEL;
        mTitle = username;

        nt = new RepositoryConnection(username, "192.168.43.114", 2408, 2409);
        nt.setOp(RepositoryConnection.GROUP_OP);    /* Get the groups */
        new Thread(nt).start();

        std = new BroadcastData();
        st = new UDPSender(std);
        st.start();

        bservice = new Thread(new BroadcastService(this, std));
        bservice.start();

        // Fragment creation
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

        // Get the list of groups
        // @// TODO: 18/01/2017 Display the groups in a box; handle the selection and the registering
        List<GroupData> g = nt.getGroups();
        Log.i("GROUP_DEV", "OK get groups done");

        if (g != null) {

            Iterator<GroupData> it = g.iterator();

            while (it.hasNext()) {
                Log.i("GROUP_DEV", it.next().toString());
            }
        } else
            Log.i("GROUP_DEV", "empty group");

        /// ONLY FOR TESTING THE BROADCAST
        nt.setOp(RepositoryConnection.DEVICE_OP);
        nt.setGroupName("test");
        new Thread(nt).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException i) {
            i.printStackTrace();
        }

        List<DeviceData> ld = nt.getDevices();

        if (ld != null) {

            Iterator<DeviceData> it = ld.iterator();

            while (it.hasNext()) {
                DeviceData dd = it.next();
                std.addSender(dd);
                //std.addListener(dd);
            }

            ld.add(0, new DeviceData(username, "", 0, 0));
            navigationSenders.updateContent(ld.toArray());
            //navigationReceivers.updateContent(ld.toArray());

        } else
            Log.i("GROUP_CONTENT", "no device");

        UDPReceiver udpr = new UDPReceiver(this, std);
        udpr.launchReceiver();
        //udpr.sendMessage(new DeviceData("toto", "192.168.43.222", 2408, 2409), "LISTEN gt-i8190n\r\n");
        //udpr.sendMessage(new DeviceData("toto", "192.168.43.1", 2408, 2409), "LISTEN kenny\r\n");
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
        Log.i("GROUP", "Stop the activity");
        std.stopServer();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void update() {

        List<DeviceData> ls = std.getSenders();
        List<DeviceData> ll = std.getListeners();

        ls.add(0, new DeviceData(username, "", 0, 0));
        ll.add(0, new DeviceData(username, "", 0, 0));

        navigationSenders.updateContent(ls.toArray());
        navigationReceivers.updateContent(ll.toArray());
    }

    public String getUSR() {
        return username;
    }


    public void onSectionAttached(int number) {

        List<DeviceData> ld = nt.getDevices();

        switch (number) {
            case 1:
                mTitle = username;
                break;

            default:
                mTitle = ld.get(number - 1).getName();
                break;
        }
    }


    /**
     * Button action
     **/
    public void broadcastText(View v) {

        EditText edt = (EditText) findViewById(R.id.txtsend);
        String s = edt.getText().toString();
        Log.i("GROUP", "Send the following text: " + s);
        std.setText(s);
        Log.i("GROUP", "Send text END");

        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void receiveText(String text){

        ListView lstv = (ListView) findViewById(R.id.txtrecv);
        ltext.add(text);
        lstv.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.text_recv, ltext));
        lstv.setVisibility(View.VISIBLE);
        Log.i("GROUP", "List updated. Added the following text: " + text);
    }

    /**
     * Action Bar
     **/

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
     **/

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
