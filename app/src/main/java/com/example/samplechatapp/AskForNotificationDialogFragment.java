package com.example.samplechatapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.samplechatapp.R;

public class AskForNotificationDialogFragment extends DialogFragment {
    // private Bundle contentBundle = getArguments();
    // private String mErrorMessage = contentBundle.getString("errorMessage", "not found");

    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        final SharedPreferences chatRoomsPref = this.getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.notification_questionmark))
                .setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id){
                        if (getArguments() != null) {
                            if (getArguments().getString("roomName", null) != null) {
                                SharedPreferences.Editor editor = chatRoomsPref.edit();
                                editor.putBoolean(getArguments().getString("roomName"), true);
                                editor.commit();
                            }
                        }
                    }})
                .setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id){
                        if (getArguments() != null) {
                            if (getArguments().getString("roomName", null) != null) {
                                SharedPreferences.Editor editor = chatRoomsPref.edit();
                                editor.putBoolean(getArguments().getString("roomName"), false);
                                editor.commit();
                            }
                        }
                    }});
        return builder.create();
    }
}