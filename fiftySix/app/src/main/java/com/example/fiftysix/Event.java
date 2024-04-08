package com.example.fiftysix;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity to allow organizers to edit some event details
 * @author Brady.
 * @version 1
 * @since SDK34
 */

public class Event {
    private String eventID;
    private String organizerID;
    private String posterURL;
    private String posterID;
    private String checkInQRCodeID; // Check if should be auto generated upon creating event
    private String details;
    private String location;
    private Integer attendeeCheckInLimit, attendeeSignUpLimit;
    private Integer attendeeCount;
    private Integer signUpCount;
    private String eventName;
    private String promoQRCodeID;
    private String startDate, endDate, startTime, endTime;
    private Boolean expandable;
    private CheckInQRCode checkInQRCode;
    private FirebaseFirestore db;
    private CollectionReference ref;
    private Context mContext;



    // ________________________________CONSTRUCTORS_____________________________________

    /**
     * Creates an Event Object and adds it to the database. This constructor is used when the organizer does NOT want to reuse a check-in QR code.
     * @param organizerID String organizer ID organizing the event
     * @param details String event details
     * @param location String event location
     * @param attendeeCheckInLimit Integer limit of event checkins
     * @param attendeeSignUpLimit Integer limit of event signups
     * @param eventName String of event Name
     * @param startDate String of event start date
     * @param endDate String of event end date
     * @param startTime String of event start time
     * @param endTime String of event end time
     * @param mContext application Context
     */
    public Event(String organizerID, String details, String location, Integer attendeeCheckInLimit, Integer attendeeSignUpLimit, String eventName, String startDate, String endDate, String startTime, String endTime, Context mContext) {
        this.organizerID = organizerID;
        this.details = details;
        this.location = location;
        this.attendeeCheckInLimit = attendeeCheckInLimit;
        this.attendeeSignUpLimit = attendeeSignUpLimit;
        this.eventName = eventName;
        this.mContext = mContext;
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Events");
        this.eventID = FirebaseDatabase.getInstance().getReference("Events").push().getKey();
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.posterID = FirebaseDatabase.getInstance().getReference("images").push().getKey();

        // Generates New check-in QR code and stores the qrcode ID.
        this.checkInQRCode = new CheckInQRCode(eventID, mContext);
        this.checkInQRCodeID = this.checkInQRCode.getQRCodeID();
        addEventToDataBase();
    }


    /**
     * Used to create an event object from given event ID, mainly passing events to adapters.
     * @param eventID String ID of event
     * @param eventName String event name
     * @param eventLocation String event location
     * @param startDate String event start date
     * @param endDate String event end date
     * @param startTime String event start time
     * @param endTime String event end time
     * @param details String event details
     * @param attendeeCount Integer number of attendees checked in to the event
     * @param signUpCount Integer number of attendees signed up to the event
     * @param attendeeCheckInLimit Integer limit of event checkins
     * @param attendeeSignUpLimit Integer limit of event signups
     * @param posterURL URL of the event poster
     */
    public Event(String eventID, String eventName, String eventLocation, String startDate, String endDate, String startTime, String endTime, String details, Integer attendeeCount, Integer signUpCount, Integer attendeeCheckInLimit, Integer attendeeSignUpLimit, String posterURL) {
        this.eventID = eventID;
        this.eventName = eventName;
        this.location = eventLocation;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.details = details;
        this.attendeeCount = attendeeCount;
        this.attendeeCheckInLimit = attendeeCheckInLimit;
        this.attendeeSignUpLimit = attendeeSignUpLimit;
        this.posterURL = posterURL;
        this.expandable = false;
        this.signUpCount = signUpCount;
    }

    /**
     * Creates Event object, does not add it to database.
     * @param organizerID String organizer ID organizing the event
     * @param details String event details
     * @param location String event location
     * @param attendeeCheckInLimit Integer limit of event checkins
     * @param attendeeSignUpLimit Integer limit of event signups
     * @param eventName String of event Name
     * @param mContext Context apllication context
     * @param checkInQRCodeID String ID of checkin QrCode
     */
    public Event(String organizerID, String details, String location, Integer attendeeCheckInLimit, Integer attendeeSignUpLimit, String eventName, Context mContext, String checkInQRCodeID) {
        this.eventID = FirebaseDatabase.getInstance().getReference("Events").push().getKey();
        this.organizerID = organizerID;
        this.details = details;
        this.location = location;
        this.attendeeCheckInLimit = attendeeCheckInLimit;
        this.attendeeSignUpLimit = attendeeSignUpLimit;
        this.eventName = eventName;
        this.mContext = mContext;
        this.checkInQRCodeID = checkInQRCodeID;
        this.db = FirebaseFirestore.getInstance();
        this.posterID = FirebaseDatabase.getInstance().getReference("images").push().getKey();
        // Switches data
        db.collection("Events").document(this.eventID).update("checkInQRCode", checkInQRCodeID);
        db.collection("CheckInQRCode").document(this.checkInQRCodeID).update("event", this.eventID);
    }


    // ________________________________METHODS_____________________________________

    /**
     * Adds/Links promotion QR Code to event
     * @param promoQRCodeID String of the promotion QRCode ID to be added to the event
     */
    public void setPromoQR(String promoQRCodeID){
        db.collection("Events").document(this.eventID).update("promoQRCode", promoQRCodeID);
    }


    /**
     * Reuses previous checkin QR code, updates database.
     * @param reuseQRCodeID String checkin QR Code ID to be used.
     */
    public void reuseCheckInQR(String reuseQRCodeID){
        db.collection("Events").document(this.eventID).update("checkInQRCode", reuseQRCodeID);
        db.collection("CheckInQRCode").document(reuseQRCodeID).update("event", this.eventID);

        // Deletes new checkin QRCode created
        db.collection("CheckInQRCode").document(this.checkInQRCodeID).delete();
        FirebaseStorage.getInstance().getReference().child("images/checkInQRCode/" + this.checkInQRCodeID).delete();
    }


    /**
     * Adds event data to database.
     */
    private void addEventToDataBase(){
        Map<String,Object> eventData = new HashMap<>();
        eventData.put("attendeeCount",0);
        eventData.put("attendeeSignUpCount",0);
        eventData.put("attendeeLimit",this.attendeeCheckInLimit);
        eventData.put("attendeeSignUpLimit",this.attendeeSignUpLimit);
        eventData.put("details",this.details);
        eventData.put("eventName",this.eventName);
        eventData.put("organizer",this.organizerID);
        eventData.put("checkInQRCode",this.checkInQRCodeID);
        eventData.put("startDate",this.startDate);
        eventData.put("endDate",this.endDate);
        eventData.put("startTime",this.startTime);
        eventData.put("endTime",this.endTime);
        eventData.put("location",this.location);
        eventData.put("attendee",this.attendeeCount);
        eventData.put("posterID",this.posterID);

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




    //_______________________________Getters________________________________________________________

    public String getPosterID(){ return posterID;}

    public String getEventID() {
        return eventID;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public String getDetails() {
        return details;
    }

    public String getLocation() {
        return location;
    }

    public Integer getCheckInLimit() {
        return attendeeCheckInLimit;
    }

    public Integer getSignUpLimit() {
        return attendeeSignUpLimit;
    }

    public Integer getAttendeeCount() {
        return attendeeCount;
    }

    public String getEventName() {
        return eventName;
    }

    public Boolean getExpandable() {
        return expandable;
    }

    public void setExpandable(Boolean expandable) {
        this.expandable = expandable;
    }

    public Integer getSignUpCount() {return signUpCount;}

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }
}
