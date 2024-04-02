package com.example.fiftysix;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessaging extends FirebaseMessagingService {

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
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(message)
                    .build();
            nm.notify(100,notification);
        }
    }
}
