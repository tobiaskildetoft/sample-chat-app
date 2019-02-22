package com.example.samplechatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Just start the actual main activity
        startActivity(new Intent(this, MainActivity.class));
        // Close the splash screen again
        finish();
    }
}
