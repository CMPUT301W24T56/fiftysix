package com.example.fiftysix;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    private String id;
    private FirebaseFirestore db;

    public MyFirebaseMessaging(String attendeeid) {
        this.id = attendeeid;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle incoming message
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (remoteMessage.getData().size() > 0) {
            // Process data payload
            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            Log.d("message_received","message_receiver" + message);
            // Display notification

        }
    }
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Handle the new token here
        Log.d("New Token", token);
        // You can send the token to your server or perform any other action.
        // update the token to the users document.
        Map<String,String> token_object = new HashMap<>();
        token_object.put("token",token);
        db.collection("Users").document(id).set(token_object);
    }

}
