package com.example.fiftysix;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class send_notification extends AppCompatActivity {
    private ImageButton cancel, send;
    private EditText message;
    private Context context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Log.d("TAG", "onCreate: not working ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);
        context = this;
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
            Intent serviceIntent = new Intent(this, notification.class);
            startService(serviceIntent);
            notification nm = new notification();
            nm.sendNotificationToEventAttendees(eventID, eventName, message,context);
        }
        finish();
    }


}

