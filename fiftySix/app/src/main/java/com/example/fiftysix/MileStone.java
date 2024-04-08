package com.example.fiftysix;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

/**
 * Milestones, used to track attendance milestones for event signups. Milestones are added to database if they aren't already and can be hidden.
 *
 * @author Brady.
 * @version 1
 * @since SDK34
 */

public class MileStone {
    private String eventName;
    private String message;
    private String userID;
    private String eventID;
    private Boolean viewable;
    private String attendees;


    /**
     * Create a Milestone object
     *
     * @param userID String of user ID or organizer of event with milestone
     * @param eventID String of user ID of organizer with milestone
     * @param eventName String of event name
     * @param message string of milestone message shown to organizer
     * @param attendees string of the amount of attendees
     */
    public MileStone(String userID, String eventID, String eventName, String message, String attendees) {
        this.eventName = eventName;
        this.message = message;
        this.userID = userID;
        this.eventID = eventID;
        this.viewable = true;
        this.attendees = attendees;
    }

    /**
     * Adds milestone to the database unless it has already been added.
     */
    public void addToDatabase(){

        Map<String,Object> milestoneData = new HashMap<>();
        milestoneData.put("eventName", eventName);
        milestoneData.put("message", message);
        milestoneData.put("viewable", viewable);

        FirebaseFirestore.getInstance().collection("Users").document(userID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(attendees).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (! documentSnapshot.exists()){
                    FirebaseFirestore.getInstance().collection("Users").document(userID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(attendees).set(milestoneData);
                    FirebaseFirestore.getInstance().collection("Events").document(eventID).collection("Milestones").document(attendees).set(milestoneData);
                }
            }
        });
    }

    /**
     * sets Milestone viewable field to false in the database.
     */
    public void hideMilestone(){
        this.viewable = false;
        Map<String,Object> milestoneData = new HashMap<>();
        milestoneData.put("eventName", eventName);
        milestoneData.put("message", message);
        milestoneData.put("viewable", viewable);

        FirebaseFirestore.getInstance().collection("Users").document(userID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(attendees).update(milestoneData);
    }

    //________________________ Basic Getters _________________________________

    public String getTitle() {
        return eventName;
    }

    public String getMessage() {
        return message;
    }

    public String getEventID() {
        return eventID;
    }

    public String getUserID() { return userID; }

    public String getAttendeeCount() { return attendees; }

}
