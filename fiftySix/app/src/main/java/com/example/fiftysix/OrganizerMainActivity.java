package com.example.fiftysix;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ViewFlipper;
import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class OrganizerMainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private CollectionReference orgEventRef;
    private CollectionReference eventRef;
    private RecyclerView recyclerView;


    // Views
    private ViewFlipper viewFlipper;
    private Organizer organizer;
    private String reUseQRID;


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
    private EditText eventAttendeeLimitEditText;

    // Buttons on Upload QR page
    private Button uploadQRFromScan;
    private ArrayList<Event> eventDataList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.organizer_flipper);
        viewFlipper = findViewById(R.id.organizerFlipper);
        Context context = getApplicationContext();
        recyclerView = findViewById(R.id.orgHomeRecyclerView);

        // Creates Organizer Object
        organizer = new Organizer(context);

        // Firebase
        db = FirebaseFirestore.getInstance();
        orgEventRef = db.collection("Users").document(organizer.getOrganizerID()).collection("EventsByOrganizer");
        eventRef = db.collection("Events");


        setButtons();
        setEditText();


        eventDataList = new ArrayList<>();
        //eventDataList.add(new Event("Event Name", "Event Location", "Event Date"));

        // Sets home page recyler view event data
        EventAdapter eventAdapter = new EventAdapter(eventDataList);
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setHasFixedSize(false);


        // Adds events from database to the organizers home screen. Will only show events created by the organizer
        orgEventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshots,
                                @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (querySnapshots != null) {
                    eventDataList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        String eventID = doc.getId();
                        Log.d("EVENTNAME", "hello "+ eventID);
                        eventRef.document(eventID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (error != null) {
                                    Log.e("Firestore", error.toString());
                                    return;
                                }
                                if (querySnapshots != null) {
                                    String eventName = value.getString("eventName");
                                    //String location = doc.get("location").toString(); TODO:NEED TO ADD TO DATA BASE
                                    //String date = doc.get("date").toString(); TODO:NEED TO ADD TO DATA BASE
                                    eventDataList.add(new Event(eventName, "temp location", "temp Date"));
                                    eventAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        Log.d("Firestore", "hello");
                    }
                }
            }
        });


        // Opens page to create event
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
                //Integer eventAttendeeLimit = Integer.parseInt(eventAttendeeLimitEditText.getText().toString());


                organizer.createEventNewQRCode(eventDetails, eventAddress, 100, eventTitle);
                previousView(v);
            }
        });


        // Opens viewe to reuse android a qrcode for attendee check in.
        reuseCheckInQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextView(v);
            }
        });

        // Switchs layout to previous when user presses back in event details page
        eventDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousView(v);
            }
        });

        // TODO: fix QR reuse method createEventReuseQRCode
        uploadQRFromScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //scanCode();
                String eventTitle = eventTitleEditText.getText().toString();
                String eventDate = eventDateEditText.getText().toString();
                String eventAddress = eventAddressEditText.getText().toString();
                String eventDetails = eventDetailsEditText.getText().toString();
                //Integer eventAttendeeLimit = Integer.parseInt(eventAttendeeLimitEditText.getText().toString());
                //organizer.createEventReuseQRCode(eventDetails, eventAddress, 100, eventTitle, reUseQRID);
                previousView(v);
            }
        });





    }



    //________________________________________Methods________________________________________


    private void previousView(View v){
        viewFlipper.showPrevious();
    }

    private void nextView(View v){
        viewFlipper.showNext();
    }

    private void setButtons(){
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
    }

    private void setEditText(){
        eventTitleEditText = (EditText) findViewById(R.id.eventNameEditText);
        eventDateEditText = (EditText) findViewById(R.id.eventDateEditText);
        eventAddressEditText = (EditText) findViewById(R.id.eventAddressEditText);
        eventDetailsEditText = (EditText) findViewById(R.id.eventDetailsEditText);
        eventAttendeeLimitEditText = (EditText) findViewById(R.id.eventAttendeeLimitEditText);
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
                    reUseQRID = result.getContents().replace(" ", "");

                    dialog.dismiss();
                }
            }).show();
        }

    });



    }




