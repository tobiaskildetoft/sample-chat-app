package com.example.samplechatapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
/*
* Dialog that asks whether user would like to receive notifications from the calling chatroom
 */
public class AskForNotificationDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        // Shared preference with list of room names with either "true" or "false"
        // Depending on whether user wishes notifications from that room
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