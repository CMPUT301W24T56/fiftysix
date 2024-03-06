package com.example.fiftysix;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.ViewFlipper;

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
        attendeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Open attendee page.

                Intent intent = new Intent(MainActivity.this, AttendeeMainActivity.class);
                startActivity(intent);
            }
        });


        // User selects Organizer
        organizerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(MainActivity.this, OrganizerMainActivity.class);
               startActivity(intent);
            }
        });


        // Not for part 3
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AdminMainActivity.class);
                startActivity(intent);
            }
        });
    }
}