package com.example.fiftysix;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class OrganizerMainActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private Organizer organizer;
    private int attendeeLimit = Integer.MAX_VALUE;

    // Buttons on home pages
    private ImageButton addEventButton;
    private ImageButton orgProfileButton;
    private ImageButton orgNotificationButton;
    private ImageButton orgHomeButton;

    // Buttons on Create event page
    private Button createEvent;
    private ImageButton eventDetailsBack;
    private Button reuseCheckInQR;
    private EditText eventTitleEditText;
    private EditText eventDateEditText;
    private EditText eventAddressEditText;
    private EditText eventDetailsEditText;
    private Switch switchAttendeeLimit;
    private EditText editTextAttendeeLimit;
    // Buttons on Upload QR page
    private Button uploadQRFromScan;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_flipper);
        Context context = getApplicationContext();

        setButtons();
        setEditText();
        setupAttendeeLimitSwitch();

        viewFlipper = findViewById(R.id.myViewFlipper);


        // Creates Organizer Object
        organizer = new Organizer(context);


        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextView(v);
            }
        });


        // Create event pages
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String eventTitle = eventTitleEditText.getText().toString();
                String eventDate = eventDateEditText.getText().toString();
                String eventAddress = eventAddressEditText.getText().toString();
                String eventDetails = eventDetailsEditText.getText().toString();


                organizer.createEventNewQRCode(eventDetails, eventAddress, attendeeLimit, eventTitle);
                previousView(v);
            }

        });

        reuseCheckInQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextView(v);
            }
        });

        eventDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousView(v);
            }
        });

        reuseCheckInQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextView(v);
            }
        });


        uploadQRFromScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
            }
        });



    }

    public void previousView(View v){
        viewFlipper.showPrevious();
    }

    public void nextView(View v){
        viewFlipper.showNext();
    }

    public void setButtons(){
        // Home page buttons
         addEventButton = (ImageButton) findViewById(R.id.buttonAddEvent);
         orgProfileButton = (ImageButton) findViewById(R.id.buttonOrganizerProfile);
         orgNotificationButton = (ImageButton) findViewById(R.id.buttonOrganizerNotificationBell);
         orgHomeButton = (ImageButton) findViewById(R.id.buttonOrganizerHome);
        // Create event page buttons
        reuseCheckInQR = (Button) findViewById(R.id.reuseCheckinQR);
        createEvent = (Button) findViewById(R.id.buttonCreateEvent);
        eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackCreateEvent);
        ImageButton eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackUploadQR);
        uploadQRFromScan = (Button) findViewById(R.id.uploadQRFromScan);
        switchAttendeeLimit = findViewById(R.id.switchAttendeeLimit);

    }

    public void setEditText(){
        eventTitleEditText = (EditText) findViewById(R.id.eventNameEditText);
        eventDateEditText = (EditText) findViewById(R.id.eventDateEditText);
        eventAddressEditText = (EditText) findViewById(R.id.eventAddressEditText);
        eventDetailsEditText = (EditText) findViewById(R.id.eventDetailsEditText);
        editTextAttendeeLimit = (EditText) findViewById(R.id.editTextNumberAttendeeLimit);
    }

    private void setupAttendeeLimitSwitch() {
        switchAttendeeLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(OrganizerMainActivity.this);
                    builder.setTitle("Set Attendee Limit");

                    // Set up the input
                    final EditText input = new EditText(OrganizerMainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_NUMBER);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                int limit = Integer.parseInt(input.getText().toString());
                                if (limit > 0) {
                                    attendeeLimit = limit;
                                } else {
                                    // Invalid input, revert the switch to unchecked
                                    switchAttendeeLimit.setChecked(false);
                                    attendeeLimit = Integer.MAX_VALUE;
                                }
                            } catch (NumberFormatException e) {
                                switchAttendeeLimit.setChecked(false);
                                attendeeLimit = Integer.MAX_VALUE;
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            // Revert the switch to unchecked since canceled
                            switchAttendeeLimit.setChecked(false);
                        }
                    });

                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            // Handle the case where the user cancels the dialog (e.g., by pressing the back button)
                            switchAttendeeLimit.setChecked(false);
                        }
                    });

                    builder.show();
                } else {
                    attendeeLimit = Integer.MAX_VALUE;
                }
            }
        });
    }



    // "youtube - Implement Barcode QR Scanner in Android studio barcode reader | Cambo Tutorial" - youtube channel = Cambo Tutorial
    private void scanCode(){
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to turn on flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result->{

        if(result.getContents() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(OrganizerMainActivity.this);
            builder.setTitle("Result");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }

    });


}

