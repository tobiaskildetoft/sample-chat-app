/*
 * Sample Chat App for application at House of Code.
 * MainActivity will host a list of chatrooms to be fetched from Firebase database.
 * Main entry point for the app from a launcher will be the SplashScreen Activity which
 * displays a splash screen while MainActivity loads, in order to allow fade between the two.
 */

package com.example.samplechatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // member variables for the Views in the layout
    private SwipeRefreshLayout mSwipeRefresh;
    private ListView mChatRoomsList;

    private ChatRoomAdapter mChatRoomAdapter;


    // Methods
    private void refresh() {
        // TODO: Make this feel less like it refreshes everything and make it just update the list
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }


    // member variables for Firebase objects

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Instantiate Views
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mChatRoomsList = (ListView) findViewById(R.id.chatRoomListView);

        // Add adapter to the lidt of chat rooms
        List<ChatRoomInfo> chatRooms = new ArrayList<>();
        mChatRoomAdapter = new ChatRoomAdapter(this, R.layout.chat_room_info, chatRooms);
        mChatRoomsList.setAdapter(new ChatRoomAdapter(this, R.layout.chat_room_info, chatRooms));

        // Add chatrooms for testing
        mChatRoomAdapter.add(new ChatRoomInfo("Main Room", "today"));
        mChatRoomAdapter.add(new ChatRoomInfo("Extra Room", "yesterday"));


        // Set up the listener that refreshes the view when user swipes
        mSwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                }
        );
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

        switch (id) {
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
