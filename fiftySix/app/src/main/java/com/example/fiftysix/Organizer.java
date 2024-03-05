package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Organizer {
    private String organizerID;
    private List<String> eventIDs;
    private Context mContext;
    private String eventID;

    private String userType = "organizer";
    private FirebaseFirestore db;
    private CollectionReference ref;


    public Organizer(Context mContext) {
        this.mContext = mContext;
        this.organizerID = getDeviceId();
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Users");

        // Adds organizer to data base if the organizer doesn't already exist
        organizerExists();
    }


    // ________________________________METHODS_____________________________________


    // Gets android ID to be used as organizer ID
    // Got from https://stackoverflow.com/questions/60503568/best-possible-way-to-get-device-id-in-android
    public String getDeviceId() {
        String id = Settings.Secure.getString(this.mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
    }


    // Method creates new event in database and generates a new check-in QR code
    public void createEventNewQRCode( String details, String location, Integer attendeeLimit, String eventName){
        Event event = new Event(this.organizerID, details, location, attendeeLimit, eventName, mContext);
        addEventToOrganizerDataBase(event.getEventID());
    }

    // Method creates new event in database and reuses check-in QR code
    public void createEventReuseQRCode(){

    }


    // Adds organizer to database.
    private void addOrganizerToDatabase(){
        Map<String,Object> orgData = new HashMap<>();
        orgData.put("type",this.userType);

        this.ref
                .document(this.organizerID)
                .set(orgData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firestore", "Organizer Data successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firestore", "ERROR: Organizer Data failed to upload.");
                    }
                });
    }


    // Adds event data to database in firestore, this is nested inside the organizer.
    private void addEventToOrganizerDataBase(String eventIDKey){
        Map<String,Object> orgEventsData = new HashMap<>();
        orgEventsData.put("temp","temp");
        this.ref.document(this.organizerID).collection("EventsByOrganizer").document(eventIDKey).set(orgEventsData);
    }

    // Checks if the organizer is already in the database, If not in the database the organizer is added to it.
    // WILL NEED TO REWRITE
    // https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection
    private void organizerExists(){
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference docIdRef = rootRef.collection("Users").document(this.organizerID);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Organizer already exists!");
                        //organizerID = "thisOrganizerAlreadyExists";
                        //addOrganizerToDatabase();

                    } else {
                        Log.d(TAG, "Organizer does not already exist!");
                        addOrganizerToDatabase();
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }

            }
        });
    }
}
