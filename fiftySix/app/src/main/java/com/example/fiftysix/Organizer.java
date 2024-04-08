package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodSession;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.callback.Callback;

/**
 * Organizer class, creates organizer, adds it to database. Allows the organizer to create events.
 * @author Brady, Rakshit, Arsh.
 * @version 1
 * @since SDK34
 */
public class Organizer {
    private String organizerID;
    private Context mContext;
    private String userType = "organizer";
    private FirebaseFirestore db;
    private CollectionReference ref;

    /**
     * Ornaginer constuctor, the device id is used as the organizer id. Checks if organizer is in database already, if not it is then added.
     * @param mContext
     */
    public Organizer(Context mContext) {
        this.mContext = mContext;
        this.organizerID = getDeviceId();
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Users");
        organizerExists(); // Adds organizer to data base if the organizer doesn't already exist
        new Attendee(mContext); // Adds feilds for attendee
    }


    // ________________________________METHODS_____________________________________

    /**
     * Gets the android device ID uses it as a key in data base and as the organizers ID
     * @return String that is the android device ID
     */
    private String getDeviceId() {
        String id = Settings.Secure.getString(this.mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
    }

    /**
     * Returns the organizer ID/ android device ID
     * @return String organizerID
     */
    public String getOrganizerID(){
        return organizerID;
    }

    /**
     * Creates an event, allows optional reuse of a previous QRCode or adding a promotion QRCode.
     * Creates Event Object and adds it to the database aswell as adds it to the organizers "eventsByOrganizer" collection.
     * @param details String of event details
     * @param location String of event location
     * @param attendeeCheckInLimit Integer of the event check-in limit, is max val int if no limit.
     * @param attendeeSignUpLimit Integer of the event sign-up limit, is max val int if no limit.
     * @param eventName String of the event name.
     * @param startDate String of the event start date format dd/mm/yy
     * @param endDate String of the event end date format dd/mm/yy
     * @param startTime String of the event start time format hh:mm, hh = 00-24
     * @param endTime String of the event end time format hh:mm, hh = 00-24
     * @param promoQRCode PromoQRCode for the event of null if the use does not want to add one.
     * @param reuseQRID String ID of old QR code of null depending if the organizer wants to reuse one.
     * @return String of the posterID corresponding to the created event.
     */
    public String createEventNewQRCode( String details, String location, Integer attendeeCheckInLimit, Integer attendeeSignUpLimit, String eventName, String startDate, String endDate, String startTime, String endTime, PromoQRCode promoQRCode, String reuseQRID){
        Event event = new Event(this.organizerID, details, location, attendeeCheckInLimit, attendeeSignUpLimit, eventName, startDate, endDate, startTime, endTime, mContext);
        addEventToOrganizerDataBase(event.getEventID());
        String eventID = event.getEventID();
        if (promoQRCode != null){
            promoQRCode.setEvent(eventID);
            event.setPromoQR(promoQRCode.getQRCodeID());
        }
        if (reuseQRID != null){
            event.reuseCheckInQR(reuseQRID);
        }
        String posterID = event.getPosterID();
        return posterID;
    }


    /**
     * Adds the organizer to the firebase, using the device ID as it's primary key. Stored inside of the Users collection.
     */
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



    /**
     * Adds the event to a collection "Organizer Events", the is nested in the users docuemnt in fire store. The document ID is an eventID hosted by the organizer.
     * @param eventIDKey String of
     */
    private void addEventToOrganizerDataBase(String eventIDKey){
        Map<String,Object> orgEventsData = new HashMap<>();
        orgEventsData.put("event","event"); // Only want the document id as it is the event ID, doesn't matter what is in the document
        this.ref.document(this.organizerID).collection("EventsByOrganizer").document(eventIDKey).set(orgEventsData);

    }

    /**
     * Checks if the organizer is already in the firebase, If not in the database the organizer is added to firebase.
     * Reference: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection
     */
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
