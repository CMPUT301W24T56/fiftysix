package com.example.fiftysix;


import android.content.Context;
import android.location.Address;
import android.provider.Settings;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import static android.content.ContentValues.TAG;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.HashMap;
import java.util.Map;
/**
 * Attendee class, Has a profile they can view\edit.
 * Can View event details and announcements within the app event.
 * Check into an event by scanning the provided QR code.
 * Recieve push notifications from event organizers.
 * Can log in without username or password (Use device ID). GET DEVICE ID
 * @author Arsh, Rakshit, Brady.
 * @version 1
 * @since SDK34
 */
public class Attendee {

    private Context mContext;
    private String attendeeID;
    private FirebaseFirestore db;
    private CollectionReference ref;
    private String userType = "attendee";
    private String eventID;
    private Address location;


    // ________________________________CONSTRUCTORS_____________________________________

    /**
     *  Creates attendee object, if the device ID is not currently in the database linked to an attendee it will be added.
     *
     * @param mContext: Context current app context
     */
    public Attendee(Context mContext) {
        this.mContext = mContext;
        this.attendeeID = getDeviceId();
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Users");
        attendeeExists(); // Adds organizer to data base if the organizer doesn't already exist
    }

    /**
     * Creates attendee object, if the device ID is not currently in the database linked to an attendee it will be added.
     *
     * @param mContext Context, application context
     * @param location Address of the attendees location.
     */
    public Attendee(Context mContext, Address location) {
        this.mContext = mContext;
        this.attendeeID = getDeviceId();
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Users");
        this.location = location;
        attendeeExists(); // Adds organizer to data base if the organizer doesn't already exist
    }


    /**
     * AttendeeCallBack is used as a function pointer to check, check-in success.
     */
    public interface AttendeeCallBack{
        void checkInSuccess(Boolean checkinSuccess, String eventName);
    }


    // ________________________________METHODS_____________________________________


