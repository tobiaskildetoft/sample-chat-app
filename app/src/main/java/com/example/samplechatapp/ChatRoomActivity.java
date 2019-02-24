package com.example.samplechatapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;

public class ChatRoomActivity extends AppCompatActivity {

    // Constants for activity results
    private final static int RC_SIGN_IN = 1;
    private static final int RC_PHOTO_PICKER = 2;

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 500;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private boolean hasPermission;
    private boolean hasAskedPermission;

    private String mChatRoomName;
    private String mUsername;
    private String mUserAvatarUrl;

    // Member variables for Views
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    // Member variables for Firebase objects
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseFirestore mDatabase;
    private EventListener<QuerySnapshot> mDatabaseListener;
    private ListenerRegistration mListenerRegistration;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        mUsername = "Anonymous";

        mChatRoomName = getIntent().getExtras().getString("chatRoomName", null);
        if (mChatRoomName == null) {
            // Got sent here with wrong intent
            finish();
        }

        // Instantiate Views
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Add adapter to the message list
        List<ChatMessage> chatMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, chatMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time = System.currentTimeMillis();
                ChatMessage chatMessage = new ChatMessage(mMessageEditText.getText().toString(),
                        mUsername, time, mUserAvatarUrl, null);
                mDatabase.collection("chatrooms")
                        .document(mChatRoomName)
                        .collection("messages").add(chatMessage);
                mDatabase.collection("chatrooms")
                        .document(mChatRoomName).update("timestamp", time);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent localPhotoIntent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                Intent chooserIntent = Intent.createChooser(localPhotoIntent,
                        getString(R.string.pick_image_using));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });
                startActivityForResult(chooserIntent, RC_PHOTO_PICKER);
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();

        // Create a listener to check whether the user is signed in
        // If not signed in, display a sign in page
        // This will be attached in onResume to also check when user returns from elsewhere
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is logged in
                    onSignedInInitialize(user.getDisplayName(), user.getPhotoUrl());
                }
                else {
                    // User is not logged in
                    onSignedOutCleanup();
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

        mDatabase = FirebaseFirestore.getInstance();

        mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri == null) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    selectedImageUri = uriFromImage(this, (Bitmap) data.getExtras().get("data"));
                }
            }
            if (selectedImageUri != null) {
                final StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
                photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                long time = System.currentTimeMillis();
                                ChatMessage chatMessage = new ChatMessage(null, mUsername,
                                        time, mUserAvatarUrl, uri.toString());
                                mDatabase.collection("chatrooms")
                                        .document(mChatRoomName)
                                        .collection("messages").add(chatMessage);
                                mDatabase.collection("chatrooms")
                                        .document(mChatRoomName).update("timestamp", time);
                            }
                        });
                    }
                });
            }
        }
    }

    public Uri uriFromImage(Context inContext, Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), image, "Title", null);
        return Uri.parse(path);
    }

    private void onSignedInInitialize(String displayName, Uri photoUrl) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
        mUsername = displayName;
        if (photoUrl != null) {
            mUserAvatarUrl = photoUrl.toString();
        }
        attachDatabaseListener();
    }

    private void attachDatabaseListener() {
        if (mDatabaseListener == null) {
            mDatabaseListener = new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if (e == null) {
                        // No errors, go ahead and use result
                        for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (dc.getType() == ADDED) {
                                ChatMessage chatMessage = dc.getDocument().toObject(ChatMessage.class);
                                boolean exists = false;
                                // Check if the message has already been added to the list.
                                // Only compares user and timestamp as same user should not be able to post
                                // two message with same timestamp anyway.
                                for (int i = mMessageAdapter.getCount() - 1; i >= 0; i--) {
                                    if (mMessageAdapter.getItem(i).getTimestamp() > chatMessage.getTimestamp()) {
                                        break;
                                    } else if (mMessageAdapter.getItem(i).getTimestamp() == chatMessage.getTimestamp()
                                            && mMessageAdapter.getItem(i).getName() == chatMessage.getName()) {
                                        exists = true;
                                    }
                                }
                                if (!exists) {
                                    mMessageAdapter.add(chatMessage);
                                }


                            }
                        }
                    }
                }
            };
        }
        ListenerRegistration mListenerRegistration =  mDatabase.collection("chatrooms")
                .document(mChatRoomName).collection("messages").orderBy("timestamp")
                .addSnapshotListener(mDatabaseListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_room, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_logout:
                // Sign out the user
                onSignedOutCleanup();
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void onSignedOutCleanup() {
        mUsername = "Anonymous";
        mUserAvatarUrl = null;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    private void detachDatabaseReadListener() {
        if (mListenerRegistration != null) {
            mListenerRegistration.remove();
            mListenerRegistration = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        detachDatabaseReadListener();
        mMessageAdapter.clear();
    }
}
