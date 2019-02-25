package com.example.samplechatapp;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;

/**
 * Service to check for new messages and send push notifications
 */
public class NotificationService extends IntentService {

    private FirebaseFirestore mDatabase;
    private List<com.google.firebase.firestore.EventListener<QuerySnapshot>> mDatabaseListeners = new ArrayList<>();
    // private List<ListenerRegistration> mListenerRegistrations = new ArrayList<>();

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDatabase = FirebaseFirestore.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences chatRoomsPref = this.getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        final SharedPreferences timeExited = this.getSharedPreferences(
                getString(R.string.time_exited_key), Context.MODE_PRIVATE);
        mDatabaseListeners.clear();
        // mListenerRegistrations.clear();
        EventListener<QuerySnapshot> dummyListener;
        // ListenerRegistration dummyRegistration;
        for (final String room: chatRoomsPref.getAll().keySet()) {
            if (chatRoomsPref.getBoolean(room, false)) {
                createNotificationChannel(room);
                mDatabase.collection("chatrooms")
                        .document(room).collection("messages")
                        .addSnapshotListener(new com.google.firebase.firestore.EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                if (e == null) {
                                    // No errors, go ahead and use result
                                    for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                                        if (dc.getType() == ADDED) {
                                            ChatMessage chatMessage = dc.getDocument().toObject(ChatMessage.class);
                                            if (!MainActivity.getIsActive() && !ChatRoomActivity.getIsActive()) {
                                                if (chatMessage.getTimestamp() > timeExited.getLong("timeExited", -1)) {
                                                    notifyNewMessage(room);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    public void notifyNewMessage(String room) {

        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatRoomName", room);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, room)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.new_message))
                .setContentText(room)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(room.hashCode(), builder.build());

    }

    private void createNotificationChannel(String room) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(room, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        //for (ListenerRegistration registration: mListenerRegistrations) {
        //    registration.remove();
        //}
        super.onDestroy();
    }
}
