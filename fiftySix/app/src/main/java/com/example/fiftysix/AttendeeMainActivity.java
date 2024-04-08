package com.example.fiftysix;

import static android.content.ContentValues.TAG;
import static androidx.core.content.FileProvider.getUriForFile;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import jp.wasabeef.picasso.transformations.CropCircleTransformation;

/**
 * Creates an attendee. Completes attendee tasks such as
 * 1. Allows attendee flip trough all pages in spinner, and profile.
 * 2. Allows Attendee to scan checkin QR Code or promo QR Code.
 * 3. Attendee can check into or signup to events.
 * 4. Attendee can create profile and upload image.
 * 5. Attendee can browse all events and view announcements.
 * 6. Attendee cen enable or disable geolocation.
 *
 * @author Rakshit, Brady, Arsh.
 * @version 1
 * @since SDK34
 */
public class AttendeeMainActivity extends AppCompatActivity {

    // Firebase
    private FirebaseFirestore db;
    private CollectionReference attCheckinEventRef;
    private CollectionReference eventRef;
    private CollectionReference attSignUpEventRef;
    private CollectionReference imageRef;

    // Layouts & views
    private ViewFlipper viewFlipper;
    private RecyclerView recyclerViewMyEvents, recyclerViewAllEvents, recyclerViewSignUpEvents;

    // Spinners for home page
    private Spinner myEventSpinner, allEventSpinner, myEventsSignUpSpinner, settingsSpinner;

    // Backend Misc
    private Attendee attendee;
    private String attendeeID, qrCodeID;
    private ArrayList<Event> myEventDataList, allEventDataList, signUpEventDataList;
    private AttendeeCheckInsEventAdapter attendeeCheckInsEventAdapter;
    private AttendeeSignUpsEventAdapter attendeeSignUpEventAdapter;
    private AttendeeAllEventAdapter attendeeAllEventAdapter;

    // Openening camera
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri cameraImageUri = null; // To store the camera image URI
    private Profile posterHandler;
    private Uri selectedImageUri = null;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    // Buttons on home pages
    private ImageButton attendeeNotificationButton;

    // Back Buttons
    private ImageButton buttonAttendeeBackAllEvents, buttonAttendeeBackCheckin, buttonAttendeeBackSignUp, buttonSettingsBack;

    // Buttons on Add event page
    private Button addEventScanCheckinButton;
    private ImageButton addEventBackImageButton;
    private Button browseEventsButton;

    // Profile edit page
    private ImageButton profileImage;
    private Button profileSave;
    private Button profileBack;
    private EditText profileName;
    private EditText profileEmail;
    private EditText profilePhoneNumber;
    private EditText profileBio;
    private Button profileRemovePic;

    // Settings
    private Switch locationSwitch;
    private ImageButton   buttonSettingsEventSignUp, buttonSettingsNotificationBell;

    // Add Event Buttons
    private ImageButton buttonAttendeeSignInEvent;
    private ImageButton buttonAttendeeSignInEventAllEvents;
    private ImageButton buttonAttendeeSignInEventSignUp;

    // Profile Buttons
    private ImageButton buttonAttendeeProfile, buttonAttendeeProfileSignUp, buttonAttendeeProfileAllEvents, buttonSettingsProfile;

    // Getting geolocation
    private LocationManager locationManager;
    private final static int REQUEST_CODE=100;
    FusedLocationProviderClient fusedLocationProviderClient;
    private int requestCode;
    private String[] permissions;
    private int[] grantResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        // View flipper, used to avoid opening new activities and keep the app running fast, Stores all of the layouts for attendee inside it.
        setContentView(R.layout.attendee_flipper);

        // Please add ALL FUTURE LAYOUTS to view flipper. this way we can switch layouts without switching activities and the app will run faster.
        viewFlipper = findViewById(R.id.attendeeFlipper);
        Context context = getApplicationContext();

        // Sets buttons and edit text for all of attendees layouts
        setButtons();
        setEditText();

        // Creates/Gets attendee
        getLocation();
        attendee = new Attendee(context);
        attendeeID = attendee.getDeviceId();

        // Adds Drop down menu to top right
        addSpinners();

        // Data base references commonly used
        setDataBaseRef();

        // Sets up array adapters for my and all events page
        setupEventAdapters();

        //________________________________________HomePage_______________________________________
        displayMyCheckins(); // updates users my events and all events recylcer views with live event data
        displayAllEvents();
        displayMySignUps();
        initializeHomePage(); // Sets up button functions on home page
        checkAnnouncements();

        //______________________________________Sign in to event Page_______________________________________
        initializeEventSignIn();

