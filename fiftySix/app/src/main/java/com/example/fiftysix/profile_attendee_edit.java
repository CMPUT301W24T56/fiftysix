package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class profile_attendee_edit extends AppCompatActivity {
    private ImageButton btnImage;
    private Button btnBack, btnSave;
    private EditText edEmail, edPhone, edName, edHome;
    private DocumentReference dr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_attendee);

        btnImage = findViewById(R.id.profile_image);
        btnBack  = findViewById(R.id.profile_back);
        btnSave  = findViewById(R.id.profile_save);
        edName   = findViewById(R.id.profile_name);
        edEmail  = findViewById(R.id.profile_email);
        edPhone  = findViewById(R.id.profile_phone);
        edHome   = findViewById(R.id.profile_home);

        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        dr = FirebaseFirestore.getInstance().collection("Users").document(id);
        load();

        btnImage.setOnClickListener(v ->
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100));
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());
    }

    private void load() {
        dr.get().addOnCompleteListener(task -> {
            String name, email, phone, home;

            if (!task.isSuccessful()) {
                Log.d(TAG, "Failed with: ", task.getException());
                return;
            }

            Map m = task.getResult().getData();
            if (m == null) {
                // user has nothing saved
                return;
            }

            if ((name  = (String) m.get("name")) != null)
                edName.setText(name);
            if ((email = (String) m.get("email")) != null)
                edEmail.setText(email);
            if ((phone = (String) m.get("phone")) != null)
                edPhone.setText(phone);
            if ((home  = (String) m.get("home")) != null)
                edHome.setText(home);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras;
        Bitmap photo;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 100 || resultCode != RESULT_OK || data == null)
            return;
        if ((extras = data.getExtras()) == null)
            return;
        if ((photo = (Bitmap) extras.get("data")) == null)
            return;
        btnImage.setImageBitmap(photo);
    }

    private void save() {
        String name, email, phone, home;
        name  = edName.getText().toString();
        email = edEmail.getText().toString();
        phone = edPhone.getText().toString();
        home  = edHome.getText().toString();
        Map<String, Object> m = Map.of(
                "name",  name,
                "email", email,
                "phone", phone,
                "home",  home
        );
        dr.set(m)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Firestore update successful for profile");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Firestore update failed for poster");
                });
    }

    // Miserably rescued from CheckInQRCode.java; TODO put back
    private Uri img2uri(Bitmap img) {
        if (img == null)
            return null;
        img.compress(Bitmap.CompressFormat.JPEG, 90, new ByteArrayOutputStream());
        return Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),
                img, "tmporalis", null));
    }

    private Bitmap uri2img(Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (Exception ignore) {
            return null;
        }
    }
}
