package com.example.fiftysix;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MileStone {
    private String eventName;
    private String message;
    private String userID;
    private String eventID;
    private Boolean viewable;
    private Integer attendees;


    public MileStone(String userID, String eventID, String eventName, String message, Integer attendees) {
        this.eventName = eventName;
        this.message = message;
        this.userID = userID;
        this.eventID = eventID;
        this.viewable = true;
        this.attendees = attendees;
    }

    public void addToDatabase(){

        FirebaseFirestore.getInstance().collection("Users").document(userID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(attendees.toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String,Object> milestoneData = new HashMap<>();
                milestoneData.put("eventName", eventName);
                milestoneData.put("message", message);
                milestoneData.put("viewable", viewable);

                FirebaseFirestore.getInstance().collection("Users").document(userID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(attendees.toString()).set(milestoneData);

            }
        });
    }

    public void hideMilestone(){
        this.viewable = false;
        Map<String,Object> milestoneData = new HashMap<>();
        milestoneData.put("eventName", eventName);
        milestoneData.put("message", message);
        milestoneData.put("viewable", viewable);

        FirebaseFirestore.getInstance().collection("Users").document(userID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(attendees.toString()).update(milestoneData);
    }
}
