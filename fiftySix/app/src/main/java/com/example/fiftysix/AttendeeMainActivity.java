package com.example.fiftysix;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;

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

    private ImageButton profile_button;
    private ImageButton notification_button;
    private ImageButton qrcode_button;
    private ImageButton home_button;

    private FirebaseFirestore db;
    private CollectionReference attEventRef;
    private CollectionReference eventRef;
    private ViewFlipper viewFlipper;
    private Attendee attendee;
    private String eventCheckinID;
    private RecyclerView recyclerViewMyEvents;
    private RecyclerView recyclerViewAllEvents;
    private ArrayList<Event> myEventDataList;
    private ArrayList<Event> allEventDataList;

    // Buttons on home pages
    private ImageButton attendeeAddEventButton;
    private ImageButton attendeeProfileButton;
    private ImageButton attendeeNotificationButton;
    private ImageButton attendeeHomeButton;
    private Button browseAllEventsButton;
    private Button browseMyEvents;

    // Buttons on Add event page
    private Button addEventScanCheckinButton;
    private ImageButton addEventBackImageButton;
    private Button browseEventsButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onClick:   not working ");
        setContentView(R.layout.activity_attendee_main);

        profile_button = findViewById(R.id.attendee_profile);
        qrcode_button = findViewById(R.id.qr_code_button);
        notification_button = findViewById(R.id.notification_button);
        home_button = findViewById(R.id.button_attendee_home);

        setButtons();
        attendee = new Attendee(context);





        // The user is stored as an organizer and should be returned to main page
       // if (attendee.getUserType()=="organizer"){
            //TODO: doesn't work but shows general idea, need to query the data to check user type from firebase
            //finish();

        //}

        myEventDataList = new ArrayList<>();
        allEventDataList = new ArrayList<>();
        //myEventDataList.add(new Event("temp location", "temp location", "temp Date", "temp details", 11, 100));

        recyclerViewMyEvents = findViewById(R.id.attendeeHomeRecyclerView);
        recyclerViewAllEvents = findViewById(R.id.attendeeHomeRecyclerViewAllEvents);
        //setRecyclerView();

        db = FirebaseFirestore.getInstance();
        attEventRef = db.collection("Users").document(attendee.getDeviceId()).collection("UpcomingEvents");
        eventRef = db.collection("Events");

        AttendeeMyEventAdapter attendeeMyEventAdapter = new AttendeeMyEventAdapter(myEventDataList);
        recyclerViewMyEvents.setAdapter(attendeeMyEventAdapter);
        recyclerViewMyEvents.setHasFixedSize(false);


        AttendeeMyEventAdapter attendeeAllEventAdapter = new AttendeeMyEventAdapter(allEventDataList);
        recyclerViewAllEvents.setAdapter(attendeeAllEventAdapter);
        recyclerViewAllEvents.setHasFixedSize(false);




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
                    myEventDataList.clear();

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
                                    Integer inAttendeeLimit = value.getLong("attendeeLimit").intValue();
                                    Integer inAttendeeCount = value.getLong("attendeeCount").intValue();
                                    String imageUrl = value.getString("posterURL");
                                    String inDate = value.getString("date");
                                    String location = value.getString("location");
                                    String details = value.getString("details");

                                    //if ((String.valueOf(inAttendeeCount) == null) || (String.valueOf(inAttendeeLimit) == null)){
                                    myEventDataList.add(new Event(eventName, location, inDate, details, inAttendeeCount, inAttendeeLimit, imageUrl));
                                    //}

                                    attendeeMyEventAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                        Log.d("Firestore", "hello");
                    }
                }
            }
        });







        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc : value) {

                    allEventDataList.clear();

                    String eventID = doc.getId();
                    String eventName = doc.getString("eventName");
                    Integer inAttendeeLimit = doc.getLong("attendeeLimit").intValue();
                    Integer inAttendeeCount = doc.getLong("attendeeCount").intValue();
                    String inDate = doc.getString("date");
                    String location = doc.getString("location");
                    String details = doc.getString("details");
                    String imageUrl = doc.getString("posterURL");

                    allEventDataList.add(new Event(eventName, location, inDate, details, inAttendeeCount, inAttendeeLimit, imageUrl));

                    attendeeAllEventAdapter.notifyDataSetChanged();
                }
            }
        });






        //________________________________________HomePage________________________________________

        browseAllEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: For Botsian
                viewFlipper.setDisplayedChild(2);
            }
        });

        browseMyEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: For Botsian
                nextView(v);
            }
        });




        attendeeAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextView(v); // Opens add event page
                //setContentView(R.layout.attendee_sign_up_event_view);
            }
        });

        notification_button.setOnClickListener(v -> {
            startActivity(new Intent(AttendeeMainActivity.this, Notification.class));
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
        browseAllEventsButton = (Button) findViewById(R.id.browseAllEvents);
        browseMyEvents = (Button) findViewById(R.id.browseMyEvents);

        // Add event page buttons
        addEventScanCheckinButton = (Button) findViewById(R.id.buttonAttendeeCheckinWithQR);
        browseEventsButton = (Button) findViewById(R.id.buttonAttendeeBrowseEvent);
        addEventBackImageButton = (ImageButton) findViewById(R.id.buttonAttendeeBackSignUp);





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
    }
}
