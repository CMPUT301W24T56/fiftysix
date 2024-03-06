package com.example.fiftysix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;

public class AttendeeMainActivity extends AppCompatActivity {

    private ImageButton profile_button;
    private ImageButton notification_button;
    private ImageButton qrcode_button;
    private ImageButton home_button;

    private SearchView serachview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendee_main);


        profile_button = findViewById(R.id.attendee_profile);
        profile_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeMainActivity.this, profile_attendee_edit.class);
                startActivity(intent);
            }
        });
        qrcode_button = findViewById(R.id.qr_code_button);

        qrcode_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AttendeeMainActivity.this,PromotionQRCode.class);
                startActivity(intent);
            }
        });









    }
}