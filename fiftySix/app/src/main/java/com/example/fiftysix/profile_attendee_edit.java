package com.example.fiftysix;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class profile_attendee_edit extends AppCompatActivity {
    private ImageButton btnImage;
    private Button btnBack, btnSave;
    private EditText edEmail, edPhone, edName, edHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_attendee);

        set_profile();

        btnImage = findViewById(R.id.profile_image);
        btnBack  = findViewById(R.id.profile_back);
        btnSave  = findViewById(R.id.profile_save);
        edName   = findViewById(R.id.profile_name);
        edEmail  = findViewById(R.id.profile_email);
        edPhone  = findViewById(R.id.profile_phone);
        edHome   = findViewById(R.id.profile_home);

        btnImage.setOnClickListener(v ->
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100));
        btnBack.setOnClickListener(v -> finish());
        btnSave = btnSave; // TODO
    }

    // Gets android ID to be used as organizer ID
    // https://stackoverflow.com/q/60503568

    public void set_profile() {
        // setting the profile from the database to the app
        //  we need to get the id first of the user to identify it.
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap photo = (Bitmap) extras.get("data");
                if (photo != null)
                    btnImage.setImageBitmap(photo);
            }
        }
    }
}
