package com.pl.multicast.kraken;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pl.multicast.kraken.audio.KrakenSample;

import java.util.ArrayList;

public class SampleActivity extends Activity {

    private ArrayList<KrakenSample> slist;
    private ArrayList<String> tlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        tlist = new ArrayList<>();
        slist = getIntent().getParcelableArrayListExtra(GraphActivity.SAMPLE_TAG);

        if (slist == null || slist.isEmpty())
            Log.v(getLocalClassName(), "empty list");
        else {

            ListView lview = (ListView) findViewById(R.id.list_samples);

            if (lview == null) {
                Log.e(getLocalClassName(), "empty view");
                return;
            }

            Log.i(getLocalClassName(), "DISPLAY LIST");
            for (KrakenSample ks : slist) {
                Log.i(getLocalClassName(), ks.toString());
                tlist.add(ks.toString());
            }
            Log.i(getLocalClassName(), "DISPLAY LIST DONE");

            lview.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.sample_view, tlist));
            lview.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sample, menu);
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