        // Geolocation stuff
        LocationManager locationMangaer = null;
        LocationListener locationListener = null;
        locationMangaer = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setupLocationSwitch();

        //________________________________________Profile________________________________________
        displayProfileData(); // Gets profile data and displays it from the data base.
        initializeProfileLayout(); // Adds onclick listeners and what should happen on click.



        // sets up camera and gallery launchers
        setUpLaunchers();
    }


    //________________________________________Methods________________________________________


    /**
     * Creates galleryLauncher and cameraLauncher used when opening camera or gallery
     */
    public void setUpLaunchers(){
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                    }
                }
        );
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && cameraImageUri != null) {
                        selectedImageUri = cameraImageUri;
                        // Now the selectedImageUri contains the URI of the captured image
                    }
                }
        );
    }


    /**
     * Checks if request codes for Camera, and geolocation are given. either opens camera or gets location when the respective permission is given.
     *
     * @param requestCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, open camera
                openCamera();
            } else {
                // Permission was denied
                Toast.makeText(this, "Camera permission is required to use the camera", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            if (requestCode == REQUEST_CODE) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                attendee.setLocation(null);
            }
        }
    }


    /**
     * Sets up geolocation switch in settings page. Store the position in the database when changed and sets the starting position based on database info.
     * Enables or disables geolocation for event sign ups depending on position and displays alert dialog when the switch is flipped or when the attendee first returns to the app after having location enabled.
     */
    private void setupLocationSwitch()  {
        db.collection("Users").document(attendeeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null){
                    String locationAllowed = documentSnapshot.getString("locationAllowed");
                    if (locationAllowed != null){
                        if (locationAllowed.equals("yes")){
                            locationSwitch.setChecked(true);
                        }
                        else{
                            locationSwitch.setChecked(false);
                        }
                    }
                }
            }
        });
        locationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AttendeeMainActivity.this);
                    builder.setTitle("Location Enabled");
                    builder.setMessage("Your sign-up location will be shared with event organizer upon sign-up.");

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.collection("Users").document(attendeeID).update("locationAllowed", "yes");
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            // Revert the switch to unchecked since canceled
                            locationSwitch.setChecked(false);
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            // Handle the case where the user cancels the dialog (e.g., by pressing the back button)
                            locationSwitch.setChecked(false);
                        }
                    });
                    builder.show();
                }
                else{
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(AttendeeMainActivity.this);
                    builder.setTitle("Location Disabled");
                    builder.setMessage("Your sign-up location will no longer shared with event organizer upon sign-up.");

                    // Set up the buttons
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.collection("Users").document(attendeeID).update("locationAllowed", "no");
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            // Revert the switch to unchecked since canceled
                            locationSwitch.setChecked(true);
                        }
                    });
                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            // Handle the case where the user cancels the dialog (e.g., by pressing the back button)
                            locationSwitch.setChecked(true);
                        }
                    });
                    builder.show();
                }
            }
        });
    }


    /**
     * Checks for event announcements or notifications from the event organizer, if the attendee has not received the notification previously then they will receive a push
     * notification containing the message sent by the organizer. calls notify_client() to make the push notification.
     */
    private void checkAnnouncements() {
        List<String> eventIds;
        attendee.event_ids(attendeeID, new Attendee.EventIdsCallback() {
            @Override
            public void onCallback(List<String> eventIds) {
                // Handle the retrieved event IDs here

                db.collection("Events").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (eventIds.size() > 0){
                            for (String id : eventIds) {

                                CollectionReference collectionRef  = db.collection("Events").document(id).collection("Notifications");
                                collectionRef.get().addOnCompleteListener(task -> {

                                    if (task.isSuccessful()) { // Check if the fetch was successful
                                        for (QueryDocumentSnapshot document : task.getResult()) { // Iterate through each document
                                            // getting the data stored in the attendees field which is a list of strigs
                                            String document_id = (String) document.getId();
                                            List<String> attendees = (List<String>) document.get("attendees");
                                            if (attendees != null) {
                                                // Process the list of attendees. This is a placeholder for whatever processing you need to do.
                                                if (attendees.contains(attendeeID)) {
                                                    // Found a matching ID
                                                    Log.d("attendeeId_announcement", " inside announcements function attendee");
                                                }
                                                else {
                                                    String notification = (String) document.get("notification");
                                                    String event_name = (String) document.get("event_name");
                                                    notify_client(document_id,notification,id,event_name);
                                                }
                                            }
                                            else {
                                                Log.d("new_notification","empty attendee list");
                                                String notification = (String) document.get("notification");
                                                String event_name = (String) document.get("event_name");
                                                notify_client(document_id,notification,id,event_name);
                                            }
                                        }
                                    } else { // Handle failures
                                        Log.w("Notification", "Error getting documents.", task.getException());
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Creates a push notification.
     *
     * @param id String of the notification ID
     * @param notify_message String of the message contained in the notification
     * @param event_id String of the event ID
     * @param event String of the event Name
     */
    private void notify_client(String id, String notify_message,String event_id,String event) {
        Log.d("checking notification", " inside announcements function attendee");

        Context context = getApplicationContext();
        sendNotification(context,event_id,event,notify_message);
        DocumentReference docRef = db.collection("Events").document(event_id).collection("Notifications").document(id);

        // Add attendee_id to the attendees list using arrayUnion to avoid duplicates
        docRef.update("attendees", FieldValue.arrayUnion(attendeeID))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Attendee added successfully to the document"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding attendee to the document", e));
    }

    /**
     * Sends the push notification created
     *
     * @param context Context application context
     * @param eventId String of the event ID
     * @param eventName String of the event Name
     * @param message String of the message contained in the notification
     */
    private void sendNotification(Context context, String eventId, String eventName, String message) {
        createNotificationChannel(context,eventId);
        // Get the notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Build the notification
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context,eventId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(eventName)
                    .setContentText(message)
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        // Show the notification
        notificationManager.notify(100, builder.build());
    }

    /**
     * Creates notification channel
     * @param context Context application context
     * @param eventId String of the event ID
     */
    private void createNotificationChannel(Context context,String eventId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(eventId, "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel Description");
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Checks for permission to get last known location,requests the permission if it is not already give.
     * Once it get permission it gets the last recorded location of the attendees android device.
     *
     * Reference "How to Get Current Location in Android Studio||Get user's current Location||Location App 2022" - by "Coding with Aiman" - Youtube.com
     */
    private void getLocation() {
        List<Address> addresses;
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(AttendeeMainActivity.this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(AttendeeMainActivity.this, Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            new Attendee(getApplicationContext()).setLocation(addresses.get(0));
                        }
                        catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
        else {
            ActivityCompat.requestPermissions(AttendeeMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }


    /**
     * Sets view flipper to specific index to display the corresponding home page showing MY events
     * @param v: View
     */
    private void myEventsView(View v){
        viewFlipper.setDisplayedChild(0);
    }
    /**
     * Sets view flipper to specific index to display the corresponding home page showing ALL events
     * @param v: View
     */
    private void allEventsView(View v){
        viewFlipper.setDisplayedChild(1);
    }

    /**
     * Sets view flipper to specific index to display the corresponding users profile page
     * @param v: View
     */
    private void profileView(View v){
        viewFlipper.setDisplayedChild(3);
    }

    /**
     * Sets view flipper to specific index to display the corresponding users event Signups page
     * @param v
     */
    private void signUpEventsView(View v) {viewFlipper.setDisplayedChild(4);}

    /**
     * Sets view flipper to specific index to display the corresponding users Settings page
     * @param v
     */
    private void settingsView(View v) {viewFlipper.setDisplayedChild(5);}

    /**
     * Shows dialog for when the user uploads an image
     */
    private void showImageSourceDialog() {
        CharSequence[] items = {"Upload from Gallery", "Upload from Camera"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Upload Profile Photo");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Upload from Gallery
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(intent);
                        startActivity(intent);
                        break;
                    case 1: // Upload from Camera
                        Log.d(TAG, "Attempting to launch camera.");
                        if (ContextCompat.checkSelfPermission(AttendeeMainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "RequestCameraPermissionCalled");
                            requestCameraPermission();
                        }
                        else {
                         //   Log.d(TAG, "Permission is already granted");
                            openCamera();
                        }
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * requests camera premission for when the camera is required
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    /**
     * Opens users device camera
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, "Error occurred while creating the file", ex);
                return;
            }
            Uri photoURI = getUriForFile(AttendeeMainActivity.this, "com.example.fiftysix.provider", photoFile);
            //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(photoURI);
        } else {
            Log.d(TAG, "No app can handle the camera intent.");
        }
    }

    /**
     * creates an image file
     * @return File: image JPEG
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        cameraImageUri = Uri.fromFile(image);
        return image;
    }

    /**
     *  Sets (findsviewbyid) buttons for all of attendee layouts
     */
    private void setButtons(){
        // Home page buttons
        attendeeNotificationButton = (ImageButton) findViewById(R.id.buttonAttendeeNotificationBelAllEvents);
        buttonAttendeeBackAllEvents = (ImageButton) findViewById(R.id.buttonAttendeeHomeAllEvents);
        buttonAttendeeBackCheckin = (ImageButton) findViewById(R.id.buttonAttendeeBackCheckin);
        buttonAttendeeBackSignUp = (ImageButton) findViewById(R.id.buttonAttendeeBackSignUp);

        // Add event page buttons
        addEventScanCheckinButton = (Button) findViewById(R.id.buttonAttendeeCheckinWithQR);
        browseEventsButton = (Button) findViewById(R.id.buttonAttendeeBrowseEvent);
        addEventBackImageButton = (ImageButton) findViewById(R.id.buttonAttendeeBackSignUp);

        // Profile tab buttons
        profileBack = (Button) findViewById(R.id.profile_back);
        profileSave = (Button) findViewById(R.id.profile_save);
        profileImage = (ImageButton) findViewById(R.id.profile_image);
        profileRemovePic = (Button) findViewById(R.id.profile_remove_pic);

        // Add event Buttons
        buttonAttendeeSignInEvent = (ImageButton) findViewById(R.id.buttonAttendeeSignInEvent);
        buttonAttendeeSignInEventAllEvents = (ImageButton) findViewById(R.id.buttonAttendeeSignInEventAllEvents);
        buttonAttendeeSignInEventSignUp = (ImageButton) findViewById(R.id.buttonAttendeeSignInEventSignUp);

        // Profile Buttons
        buttonAttendeeProfile = (ImageButton) findViewById(R.id.buttonAttendeeProfile);
        buttonAttendeeProfileSignUp = (ImageButton) findViewById(R.id.buttonAttendeeProfileSignUp);
        buttonAttendeeProfileAllEvents = (ImageButton) findViewById(R.id.buttonAttendeeProfileAllEvents);

        // Settings
        buttonSettingsProfile = findViewById(R.id.buttonSettingsProfile);
        buttonSettingsBack = findViewById(R.id.buttonSettingsBack);
        buttonSettingsEventSignUp = findViewById(R.id.buttonSettingsEventSignUp);
        buttonSettingsNotificationBell = findViewById(R.id.buttonSettingsNotificationBell);
        buttonAttendeeBackSignUp = findViewById(R.id.buttonAttendeeBack);
        locationSwitch = findViewById(R.id.locationSwitch);
    }

    /**
     *  Sets Edit for all of attendee layouts
     */
    private void setEditText() {
        profileName = (EditText) findViewById(R.id.profile_name);
        profileEmail = (EditText) findViewById(R.id.profile_email);
        profilePhoneNumber = (EditText) findViewById(R.id.profile_phone);
        profileBio = (EditText) findViewById(R.id.profile_home);
    }

    /**
     *  Scans QR Code and checks attendee into the event corresdponding to the barcode
     * Reference: "youtube - Implement Barcode QR Scanner in Android studio barcode reader | Cambo Tutorial" - youtube channel = Cambo Tutorial
     */
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
            qrCodeID = result.getContents().toString();
            db.collection("CheckInQRCode").document(qrCodeID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        String qrType = documentSnapshot.getString("type");
                        String eventID = documentSnapshot.getString("event");

                        if (qrType != null){
                            // Is a Promo QR Code

                            if (qrType.equals("promo")){
                                //TODO:  Open & Display event
                                Context context = getApplicationContext();
                                Intent intent = new Intent(AttendeeMainActivity.this, PromoScanActivity.class);
                                intent.putExtra("eventID", eventID);
                                startActivity(intent);
                            }
                            // Is a checkin QR Code
                            else{
                                attendee.alreadyCheckedIn(eventID, new Attendee.AttendeeCallBack() {
                                    @Override
                                    public void checkInSuccess(Boolean checkinSuccess, String eventName) {
                                        if (checkinSuccess){
                                            attendee.checkInToEvent(qrCodeID, new Attendee.AttendeeCallBack() {
                                                @Override
                                                public void checkInSuccess(Boolean checkinSuccess, String eventName) {
                                                    if (checkinSuccess){
                                                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeMainActivity.this);
                                                        builder.setTitle("Check-in Successful");
                                                        builder.setMessage(eventName);
                                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                                    }
                                                    else {
                                                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeMainActivity.this);
                                                        builder.setTitle("Check-in Failed");
                                                        builder.setMessage(eventName + " is at max capacity");
                                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        }).show();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeMainActivity.this);
                                            builder.setTitle("Check-in Failed");
                                            builder.setMessage("User already checked into this event.");
                                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    });

    /**
     *  Loads and displays event data that the attendee is currently signed up for from firebase,
     */
    private void displayMyCheckins(){
        // Adds events from database to the attendee home screen. Will only show events the attendee has signed up for.
        FirebaseFirestore.getInstance().collection("Users").document(attendeeID).collection("UpcomingEvents").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                myEventDataList.clear();

                if (value.isEmpty()){
                    String hardCode = "No Current Checked-Ins";
                    myEventDataList.add(new Event(null, hardCode, hardCode, hardCode, hardCode, hardCode, hardCode, hardCode, 0, 0, 0, 0, hardCode));
                    attendeeCheckInsEventAdapter.notifyDataSetChanged();
                }
                else {
                    for (QueryDocumentSnapshot v : value) {
                        String eventID = v.getId();
                        FirebaseFirestore.getInstance().collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null) {

                                    String eventName = documentSnapshot.getString("eventName");
                                    String location = documentSnapshot.getString("location");
                                    String details = documentSnapshot.getString("details");
                                    String posterID = documentSnapshot.getString("posterID");
                                    String startDate = documentSnapshot.getString("startDate");
                                    String startTime = documentSnapshot.getString("startTime");
                                    String endDate = documentSnapshot.getString("endDate");
                                    String endTime = documentSnapshot.getString("endTime");

                                    if (documentSnapshot.getLong("attendeeSignUpCount") != null) {

                                        Integer attendeeCheckinLimitIn = documentSnapshot.getLong("attendeeLimit").intValue();
                                        Integer attendeeSignUpLimitIn = documentSnapshot.getLong("attendeeSignUpLimit").intValue();
                                        Integer inAttendeeCount = documentSnapshot.getLong("attendeeCount").intValue();
                                        Integer signUpCount = documentSnapshot.getLong("attendeeSignUpCount").intValue();
                                        db.collection("PosterImages").document(posterID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                                if (documentSnapshot2 != null) {
                                                    String imageUrl = documentSnapshot2.getString("image");
                                                    myEventDataList.add(new Event(eventID, eventName, location, startDate, endDate, startTime, endTime, details, inAttendeeCount, signUpCount, attendeeCheckinLimitIn, attendeeSignUpLimitIn, imageUrl));
                                                    attendeeCheckInsEventAdapter.notifyDataSetChanged();
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

    /**
     * Gets all events that the attendee is signed up for, getting all event details then displaying the events by adding them to signUpEventDataList
     * and using attendeeSignUpEventAdapter
     */
    private void displayMySignUps() {
        // Adds events from database to the attendee home screen. Will only show events the attendee has signed up for.
        FirebaseFirestore.getInstance().collection("Users").document(attendeeID).collection("SignedUpEvents").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                signUpEventDataList.clear();

                if (value.isEmpty()) {
                    String hardCode = "No Current Sign-Ups";
                    signUpEventDataList.add(new Event(null, hardCode, hardCode, hardCode, hardCode, hardCode, hardCode, hardCode, 0, 0, 0, 0, hardCode));
                    attendeeSignUpEventAdapter.notifyDataSetChanged();
                }
                else {
                    for (QueryDocumentSnapshot v : value) {
                        String eventID = v.getId();
                        FirebaseFirestore.getInstance().collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null) {

                                    String eventName = documentSnapshot.getString("eventName");
                                    String location = documentSnapshot.getString("location");
                                    String details = documentSnapshot.getString("details");
                                    String posterID = documentSnapshot.getString("posterID");
                                    String startDate = documentSnapshot.getString("startDate");
                                    String startTime = documentSnapshot.getString("startTime");
                                    String endDate = documentSnapshot.getString("endDate");
                                    String endTime = documentSnapshot.getString("endTime");

                                    if (documentSnapshot.getLong("attendeeSignUpCount") != null) {
                                        Integer attendeeCheckinLimitIn = documentSnapshot.getLong("attendeeLimit").intValue();
                                        Integer attendeeSignUpLimitIn = documentSnapshot.getLong("attendeeSignUpLimit").intValue();
                                        Integer inAttendeeCount = documentSnapshot.getLong("attendeeCount").intValue();
                                        Integer signUpCount = documentSnapshot.getLong("attendeeSignUpCount").intValue();
                                        db.collection("PosterImages").document(posterID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot2) {
                                                if (documentSnapshot2 != null) {
                                                    if (endDate != null) {
                                                        // TODO: Finish this/ test
                                                        // From https://stackoverflow.com/questions/10774871/best-way-to-compare-dates-in-android
                                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                                                        Date strDate = null;
                                                        try {
                                                            strDate = sdf.parse(endDate);
                                                        } catch (ParseException e) {
                                                            throw new RuntimeException(e);
                                                        }
                                                        if (new Date().after(strDate)) {
                                                            //Event is old
                                                        } else {
                                                            String imageUrl = documentSnapshot2.getString("image");
                                                            signUpEventDataList.add(new Event(eventID, eventName, location, startDate, endDate, startTime, endTime, details, inAttendeeCount, signUpCount, attendeeCheckinLimitIn, attendeeSignUpLimitIn, imageUrl));
                                                            attendeeSignUpEventAdapter.notifyDataSetChanged();
                                                        }
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
            }
        });
    }

    /**
     *  Loads and displays event data that the attendee is currently signed up for from firebase,
     */
    private void displayAllEvents(){
        // Displays all events
        eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (value.isEmpty()){
                    allEventDataList.clear();
                    if (allEventDataList.isEmpty()) {
                        String hardCode = "No Upcoming Events :(";
                        allEventDataList.add(new Event(null, hardCode, hardCode, hardCode, hardCode, hardCode, hardCode, hardCode, 0, 0, 0, 0, hardCode));
                        attendeeAllEventAdapter.notifyDataSetChanged();
                    }
                }
                else{
                    allEventDataList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        if (doc.getId() != "temp") {
                            String eventID = doc.getId();
                            String eventName = doc.getString("eventName");
                            String imageUrl = doc.getString("posterURL");
                            String location = doc.getString("location");
                            String details = doc.getString("details");
                            String posterID = doc.getString("posterID");
                            String startDate = doc.getString("startDate");
                            String startTime = doc.getString("startTime");
                            String endDate = doc.getString("endDate");
                            String endTime = doc.getString("endTime");

                            if (doc.getLong("attendeeSignUpCount") != null) {
                                Integer attendeeCheckinLimitIn = doc.getLong("attendeeLimit").intValue();
                                Integer attendeeSignUpLimitIn = doc.getLong("attendeeSignUpLimit").intValue();
                                Integer inAttendeeCount = doc.getLong("attendeeCount").intValue();
                                Integer signUpCount = doc.getLong("attendeeSignUpCount").intValue();

                                db.collection("PosterImages").document(posterID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                        if (value != null) {
                                            if (endDate != null) {
                                                // TODO: Finish this/ test
                                                // From https://stackoverflow.com/questions/10774871/best-way-to-compare-dates-in-android
                                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                                                Date strDate = null;
                                                try {
                                                    strDate = sdf.parse(endDate);
                                                } catch (ParseException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                if (new Date().after(strDate)) {
                                                    //Event is old
                                                } else {
                                                    String imageUrl = value.getString("image");
                                                    allEventDataList.add(new Event(eventID, eventName, location, startDate, endDate, startTime, endTime, details, inAttendeeCount, signUpCount, attendeeCheckinLimitIn, attendeeSignUpLimitIn, imageUrl));
                                                    attendeeAllEventAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     *  Sets all onClicks for buttons, in the profile page, allows editing data and changing/deleting the photos
     */
    private void initializeProfileLayout(){
        profileSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = String.valueOf(profileName.getText());
                String email = String.valueOf(profileEmail.getText());
                String phone = String.valueOf(profilePhoneNumber.getText());
                String bio = String.valueOf(profileBio.getText());
                Profile profile = new Profile(attendee.getDeviceId());
                profile.editProfile(attendee.getDeviceId() , name, email, phone, bio);
                if (selectedImageUri != null) {
                    posterHandler.uploadImageAndStoreReference(selectedImageUri, new Profile.ProfileUploadCallback() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            if (imageUrl != null) {
                                // Stores image URL
                                posterHandler.storeImageInUser(imageUrl);
                                Log.d(TAG, "IMAGE URL PROFILE = " + imageUrl);
                                Picasso.get()
                                        .load(imageUrl)
                                        .fit().transform(new CropCircleTransformation())
                                        .into(profileImage);
                            }
                        }
                        @Override
                        public void onUploadFailure(Exception e) {
                            Log.d(TAG, "IMAGE URL PROFILE = Failed");
                        }
                    });
                }
                Picasso.get()
                        .load(profile.getProfileURL())
                        .fit().transform(new CropCircleTransformation())
                        .into(profileImage);
            }
        });
        posterHandler = new Profile(attendee.getDeviceId());

        profileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEventsView(v);
            }
        });

        profileRemovePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Users").document(attendee.getDeviceId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String name  = documentSnapshot.getString("name");
                        db.collection("Users").document(attendee.getDeviceId()).update("profileImageURL", "https://ui-avatars.com/api/?rounded=true&name="+ name +"&background=random&size=512");
                    }
                });
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageSourceDialog();
            }
        });

    }

    /**
     *  Loads and displays profile data for the attendee from firebase,
     */
    private void displayProfileData(){
        db.collection("Users").document(attendee.getDeviceId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){

                    String imageURL = value.getString("profileImageURL");
                    String name = value.getString("name");
                    String email = value.getString("email");
                    String phone = value.getString("phone");
                    String bio = value.getString("bio");

                    if (name != null && !name.equals("unknown")){
                        profileName.setText(name);
                    }
                    if (email != null && !email.equals("unknown")){
                        profileEmail.setText(email);
                    }
                    if (phone != null && !phone.equals("unknown")){
                        profilePhoneNumber.setText(phone);
                    }
                    if (email != null && !email.equals("unknown")){
                        profileBio.setText(bio);
                    }
                    if (imageURL != null && imageURL.equals("unknown")){
                        imageURL = "https://firebasestorage.googleapis.com/v0/b/fiftysix-a4bcf.appspot.com/o/images%2FDoNotDeleteStockProfilePic%2Fprofile.png?alt=media&token=19c78f81-176c-4d93-817d-4a789d5c8cd2";
                    }

                    Picasso.get()
                            .load(imageURL)
                            .fit().transform(new CropCircleTransformation())
                            .into(profileImage);
                }
            }
        });
    }

    /**
     *  Sets all onClicks for buttons, in the profile page, allows the attendee to scan a QR code to check in to an event
     */
    private void initializeEventSignIn(){
        addEventBackImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myEventsView(v);
            }
        });

        addEventScanCheckinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
                myEventsView(v);
            }
        });
    }

    /**
     *  Sets all onClicks for buttons, in the profile page, allows the attendee to scan a QR code to check in to an event
     */
    private void initializeHomePage(){

        //_______________________ Profile Buttons ______________________________________
        buttonAttendeeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileView(v);
            }
        });
        buttonAttendeeProfileSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileView(v);
            }
        });
        buttonAttendeeProfileAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileView(v);
            }
        });
        buttonSettingsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileView(v);
            }
        });

        //________________________ Notifications _______________________________________

        attendeeNotificationButton.setOnClickListener(v -> {
            //startActivity(new Intent(AttendeeMainActivity.this, Notification.class));
        });
        buttonSettingsNotificationBell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //________________________ Add Event Buttons ___________________________________
        buttonAttendeeSignInEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
                myEventsView(v);
            }
        });
        buttonAttendeeSignInEventAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
                myEventsView(v);
            }
        });
        buttonAttendeeSignInEventSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
                myEventsView(v);
            }
        });
        buttonSettingsEventSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanCode();
                myEventsView(v);
            }
        });

        //________________________ Return to MainActivity Buttons ___________________________________
        buttonAttendeeBackAllEvents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonAttendeeBackCheckin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonAttendeeBackSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonSettingsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     *  Sets up the event adapters to display the event data in the recycler views of the home page
     */
    private void setupEventAdapters(){
        myEventDataList = new ArrayList<>();
        allEventDataList = new ArrayList<>();
        signUpEventDataList = new ArrayList<>();

        recyclerViewMyEvents = findViewById(R.id.attendeeHomeRecyclerView);
        recyclerViewAllEvents = findViewById(R.id.attendeeHomeRecyclerViewAllEvents);
        recyclerViewSignUpEvents = findViewById(R.id.attendeeHomeRecyclerViewSignUp);

        attendeeCheckInsEventAdapter = new AttendeeCheckInsEventAdapter(myEventDataList, getApplicationContext());
        recyclerViewMyEvents.setAdapter(attendeeCheckInsEventAdapter);
        recyclerViewMyEvents.setHasFixedSize(false);

        attendeeAllEventAdapter = new AttendeeAllEventAdapter(allEventDataList, getApplicationContext());
        recyclerViewAllEvents.setAdapter(attendeeAllEventAdapter);
        recyclerViewAllEvents.setHasFixedSize(false);

        attendeeSignUpEventAdapter = new AttendeeSignUpsEventAdapter(signUpEventDataList, getApplicationContext());
        recyclerViewSignUpEvents.setAdapter(attendeeSignUpEventAdapter);
        recyclerViewSignUpEvents.setHasFixedSize(false);
    }

    /**
     *  Sets up common database reference
     */
    private void setDataBaseRef(){
        db = FirebaseFirestore.getInstance();
        attCheckinEventRef = db.collection("Users").document(attendee.getDeviceId()).collection("UpcomingEvents");
        attSignUpEventRef = db.collection("Users").document(attendee.getDeviceId()).collection("SignedUpEvents");
        eventRef = db.collection("Events");
        imageRef = db.collection("Images");
    }

    /**
     * Sets up the four spinners on the four main layouts.
     */
    private void addSpinners(){
        // Adds Drop down menu to top left
        myEventSpinner = findViewById(R.id.menuButtonMyEvents);
        allEventSpinner = findViewById(R.id.menuButtonAllEvents);
        myEventsSignUpSpinner = findViewById(R.id.menuButtonMyEventsSignUp);
        settingsSpinner = findViewById(R.id.menuButtonSettings);

        settingsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                // TODO: Split checkin and signUps
                if(position == 0){
                    myEventsView(view);
                    myEventSpinner.setSelection(0);
                    allEventSpinner.setSelection(0);
                    myEventsSignUpSpinner.setSelection(0);
                }
                else if(position == 1){
                    signUpEventsView(view);
                    myEventSpinner.setSelection(1);
                    allEventSpinner.setSelection(1);
                    myEventsSignUpSpinner.setSelection(1);
                }
                else if(position == 2){
                    allEventsView(view);
                    myEventSpinner.setSelection(2);
                    allEventSpinner.setSelection(2);
                    myEventsSignUpSpinner.setSelection(2);
                }
                // TODO: Make a settings page
                else if(position == 3){
                    settingsView(view);
                    myEventSpinner.setSelection(3);
                    allEventSpinner.setSelection(3);
                    myEventsSignUpSpinner.setSelection(3);
                }
                ((TextView)view).setText(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        myEventsSignUpSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                // TODO: Split checkin and signUps
                if(position == 0){
                    myEventsView(view);
                    myEventSpinner.setSelection(0);
                    allEventSpinner.setSelection(0);
                    settingsSpinner.setSelection(0);
                }
                else if(position == 1){
                    signUpEventsView(view);
                    myEventSpinner.setSelection(1);
                    allEventSpinner.setSelection(1);
                    settingsSpinner.setSelection(1);
                }
                else if(position == 2){
                    allEventsView(view);
                    myEventSpinner.setSelection(2);
                    allEventSpinner.setSelection(2);
                    settingsSpinner.setSelection(2);
                }
                // TODO: Make a settings page
                else if(position == 3){
                    settingsView(view);
                    myEventSpinner.setSelection(3);
                    allEventSpinner.setSelection(3);
                    settingsSpinner.setSelection(3);
                }
                ((TextView)view).setText(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        allEventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                // TODO: Split checkin and signUps
                if(position == 0){
                    myEventsView(view);
                    myEventSpinner.setSelection(0);
                    myEventsSignUpSpinner.setSelection(0);
                    settingsSpinner.setSelection(0);
                }
                else if(position == 1){
                    signUpEventsView(view);
                    myEventSpinner.setSelection(1);
                    myEventsSignUpSpinner.setSelection(1);
                    settingsSpinner.setSelection(1);
                }
                else if(position == 2){
                    allEventsView(view);
                    myEventSpinner.setSelection(2);
                    myEventsSignUpSpinner.setSelection(2);
                    settingsSpinner.setSelection(2);
                }
                // TODO: Make a settings page
                else if(position == 3){
                    settingsView(view);
                    myEventSpinner.setSelection(3);
                    myEventsSignUpSpinner.setSelection(3);
                    settingsSpinner.setSelection(3);
                }
                ((TextView)view).setText(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        myEventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                // TODO: Split checkin and signUps
                if(position == 0){
                    myEventsView(view);
                    myEventsSignUpSpinner.setSelection(0);
                    allEventSpinner.setSelection(0);
                    settingsSpinner.setSelection(0);
                }
                else if(position == 1){
                    signUpEventsView(view);
                    myEventsSignUpSpinner.setSelection(1);
                    allEventSpinner.setSelection(1);
                    settingsSpinner.setSelection(1);
                }
                else if(position == 2){
                    allEventsView(view);
                    myEventsSignUpSpinner.setSelection(2);
                    allEventSpinner.setSelection(2);
                    settingsSpinner.setSelection(2);
                }
                // TODO: Make a settings page
                else if(position == 3){
                    settingsView(view);
                    myEventsSignUpSpinner.setSelection(3);
                    allEventSpinner.setSelection(3);
                    settingsSpinner.setSelection(3);
                }
                ((TextView)view).setText(null);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Event Check-ins");
        arrayList.add("Event Sign-ups");
        arrayList.add("Browse All Events");
        arrayList.add("Settings");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        myEventSpinner.setAdapter(adapter);
        allEventSpinner.setAdapter(adapter);
        myEventsSignUpSpinner.setAdapter(adapter);
        settingsSpinner.setAdapter(adapter);
    }


}
