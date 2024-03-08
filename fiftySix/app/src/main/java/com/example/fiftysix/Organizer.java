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
 * An event organizer.
 * @author Bnpower, ShikariRB
 * @see Event
 * @version 1.0
 */
public class Organizer {
    private String organizerID;
    private ArrayList<String> eventIDs;
    private Context mContext;
    private String eventID;
    private ArrayList<String> myEvents;

    private Integer attendeeLimit;
    private String userType = "organizer";
    private FirebaseFirestore db;
    private CollectionReference ref;


    /**
     * Constructs an organizer.
     * @param mContext references global information about the application environment
     */
    public Organizer(Context mContext) {
        this.mContext = mContext;
        this.organizerID = getDeviceId();
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Users");


        // Adds organizer to data base if the organizer doesn't already exist
        organizerExists();
    }


    // ________________________________METHODS_____________________________________


    /**
     * Gets android ID to be used as organizer ID
     * Got from https://stackoverflow.com/questions/60503568/best-possible-way-to-get-device-id-in-android
     * @return id the Android id of the users device
     */
    public String getDeviceId() {
        String id = Settings.Secure.getString(this.mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
    }

    /**
     * Gets the id of the organizer
     * @return organizerID the Id of the organizer
     */
    public String getOrganizerID(){
        return organizerID;
    }

    /**
     * Creates a new event in the database and generates a new check-in QR code
     * @param details information about the event
     * @param location the location where the event takes place
     * @param attendeeLimit the maximum number of people that can attend the event
     * @param eventName the name of the event
     * @return the id of the newly created event
     */
    public String createEventNewQRCode( String details, String location, Integer attendeeLimit, String eventName){
        Event event = new Event(this.organizerID, details, location, attendeeLimit, eventName, mContext);
        addEventToOrganizerDataBase(event.getEventID());
        return event.getEventID();
    }

    /**
     * Creates a new event in the database and reuses a check-in QR code
     * @param details information about the event
     * @param location the location where the event takes place
     * @param attendeeLimit the maximum number of people that can attend the event
     * @param eventName the name of the event
     * @param context references global information about the application environment
     * @param oldQRID the check-in QR code to use
     */
    public void createEventReuseQRCode(String details, String location, Integer attendeeLimit, String eventName,Context context, String oldQRID){
        Event event = new Event(this.organizerID, details, location, attendeeLimit, eventName, context, oldQRID);
        addEventToOrganizerDataBase(event.getEventID());
    }

    /**
     * Gets a list all event data to display on the organizers home page
     * https://stackoverflow.com/questions/50035752/how-to-get-list-of-documents-from-a-collection-in-firestore-android
     * @exception Exception raisednwhen there is an error getting the requested documents
     */
    public void getEventIDList() {

        this.myEvents = new ArrayList<>();

        //ArrayList<String> arrayList= new ArrayList<String>();

        db.collection("Users").document(organizerID).collection("EventsByOrganizer")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {



                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                addEventID(document.getId().toString());

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    // kind of like a call back used for get event IDs
    private void addEventID(String eventID){
        this.myEvents.add(eventID);
        Log.d("Cunt", "NNNNNNNN => " + this.myEvents);
    }

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

    private void addEventToOrganizerDataBase(String eventIDKey){
        Map<String,Object> orgEventsData = new HashMap<>();
        orgEventsData.put("testing","temp");
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

    /**
     * Gets the first event in the organizers list of events.
     * @return the first event in the list of the organizers events.
     */
    public String getMyEvents() {
        return myEvents.get(1);
    }
}
