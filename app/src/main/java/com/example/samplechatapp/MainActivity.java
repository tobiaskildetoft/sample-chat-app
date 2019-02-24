/*
 * Sample Chat App for application at House of Code.
 * MainActivity will host a list of chatrooms to be fetched from Firebase database.
 * Main entry point for the app from a launcher will be the SplashScreen Activity which
 * displays a splash screen while MainActivity loads, in order to allow fade between the two.
 */

package com.example.samplechatapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.OrderBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Constants for activity results
    private final static int RC_SIGN_IN = 1;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    // member variables for the Views in the layout
    private SwipeRefreshLayout mSwipeRefresh;
    private ListView mChatRoomsList;

    private ChatRoomAdapter mChatRoomAdapter;


    // Private methods
    private void refresh() {
        // TODO: Make this feel less like it refreshes everything and make it just update the list

        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }


    // member variables for Firebase objects
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate Views
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mChatRoomsList = (ListView) findViewById(R.id.chatRoomListView);

        // Add adapter to the list of chat rooms
        List<ChatRoomInfo> chatRooms = new ArrayList<>();
        mChatRoomAdapter = new ChatRoomAdapter(this, R.layout.chat_room_info, chatRooms);
        mChatRoomsList.setAdapter(new ChatRoomAdapter(this, R.layout.chat_room_info, chatRooms));

        // Add onClick functionality to the list items
        mChatRoomsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRoomInfo chatRoomInfo = mChatRoomAdapter.getItem(position);
                Intent intent = new Intent(getBaseContext(), ChatRoomActivity.class);
                intent.putExtra("chatRoomName", chatRoomInfo.getName());
                startActivity(intent);
            }
        });


        // Set up the listener that refreshes the view when user swipes
        mSwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );

        // Create a listener to check whether the user is signed in
        // If not signed in, display a sign in page
        // This will be attached in onResume to also check when user returns from elsewhere
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is logged in
                }
                else {
                    // User is not logged in
                    mChatRoomAdapter.clear();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.FacebookBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        // Initialize an instance of FirebaseAuth to which the listener can be attached
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Instantiate the database
        mDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_refresh:
                // Refresh the page
                refresh();
                return true;
            case R.id.menu_logout:
                // Sign out the user
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.welcome), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            } else {
                showError();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Add the listener to check whether user is signed in.
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }

        // Then populate the list of chat rooms from the database, ordered by timestamp
        mDatabase.collection("chatrooms")
                .orderBy("timestamp", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mChatRoomAdapter.clear();
                for (ChatRoomInfo chatRoom : queryDocumentSnapshots.toObjects(ChatRoomInfo.class)) {
                    mChatRoomAdapter.add(chatRoom);
                }
                // Reattach the adapter to force the View to update.
                mChatRoomsList.setAdapter(mChatRoomAdapter);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove the AuthState listener if it exists.
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    public void showError() {
        DialogFragment dialog = new ErrorMessageFragment();
        dialog.show(getSupportFragmentManager(), "ErrorMessage");
    }
}
