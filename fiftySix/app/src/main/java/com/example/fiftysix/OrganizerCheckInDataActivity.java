package com.example.fiftysix;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class OrganizerCheckInDataActivity extends AppCompatActivity {

    private Organizer organizer;
    private String organizerID;
    private OrganizerCheckInEventAdapter organizerCheckInEventAdapter;
    private ArrayList<Event> eventAttendeeDataList;
    private RecyclerView recyclerView;
    private String eventID;

    private FirebaseFirestore db;
    private CollectionReference orgEventRef;
    private CollectionReference eventRef;
    private CollectionReference imageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_attendee_data);
        Context context = getApplicationContext();
        recyclerView = findViewById(R.id.orgAttendeeDataRecyclerView);

        Bundle bundle = getIntent().getExtras();
        eventID = bundle.getString("eventID");



        ImageButton buttonBackCreateEvent = (ImageButton) findViewById(R.id.buttonBackCreateEvent);

        // Creates Organizer Object
        organizer = new Organizer(context);
        organizerID = organizer.getOrganizerID();

        eventAttendeeDataList = new ArrayList<>();
        organizerCheckInEventAdapter = new OrganizerCheckInEventAdapter(eventAttendeeDataList, this);
        recyclerView.setAdapter(organizerCheckInEventAdapter);
        recyclerView.setHasFixedSize(false);

        db = FirebaseFirestore.getInstance();
        orgEventRef = db.collection("Users").document(organizer.getOrganizerID()).collection("EventsByOrganizer");
        eventRef = db.collection("Events");
        imageRef = db.collection("Images");

        loadOrganizerAttendees();

        buttonBackCreateEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }




    private void loadOrganizerAttendees() {

        // Checks for updates in Events collection in firebase

        Log.d("ORGATTENDEES", "eventID = " + eventID);
        db.collection("Events").document(eventID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    String eventID = value.getId();
                    String eventName = value.getString("eventName");
                    String posterID = value.getString("posterID");
                    Integer inAttendeeLimit = value.getLong("attendeeLimit").intValue();
                    Integer inAttendeeCount = value.getLong("attendeeCount").intValue();
                    String inDate = value.getString("date");
                    String location = value.getString("location");
                    String details = value.getString("details");

                    if (inAttendeeCount > 0) {

                        // There are attendees at the event so we get the attendees ID from the sub-colletion "attendeesAtEvent" inside the event document
                        eventRef.document(eventID).collection("attendeesAtEvent").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (querySnapshot != null) {

                                    // Loops through all attendees at the event and adds them to eventAttendeeDataList to be displayed to the organizer
                                    for (QueryDocumentSnapshot attendeeDoc : querySnapshot) {
                                        String attendeeID = attendeeDoc.getId();

                                        // Gets the profile matching the attendee at the event, used to display the attendees info to the organizer.
                                        db.collection("Profiles").document(attendeeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                if (documentSnapshot != null) {
                                                    String attendeeName = documentSnapshot.getString("name");
                                                    String attendeePhoneNumber = documentSnapshot.getString("phone");
                                                    String attendeeEmail = documentSnapshot.getString("email");
                                                    String profileURL = documentSnapshot.getString("profileImageURL");
                                                    eventAttendeeDataList.add(new Event(eventID, attendeeName, attendeePhoneNumber, attendeeEmail, "temp hard coded for testing", 0, 0, 0,profileURL));
                                                    organizerCheckInEventAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}