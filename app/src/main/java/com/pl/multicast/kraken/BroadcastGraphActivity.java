package com.pl.multicast.kraken;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class BroadcastGraphActivity extends Activity {

    private static final int NB_TOKENS = 2;
    private static final String ARROW = " -> ";
    private static final String SEP = " ";
    private ArrayList<String> vertices_strlist;
    private ArrayList<String> lines_strlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_graph);

        vertices_strlist = getIntent().getStringArrayListExtra(MixActivity.VERTEX_TAG);
        lines_strlist = getIntent().getStringArrayListExtra(MixActivity.LINES_TAG);

        Log.i(getClass().getName(), vertices_strlist.toString());
        Log.i(getClass().getName(), lines_strlist.toString());

        ListView lvertexv = (ListView) findViewById(R.id.list_vertices);
        ListView llinesv = (ListView) findViewById(R.id.list_lines);

        lvertexv.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.sample_view, vertices_strlist));
        llinesv.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.lines_view,
                transformLines(lines_strlist)));

        lvertexv.setVisibility(View.VISIBLE);
        llinesv.setVisibility(View.VISIBLE);
    }


    private ArrayList<String> transformLines(ArrayList<String> lines_strlist) {

        if (lines_strlist.isEmpty())
            return lines_strlist;

        ArrayList<String> nlist = new ArrayList<>();

        for (String s : lines_strlist) {

            StringBuilder stbuild = new StringBuilder("");
            String[] tokens = s.split(SEP);

            if (tokens.length == NB_TOKENS) {
                stbuild.append(tokens[0]).append(ARROW).append(tokens[1]);
            }
            nlist.add(stbuild.toString());
        }

        return nlist;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_broadcast_graph, menu);
        return true;
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
}
