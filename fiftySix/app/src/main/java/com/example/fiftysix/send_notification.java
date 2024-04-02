package com.example.fiftysix;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

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
        String channelId = "Channel_ID_Notification";
        String topic = "event_" + eventId;

        Map<String, String> notificationData = new HashMap<>();
        notificationData.put("title", eventName);
        notificationData.put("message", message);

        // See documentation on defining a message payload.
        RemoteMessage send_message = new RemoteMessage.Builder(topic)
                .setData(notificationData)
                .build();

// Send a message to the devices subscribed to the provided topic.
        FirebaseMessaging.getInstance().send(send_message);
        Log.d("FCM-notification","message successfully send ");
    }



}
