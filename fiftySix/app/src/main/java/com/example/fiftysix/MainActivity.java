package com.example.fiftysix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * Displays user selection screen, allows user to select organizer, attendee, or admin.
 * If user selects admin their device ID must be prior added to the Firebase in the administrators collection.
 * If the device ID is not in the collection they cannot access Admin.
 *
 * Link to add ADMIN device ID
 * https://console.firebase.google.com/project/fiftysix-a4bcf/firestore/databases/-default-/data/~2FAdministrators~2F185a2e0408f92fd9
 *
 * @author Arsh, Rakshit, Brady.
 * @version 1
 * @since SDK34
 */
public class MainActivity extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_selection_view);
        context = getApplicationContext();
        Button attendeeButton = (Button) findViewById(R.id.buttonAttendee);
        Button organizerButton = (Button) findViewById(R.id.buttonOrganizer);
        Button adminButton = (Button) findViewById(R.id.buttonAdmin);

        // User selects Attendee, attendee main activity launches.
        attendeeButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AttendeeMainActivity.class));
        });


        // User selects Organizer
        organizerButton.setOnClickListener(v -> {
           startActivity(new Intent(MainActivity.this, OrganizerMainActivity.class));
        });

        // User selects Admin
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Administrators").document(deviceId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null && document.exists()) {
                                        // The device ID matches an admin ID
                                        Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        // The device ID does not match an admin ID
                                        Toast.makeText(MainActivity.this, "You are not authorized as an admin.", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    // Error handling
                                    Toast.makeText(MainActivity.this, "Error verifying admin status: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }
}
