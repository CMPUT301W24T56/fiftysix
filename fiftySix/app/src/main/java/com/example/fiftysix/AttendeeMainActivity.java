package com.example.fiftysix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;

public class AttendeeMainActivity extends AppCompatActivity {

    private ImageButton profile_button;
    private ImageButton notification_button;
    private ImageButton qrcode_button;
    private ImageButton home_button;

    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onClick:   not working ");
        setContentView(R.layout.activity_attendee_main);


        profile_button = findViewById(R.id.attendee_profile);
        qrcode_button = findViewById(R.id.qr_code_button);
        notification_button = findViewById(R.id.notification_button);
        home_button = findViewById(R.id.button_attendee_home);
        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Uncomment and replace profile_attendee_edit with the correct activity class
                 Intent intent = new Intent(AttendeeMainActivity.this, profile_attendee_edit.class);
                 startActivity(intent);
            }
        });

        qrcode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Uncomment and replace PromotionQRCode with the correct activity class
                 Intent intent = new Intent(AttendeeMainActivity.this, PromotionQRCode.class);
                 startActivity(intent);
            }
        }); 

        notification_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeMainActivity.this, Notification.class);
                startActivity(intent);
            }
        });
    }
}
