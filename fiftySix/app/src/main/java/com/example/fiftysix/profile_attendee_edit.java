package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class profile_attendee_edit extends AppCompatActivity {
    private ImageButton btnImage;
    private Button btnBack, btnSave, btnNoimg;
    private EditText edEmail, edPhone, edName, edHome;
    private DocumentReference dr;
    private String id;
    private Map<String, Object> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_attendee);

        btnImage = findViewById(R.id.profile_image);
        btnBack  = findViewById(R.id.profile_back);
        btnSave  = findViewById(R.id.profile_save);
        btnNoimg = findViewById(R.id.profile_noimg);
        edName   = findViewById(R.id.profile_name);
        edEmail  = findViewById(R.id.profile_email);
        edPhone  = findViewById(R.id.profile_phone);
        edHome   = findViewById(R.id.profile_home);

        id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        dr = FirebaseFirestore.getInstance().collection("Users").document(id);
        load();

        btnImage.setOnClickListener(v ->
                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100));
        btnBack.setOnClickListener(v -> finish());
        btnNoimg.setOnClickListener(v -> {
            this.data.put("image", null);
            saveProfile();
        });
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void load() {
        dr.get().addOnCompleteListener(task -> {
            String name, email, phone, home, image;

            if (!task.isSuccessful()) {
                Log.d(TAG, "Failed with: ", task.getException());
                return;
            }

            if ((data = task.getResult().getData()) == null) {
                // user has nothing saved
                return;
            }

            if ((image = (String) data.get("image")) != null)
                try {
                    byte []b = Base64.decode(image, Base64.DEFAULT);
                    btnImage.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
                } catch (Exception e) {
                    // there is no pfp
                }
            if ((name  = (String) data.get("name")) != null)
                edName.setText(name);
            if ((email = (String) data.get("email")) != null)
                edEmail.setText(email);
            if ((phone = (String) data.get("phone")) != null)
                edPhone.setText(phone);
            if ((home  = (String) data.get("home")) != null)
                edHome.setText(home);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bundle extras;
        Bitmap img;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 100 || resultCode != RESULT_OK || data == null)
            return;
        if ((extras = data.getExtras()) == null)
            return;
        if ((img = (Bitmap) extras.get("data")) == null)
            return;
        btnImage.setImageBitmap(img);
        // https://stackoverflow.com/a/57477296
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 90, b);
        this.data.put("image", Base64.encodeToString(b.toByteArray(), Base64.DEFAULT));
    }

    private void saveProfile() {
        data.put("name",  edName.getText().toString());
        data.put("email", edEmail.getText().toString());
        data.put("phone", edPhone.getText().toString());
        data.put("home",  edHome.getText().toString());
        dr.set(data)
            .addOnSuccessListener(v -> {
                Log.d("Firestore", "Firestore update successful for profile");
                finish();
            })
            .addOnFailureListener(e -> {
                Log.e("Firestore", "Firestore update failed for poster");
                finish();
            });
    }
}
