package com.pl.multicast.kraken;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Luxon on 06/02/2017.
 */
public class JoinGroupDialogFragment extends DialogFragment {

    private static final String NAMES = "NAMES";

    public interface JoinGroupDialogListener {

        public void onItemSelected(DialogInterface dialog, String gname);
    }


    JoinGroupDialogListener jlistener;

    public static JoinGroupDialogFragment newInstance(String[] names) {

        JoinGroupDialogFragment f = new JoinGroupDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray(NAMES, names);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle saveInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final String[] items = getArguments().getStringArray(NAMES);

        // Title
        builder.setTitle(R.string.avgrp);

        if (items == null) {

            // OK button
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Log.i(this.getClass().getName(), "Cancel the group");
                }
            });

            builder.setMessage(R.string.nogrp);
            Log.i(this.getClass().getName(), "No group");

        } else {

            // Cancel button
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Log.i(this.getClass().getName(), "Cancel the group");
                }
            });

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // TODO: 06/02/2017 JoinGroupDialogListener
                    jlistener.onItemSelected(dialog, items[which]);
                }
            });

            Log.i(this.getClass().getName(), "There are " + items.length + "active groups");
        }

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        try {

            jlistener = (JoinGroupDialogListener) activity;

        } catch (ClassCastException ce) {
            throw new ClassCastException(activity.toString() + "must implement JoinGroupDialogListener");
        }

    }

}