    /**
     * Allows the attendee to check into an event. Takes in the QRCode ID, with that ID it fetches what event that qrcode is currently linked to in the database.
     * It then adds the event ID to a collection inside the user document to keep track of events the user has check into.
     * It also increments the attendee count in the event document in the database and stores the attendees id in a sub-collection inside the event.
     * These are used so the organizer can view attendees and track realtime attendance.
     *
     * @param qRCodeID String of the checkin QR Code ID attempting to check into.
     * @param attendeeCallBack AttendeeCallBack is used as a function pointer to check, check-in success.
     */
    public void checkInToEvent(String qRCodeID, AttendeeCallBack attendeeCallBack){

        Map<String,Object> attendeeCheckedInEventsData = new HashMap<>();
        attendeeCheckedInEventsData.put("eventDate","tempDate");
        //this.ref.document(this.attendeeID).collection("CheckedIntoEvents").document(eventID).set(attendeeCheckedInEventsData);

        Map<String,Object> attendeeCheckedInCount = new HashMap<>();
        attendeeCheckedInCount.put("timesCheckedIn",1);

        //https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        // Use this to fetch specific document.
        DocumentReference docRef = db.collection("CheckInQRCode").document(qRCodeID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        eventID = document.get("event").toString();
                        checkInToEventID(eventID, attendeeCallBack);

                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    /**
     * Allows the attendee to leave an event they are checked into. Updates data in data base.
     *
     * @param eventID String of the event ID attempting to leave checkin of.
     */
    public void leaveEvent(String eventID){
        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("currentlyAtEvent", "no");
        db.collection("Users").document(attendeeID).collection("UpcomingEvents").document(eventID).delete();
        db.collection("Events").document(eventID).update("attendeeCount", FieldValue.increment(-1));


    }

    /**
     * Allows the attendee to leave an event they are signed up for. Updates data in data base.
     *
     * @param eventID String of the event ID attempting to leave signup of.
     */
    public void leaveSignUp(String eventID){

        db.collection("Events").document(eventID).collection("attendeeSignUps").document(attendeeID).delete();
        db.collection("Users").document(attendeeID).collection("SignedUpEvents").document(eventID).delete();
        db.collection("Events").document(eventID).update("attendeeSignUpCount", FieldValue.increment(-1));

    }


    /**
     * Allows attendee to signup for an event. this is only completed if the number of current signups is less than the total limit and the attendee
     * is not currently signed up for this event. Updates data in data base.
     *
     * @param eventID String of the event ID attempting to signup for.
     */
    public void signUpForEvent(String eventID){
        db.collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    Integer attendeeSignUpCount = documentSnapshot.getLong("attendeeSignUpCount").intValue();
                    Integer attendeeSignUpLimit = documentSnapshot.getLong("attendeeSignUpLimit").intValue();

                    // Attendee can  sign up, event isn't full
                    if (attendeeSignUpCount < attendeeSignUpLimit) {

                        ref.document(attendeeID).collection("SignedUpEvents").document(eventID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    String signUpInTime = Calendar.getInstance().getTime().toString();

                                    // Document exists, attendee has previously checked into the event (Checking into an event again)
                                    if (document.exists()) {}
                                    // Attendee has never checked into the event before
                                    else {
                                        Map<String, Object> attendeeCheckedInEventsData = new HashMap<>();
                                        attendeeCheckedInEventsData.put("signUpTime", signUpInTime);
                                        Map<String, Object> attendeeCheckedInCount = new HashMap<>();
                                        attendeeCheckedInCount.put("signUpTime", signUpInTime);

                                        ref.document(attendeeID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                DocumentSnapshot documentLocation = task.getResult();
                                                String locationAllowed = documentLocation.getString("locationAllowed").trim();

                                                if (locationAllowed.equals("yes")){
                                                    String latitude = documentLocation.getString("latitude");
                                                    String longitude = documentLocation.getString("longitude");
                                                    attendeeCheckedInCount.put("longitude", latitude);
                                                    attendeeCheckedInCount.put("latitude", longitude);

                                                }else{
                                                    attendeeCheckedInCount.put("longitude", null);
                                                    attendeeCheckedInCount.put("latitude", null);
                                                }

                                                ref.document(attendeeID).collection("SignedUpEvents").document(eventID).set(attendeeCheckedInEventsData);
                                                db.collection("Events").document(eventID).collection("attendeeSignUps").document(attendeeID).set(attendeeCheckedInCount);
                                                db.collection("Events").document(eventID).update("attendeeSignUpCount", FieldValue.increment(1));
                                            }
                                        });
                                    }
                                } else {
                                    Log.d(TAG, "Failed with: ", task.getException());
                                }
                            }
                        });
                    } else {
                    }
                }
            }
        });
    }

    /**
     * Checks if the attendee is already checked into an event. AttendeeCallBack is false if they are currently checked into the event.
     *
     * @param eventID String of the event ID being check if the attendee is already checked into,.
     * @param attendeeCallBack AttendeeCallBack used as a function pointer to retrieve result.
     */
    public void alreadyCheckedIn(String eventID, AttendeeCallBack attendeeCallBack){
        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null){
                    String checkedIn = documentSnapshot.getString("currentlyAtEvent");

                    if (checkedIn != null){
                        if (checkedIn.equals("yes")){
                            attendeeCallBack.checkInSuccess(false, "Already checked in");
                        }
                        else{
                            attendeeCallBack.checkInSuccess(true, "Not checked in");
                        }
                    }
                    else{
                        attendeeCallBack.checkInSuccess(true, "Not checked in");
                    }
                }
            }
        });
    }


    /**
     * Checks if the event signups are below the signup limit. AttendeeCallBack is true if there is space to signup.
     *
     * @param eventID String of the event ID being check if there is space available.
     * @param attendeeCallBack AttendeeCallBack used as a function pointer to retrieve result.
     */
    public void hasSignUpSpace(String eventID, AttendeeCallBack attendeeCallBack){
        db.collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){


                    Integer attendeeSignUpLimit = documentSnapshot.getLong("attendeeSignUpLimit").intValue();
                    Integer attendeeSignUpCount =documentSnapshot.getLong("attendeeSignUpCount").intValue();
                    if (attendeeSignUpLimit > attendeeSignUpCount){
                        attendeeCallBack.checkInSuccess(true, "Has space");
                    }
                    else{
                        attendeeCallBack.checkInSuccess(false, "Is full");
                    }

                }
                else{
                    attendeeCallBack.checkInSuccess(true, "Has space");
                }
            }
        });
    }

    /**
     * Checks if the attendee is already signed up for the event. AttendeeCallBack is false if they are currently signed up for the event.
     * @param eventID String of the event ID being check if the attendee is already signed up to.
     * @param attendeeCallBack AttendeeCallBack used as a function pointer to retrieve result.
     */
    public void alreadySignedUp(String eventID, AttendeeCallBack attendeeCallBack){
        db.collection("Events").document(eventID).collection("SignedUpEvents").document(attendeeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    attendeeCallBack.checkInSuccess(false, "Already signed up");
                }
                else{
                    attendeeCallBack.checkInSuccess(true, "Not signed in");
                }
            }
        });
    }


    /**
     * Checks attendee into an event if eligible,to be eligible the attendee cannot be checked into the event and the current amount of checkins must be less than the checkin limit.
     * Callback is true if the attendee was successfully checked into the event ie was eligible/
     *
     * @param eventID String event ID of the event attempting to check into.
     * @param attendeeCallBack AttendeeCallBack used as a function pointer to check if check-in was successful.
     */
    public void checkInToEventID(String eventID, AttendeeCallBack attendeeCallBack) {
        db.collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null) {
                    Integer attendeeCount = documentSnapshot.getLong("attendeeCount").intValue();
                    Integer attendeeLimit = documentSnapshot.getLong("attendeeLimit").intValue();
                    String eventName = documentSnapshot.getString("eventName");

                    // Attendee can check in, event isn't full
                    if (attendeeCount < attendeeLimit) {
                        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    String checkInTime = Calendar.getInstance().getTime().toString();
                                    Map<String, Object> attendeeCheckedInEventsData = new HashMap<>();
                                    attendeeCheckedInEventsData.put("checkInTime", checkInTime);

                                    // Document exists, attendee has previously checked into the event (Checking into an event again)
                                    if (document.exists()) {
                                        Log.d("AttendeeCheck", " document exists");
                                        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("timesCheckedIn", FieldValue.increment(1));
                                        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("currentlyAtEvent", "yes");
                                        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("checkInTime", checkInTime);
                                        db.collection("Events").document(eventID).update("attendeeCount", FieldValue.increment(1));
                                        ref.document(attendeeID).collection("UpcomingEvents").document(eventID).set(attendeeCheckedInEventsData);
                                        attendeeCallBack.checkInSuccess(true, eventName);

                                        ref.document(attendeeID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot documentLocation = task.getResult();
                                                String latitude = documentLocation.getString("latitude");
                                                String longitude = documentLocation.getString("longitude");
                                                db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("latitude", latitude);
                                                db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("longitude", longitude);
                                            }
                                        });
                                    }

                                    // Attendee has never checked into the event before
                                    else {
                                        Log.d("AttendeeCheck", " document DOES NOT exist");

                                        Map<String, Object> attendeeCheckedInCount = new HashMap<>();
                                        attendeeCheckedInCount.put("timesCheckedIn", 1);
                                        attendeeCheckedInCount.put("checkInTime", checkInTime);
                                        attendeeCheckedInCount.put("currentlyAtEvent", "yes");
                                        ref.document(attendeeID).collection("UpcomingEvents").document(eventID).set(attendeeCheckedInEventsData);
                                        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).set(attendeeCheckedInCount);
                                        db.collection("Events").document(eventID).update("attendeeCount", FieldValue.increment(1));
                                        attendeeCallBack.checkInSuccess(true, eventName);

                                        ref.document(attendeeID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                DocumentSnapshot documentLocation = task.getResult();
                                                String latitude = documentLocation.getString("latitude");
                                                String longitude = documentLocation.getString("longitude");
                                                db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("latitude", latitude);
                                                db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).update("longitude", longitude);
                                            }
                                        });
                                    }
                                } else {
                                    Log.d(TAG, "Failed with: ", task.getException());
                                    attendeeCallBack.checkInSuccess(false, eventName);
                                }
                            }
                        });
                    } else {
                        attendeeCallBack.checkInSuccess(false, eventName);
                    }
                }
            }
        });
    }


    /**
     * Returns a string representing the attendees device Android ID.
     * Reference: https://stackoverflow.com/questions/60503568/best-possible-way-to-get-device-id-in-android
     *
     * @return String of android device ID
     */
    public String getDeviceId() {
        String id = Settings.Secure.getString(this.mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
    }


    /**
     * Adds attendee to database.
     */
    private void addAttendeeToDatabase(){
        Map<String,Object> attendeeUpcomingEventsData = new HashMap<>();
        attendeeUpcomingEventsData.put("eventDate","temp");

        Map<String,Object> attendeeData = new HashMap<>();
        attendeeData.put("type",this.userType);
        attendeeData.put("name","unknown");
        attendeeData.put("phone","unknown");
        attendeeData.put("email","unknown");
        attendeeData.put("bio","unknown");
        attendeeData.put("profileImageURL","unknown");
        attendeeData.put("profileID", "unknown");
        attendeeData.put("locationAllowed", "no");

        new Profile(attendeeID);

        this.ref
                .document(this.attendeeID)
                .set(attendeeData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Firestore", "Attendee Data successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firestore", "ERROR: Attendee Data failed to upload.");
                    }
                });
    }



    /**
     * Checks if the attendee has already been added to the database, If not calls addAttendeeToDatabase().
     * Reference: https://stackoverflow.com/questions/53332471/checking-if-a-document-exists-in-a-firestore-collection
     */

    private void attendeeExists(){
        FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
        DocumentReference docIdRef = rootRef.collection("Users").document(attendeeID);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        rootRef.collection("Users").document(attendeeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null){
                                    //profileID = documentSnapshot.getString("profileID");
                                }
                            }
                        });
                        Log.d(TAG, "Attendee already exists!");

                    } else {
                        Log.d(TAG, "Attendee does not already exist!");
                        //profileID = FirebaseDatabase.getInstance().getReference("Profiles").push().getKey();
                        addAttendeeToDatabase();
                    }
                } else {
                    Log.d(TAG, "Failed with: ", task.getException());
                }

            }
        });
    }


    /**
     * Sets the attendee longitude and latitude in the database.
     *
     * @param location Address representing the location of the attendee
     */
    public void setLocation(Address location){

        this.location = location;

        Map<String,Object> locationData = new HashMap<>();
        // Location permission was denied
        if (location == null) {
            locationData.put("latitude",null);
            locationData.put("longitude",null);
        }
        // Location permission was enabled, data stored in firebase.
        else{
            String lat = String.valueOf(location.getLatitude());
            String lon = String.valueOf(location.getLongitude());
            locationData.put("latitude",lat);
            locationData.put("longitude",lon);
        }
        this.ref.document(this.attendeeID).update(locationData);
    }


    /**
     * Gets all the events an attendee is signed up for.
     * @param attendeeID
     * @param listener
     */
    public void getAllSignedUpEventIds(String attendeeID, final OnSignedUpEventsListener listener) {
        CollectionReference signedUpEventsRef = db.collection("Users").document(attendeeID).collection("SignedUpEvents");

        signedUpEventsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> eventIds = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {
                        // Add the document ID to the list
                        eventIds.add(document.getId());
                    }
                    // Pass the eventIds to the listener
                    listener.onSignedUpEventsRetrieved(eventIds);
                } else {
                    // Handle unsuccessful query
                    listener.onError("Error getting documents: " + task.getException().getMessage());
                }
            }
        });
    }


    /**
     * Used in getAllSignedUpEventIds()
     */
    public interface OnSignedUpEventsListener {
        void onSignedUpEventsRetrieved(List<String> eventIds);

        void onError(String errorMessage);
    }

    /**
     * Used as function pointer for getting events the attendee is signed up for.
     */
    public interface EventIdsCallback {
        void onCallback(List<String> eventIds);
    }
    public void event_ids(String attendeeID, EventIdsCallback callback){
        List<String> eventIds = new ArrayList<>();
        db.collection("Users").document(attendeeID).collection("SignedUpEvents").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()) {
                        eventIds.add(snapshot.getId()); // Assuming you want to store the document IDs
                    }
                    callback.onCallback(eventIds); // Use the callback to return the list
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here
                    Log.w("Firestore", "Error getting documents: ", e);
                });
    }

    /**
     * helps other method
     * @return List of events the attendee is signed up for
     */
    public List<String> return_events(){
        List<String> events_list = new ArrayList<>();
        event_ids(attendeeID, new EventIdsCallback() {
            @Override
            public void onCallback(List<String> eventIds) {
                // Handle the retrieved event IDs here
                if (eventIds.size() > 0){
                    for (String id : eventIds) {
                        events_list.add(id);
                        Log.d("Firestore", "Retrieved event ID: " + id);
                    }
                }


            }
        });
        Log.d("returned_events", " returning events " + events_list.size());
        return events_list;
    }

}
