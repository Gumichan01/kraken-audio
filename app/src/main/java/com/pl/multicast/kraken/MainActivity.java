package com.pl.multicast.kraken;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

import datum.*;
import clt.*;

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
        Intent intent = new Intent(this, GraphActivity.class);

        EditText tv = (EditText) findViewById(R.id.usr);
        String s = tv.getText().toString();

        if(s.isEmpty())
            Toast.makeText(this, "Empty string", Toast.LENGTH_SHORT).show();
        else {
            intent.putExtra(USRNAME, s);
            startActivity(intent);
        }
    }
}
