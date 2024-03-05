package com.example.fiftysix;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Event {
    private String eventID;
    private String organizerID;
    private String posterID;
    private Attendee[] attendeeList;
    private Attendee[] rsvpAttendeeList;
    private String checkInQRCodeID; // Check if should be auto generated upon creating event
    private String details;
    private String location;
    private Integer attendeeLimit;
    private Integer attendeeCount;
    private String eventName;
    private String promoQRCodeID;

    private CheckInQRCode checkInQRCode;
    private FirebaseFirestore db;
    private CollectionReference ref;
    private Context mContext;
    //private Map map; used after part 3


    // ________________________________CONSTRUCTORS_____________________________________


    public String getEventID() {
        return eventID;
    }


    // This constructor is used when the organizer does NOT want to reuse a check-in QR code.
    public Event(String organizerID, String details, String location, Integer attendeeLimit, String eventName, Context mContext) {
        this.organizerID = organizerID;
        this.details = details;
        this.location = location;
        this.attendeeLimit = attendeeLimit;
        this.eventName = eventName;
        this.mContext = mContext;
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Events");
        this.eventID = FirebaseDatabase.getInstance().getReference("Events").push().getKey();

        // Generates New check-in QR code and stores the qrcode ID.
        this.checkInQRCode = new CheckInQRCode(eventID, mContext);
        this.checkInQRCodeID = this.checkInQRCode.getQRCodeID();
        addEventToDataBase();
    }


    // This constructor is used when the organizer WANTS to reuse a check-in QR code.
    public Event(String organizerID, String details, String location, Integer attendeeLimit, String eventName, Context mContext, String checkInQRCodeID) {
        this.eventID = FirebaseDatabase.getInstance().getReference("Events").push().getKey();
        this.organizerID = organizerID;
        this.details = details;
        this.location = location;
        this.attendeeLimit = attendeeLimit;
        this.eventName = eventName;
        this.mContext = mContext;
        this.checkInQRCodeID = checkInQRCodeID;
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Events");
        addEventToDataBase();

    }


    // ________________________________METHODS_____________________________________

    // Adds event data to database in firestore.
    private void addEventToDataBase(){



        Map<String,Object> eventData = new HashMap<>();
        eventData.put("attendeeCount",0);
        eventData.put("attendeeLimit",this.attendeeLimit);
        eventData.put("attendeeLimit",this.attendeeLimit);
        eventData.put("attendeeList",this.attendeeList);
        eventData.put("attendeeList",this.rsvpAttendeeList);
        eventData.put("details",this.details);
        eventData.put("eventName",this.eventName);
        eventData.put("organizer",this.organizerID);
        eventData.put("checkInQRCode",this.checkInQRCodeID);

        // These are optional so we need to check if they are null

        //eventData.put("poster",this.posterID);
        //eventData.put("promoQRCode",this.promoQRCodeID);
        //eventData.put("map",this.map); for after part 3


        this.ref
                .document(eventID)
                .set(eventData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firestore", "Event Data successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firestore", "ERROR: Event Data failed to upload.");
                    }
                });
    }

}
