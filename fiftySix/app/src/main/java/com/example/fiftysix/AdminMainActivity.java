package com.example.fiftysix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.FirebaseApp;

/**
 * Initial activity for admin to select what they'd like to browse, launches respective activity.
 * @author Rakshit, Bruce.
 * @version 1
 * @since SDK34
 */
public class AdminMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_options);
        FirebaseApp.initializeApp(this);

        findViewById(R.id.browseEvents).setOnClickListener(v ->
            startActivity(new Intent(AdminMainActivity.this, AdminBrowseEvents.class)));
        findViewById(R.id.browseProfiles).setOnClickListener(v ->
            startActivity(new Intent(AdminMainActivity.this, AdminBrowseProfiles.class)));
        findViewById(R.id.browseImages).setOnClickListener(v ->
            startActivity(new Intent(AdminMainActivity.this, AdminBrowseImages.class)));
        findViewById(R.id.buttonAdminBack).setOnClickListener(v ->
                finish());
    }
}