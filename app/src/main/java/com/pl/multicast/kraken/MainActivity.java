package com.pl.multicast.kraken;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final String USRNAME = "USRNAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    public void mix(View v) {

        int id = v.getId();
        EditText tv = (EditText) findViewById(R.id.usr);

        if (tv == null)
            Log.e(this.getLocalClassName(), "Internal error - usr: no edit text");
        else {

            String s = tv.getText().toString();

            if (s.isEmpty())
                Toast.makeText(this, "Empty string", Toast.LENGTH_SHORT).show();
            else {

                Intent intent = new Intent(this, GraphActivity.class);

                if (id == R.id.cgrp) {

                    EditText gtv = (EditText) findViewById(R.id.grp);

                    if (gtv == null || gtv.getText().toString().isEmpty())
                        Toast.makeText(this, "Empty string\n In order to create a group, you must speciify the name",
                                Toast.LENGTH_LONG).show();
                        //Log.e(this.getLocalClassName(), "Internal error - grp: no edit text");
                    else {
                        // TODO: 05/02/2017 create group in the directory server
                    }

                } else if (id == R.id.jgrp) {

                    // TODO: 05/02/2017 join a group in the server
                }

                //intent.putExtra(USRNAME, s);
                //startActivity(intent);
            }
        }

        if (id == R.id.cgrp)
            Log.i(this.getLocalClassName(), "cgrp");
        else if (id == R.id.jgrp)
            Log.i(this.getLocalClassName(), "jgrp");
    }
}
