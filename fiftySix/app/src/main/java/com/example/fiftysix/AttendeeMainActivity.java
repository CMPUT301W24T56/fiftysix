package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class AttendeeMainActivity extends AppCompatActivity {


    private FirebaseFirestore db;
    private CollectionReference attEventRef;
    private CollectionReference eventRef;
    private ViewFlipper viewFlipper;
    private Attendee attendee;
    private String eventCheckinID;
    private RecyclerView recyclerView;
    private ArrayList<Event> eventDataList;

    // Buttons on home pages
    private ImageButton attendeeAddEventButton;
    private ImageButton attendeeProfileButton;
    private ImageButton attendeeNotificationButton;
    private ImageButton attendeeHomeButton;

    // Buttons on Add event page
    private Button addEventScanCheckinButton;
    private ImageButton addEventBackImageButton;
    private Button browseEventsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_flipper);
        viewFlipper = findViewById(R.id.attendeeFlipper);

        Context context = getApplicationContext();

        setButtons();
        attendee = new Attendee(context);

        // The user is stored as an organizer and should be returned to main page
       // if (attendee.getUserType()=="organizer"){
            //TODO: doesn't work but shows general idea, need to query the data to check user type from firebase
            //finish();

        //}

        eventDataList = new ArrayList<>();

        recyclerView = findViewById(R.id.attendeeHomeRecyclerView);
        setRecyclerView();

        db = FirebaseFirestore.getInstance();
        attEventRef = db.collection("Users").document(attendee.getDeviceId()).collection("EventsByOrganizer");
        eventRef = db.collection("Events");

        EventAdapter eventAdapter = new EventAdapter(eventDataList, true);
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setHasFixedSize(false);



        //________________________________________UpdatesHomePageData________________________________________

        // Adds events from database to the attendee home screen. Will only show events the attendee has signed up for.
        attEventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
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

                        eventRef.document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        String eventName = document.get("eventName").toString();
                                        String imageUrl = document.getString("posterURL");
                                        //String location = doc.get("location").toString(); TODO:NEED TO ADD TO DATA BASE
                                        //String date = doc.get("date").toString(); TODO:NEED TO ADD TO DATA BASE
                                        eventDataList.add(new Event(eventName, "temp location", "temp Date", imageUrl));
                                        eventAdapter.notifyDataSetChanged();
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                        Log.d("Firestore", "hello");
                    }
                }
            }
        });








        //________________________________________HomePage________________________________________

        attendeeAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextView(v); // Opens add event page
                //setContentView(R.layout.attendee_sign_up_event_view);
            }
        });

        attendeeProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Open profile of attendee
            }
        });

        attendeeNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Open notifications
            }
        });

        attendeeHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: not sure what the home button should do
            }
        });


        //________________________________________AddEventPage________________________________________

        addEventScanCheckinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: ScanQRCode to check in to event
                scanCode();


            }
        });

        browseEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Browse all events to sign up for
            }
        });

        addEventBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                previousView(v); // Returns to attendee home pages
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
        attendeeAddEventButton = (ImageButton) findViewById(R.id.buttonAttendeeSignInEvent);
        attendeeProfileButton = (ImageButton) findViewById(R.id.buttonAttendeeProfile);
        attendeeNotificationButton = (ImageButton) findViewById(R.id.buttonAttendeeNotificationBell);
        attendeeHomeButton = (ImageButton) findViewById(R.id.buttonAttendeeHome);

        // Add event page buttons
        addEventScanCheckinButton = (Button) findViewById(R.id.buttonAttendeeCheckinWithQR);
        browseEventsButton = (Button) findViewById(R.id.buttonAttendeeBrowseEvent);
        addEventBackImageButton = (ImageButton) findViewById(R.id.buttonAttendeeBackSignUp);

    }

    private void setRecyclerView() {
        EventAdapter eventAdapter = new EventAdapter(eventDataList, true);
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setHasFixedSize(false);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeMainActivity.this);
            builder.setTitle("Checked In");
            builder.setMessage(result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    eventCheckinID = result.getContents().toString();
                    attendee.checkInToEvent(eventCheckinID);

                    dialog.dismiss();
                }
            }).show();
        }

    });
}