package com.davidepetti.geoclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class PrivacyDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("PRIVACY POLICY")
                .setMessage(R.string.privacy_message)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Toast.makeText(getActivity(), "Good choice", Toast.LENGTH_SHORT).show();
                        SharedPreferences sharedPref = getContext().getSharedPreferences("com.davidepetti.geoclient.DIALOG_PREFERENCE_FILE", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("agreed", true);
                        editor.commit();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        SharedPreferences sharedPref = getContext().getSharedPreferences("com.davidepetti.geoclient.DIALOG_PREFERENCE_FILE", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("agreed", false);
                        editor.commit();
                        getActivity().finish();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        SharedPreferences sharedPref = getContext().getSharedPreferences("com.davidepetti.geoclient.DIALOG_PREFERENCE_FILE", Context.MODE_PRIVATE);
        boolean agreed = sharedPref.getBoolean("agreed",false);

        if (!agreed) {
            getActivity().finish();
        }
    }
}
