package com.pl.multicast.kraken;

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
        String[] items = getArguments().getStringArray(NAMES);

        // Title
        builder.setTitle(R.string.avgrp);
        // Cancel button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Log.i(this.getClass().getName(), "Cancel the group");
            }
        });

        // TODO: 06/02/2017 Retrieve the list of groups in order to send it into the dialog
        if (items == null)
            builder.setMessage(R.string.nogrp);
        else {

            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which) {
                        case 0:
                            Log.i(this.getClass().getName(), "Read");
                            break;

                        case 1:
                            Log.i(this.getClass().getName(), "Write");
                            break;

                        case 2:
                            Log.i(this.getClass().getName(), "Delete");
                            break;

                        case 3:
                            Log.i(this.getClass().getName(), "Create");
                            break;
                    }
                }
            });
        }

        return builder.create();
    }

}
