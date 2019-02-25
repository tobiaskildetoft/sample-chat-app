package com.example.samplechatapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
/*
* Display an error message if log in fails
 */
public class ErrorMessageFragment extends DialogFragment {
    // private Bundle contentBundle = getArguments();
    // private String mErrorMessage = contentBundle.getString("errorMessage", "not found");

    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.log_in_error_message))
                .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id){ }});
        return builder.create();
    }
}