package com.example.fiftysix;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AttendeeMainActivity extends AppCompatActivity {

    private ImageButton profile_button;
    private ImageButton notification_button;
    private ImageButton qrcode_button;
    private ImageButton home_button;

    private SearchView searchView;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onClick:   not working ");
        setContentView(R.layout.activity_attendee_main);

        profile_button = findViewById(R.id.attendee_profile);
        qrcode_button = findViewById(R.id.qr_code_button);
        notification_button = findViewById(R.id.notification_button);
        home_button = findViewById(R.id.button_attendee_home);

        profile_button.setOnClickListener(v -> {
            // Uncomment and replace profile_attendee_edit with the correct activity class+
            startActivity(new Intent(AttendeeMainActivity.this, profile_attendee_edit.class));
        });

        qrcode_button.setOnClickListener(v -> {
            // Uncomment and replace PromotionQRCode with the correct activity class
            IntentIntegrator intentIntegrator = new IntentIntegrator(AttendeeMainActivity.this);
            intentIntegrator.setOrientationLocked(true);
            intentIntegrator.setPrompt("Scan a QR Code");
            intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
            intentIntegrator.initiateScan();
        });

        notification_button.setOnClickListener(v -> {
            startActivity(new Intent(AttendeeMainActivity.this, Notification.class));
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null)
                textView.setText(contents);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
