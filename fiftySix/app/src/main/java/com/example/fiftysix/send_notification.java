package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.NotificationUtils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.checkerframework.checker.units.qual.N;

import java.util.HashMap;
import java.util.Map;

public class send_notification extends AppCompatActivity {
    private ImageButton cancel, send;
    private EditText message;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("TAG", "onCreate: not working ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        message = findViewById(R.id.message);
        send = findViewById(R.id.send_button);

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(send_notification.this,new String[]{android.Manifest
                        .permission.POST_NOTIFICATIONS},101);

            }

        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sending_message = message.getText().toString();
                notify_attendee(sending_message);
                finish();
            }
        });
    }

    public void notify_attendee(String message) {
        Bundle extras = getIntent().getExtras();
        String eventID, eventName;

        // Retrieve the eventID from the Intent extras
        if (extras != null) {
            eventID = extras.getString("eventID");
            eventName = extras.getString("eventName");
            sendNotificationToEventAttendees(eventID, eventName, message);
        }
    }

    private void sendNotificationToEventAttendees(String eventId, String eventName, String message) {
        Notification notification;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notification = new Notification.Builder(this)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle(eventName)
                                        .setContentText(message)
                                         .setChannelId(eventId)
                                         .build();
            nm.createNotificationChannel(new NotificationChannel("event_" + eventId,eventName,NotificationManager.IMPORTANCE_HIGH));

        }else {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(eventName)
                    .setContentText(message)
                    .build();
        }
        nm.notify(100,notification);
        Log.d("FCM-notification","message successfully send ");

    }



}
