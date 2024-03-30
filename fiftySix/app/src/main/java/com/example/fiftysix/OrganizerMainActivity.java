package com.example.fiftysix;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.Manifest;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;

import com.google.firebase.firestore.model.Document;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.nikartm.support.ImageBadgeView;

public class OrganizerMainActivity extends AppCompatActivity {

    // Firebase
    private FirebaseFirestore db;
    private CollectionReference orgEventRef;
    private CollectionReference eventRef;
    private CollectionReference imageRef;


    // Layouts & views
    private ViewFlipper viewFlipper;
    private RecyclerView recyclerView;


    //Organizer Data
    private Organizer organizer;
    private String reUseQRID;
    private String organizerID;
    private OrganizerEventAdapter organizerEventAdapter;




    private MilestoneAdapter milestoneAdapter;
    private ArrayList<MileStone> mileStoneDataList;





    private ArrayList<Event> eventDataList;
    private int attendeeLimit = Integer.MAX_VALUE;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri cameraImageUri = null; // To store the camera image URI
    private Poster posterHandler;
    private Uri selectedImageUri = null;
    private static final int REQUEST_CAMERA_PERMISSION = 201;


    // Buttons on home pages
    private ImageButton addEventButton;
    private ImageButton orgProfileButton;
    private ImageButton orgNotificationButton;
    private ImageButton orgHomeButton;

    private ImageButton backOrgNotif;
    private ImageBadgeView notifBadge;


    // Buttons on Create event page
    private Button createEvent;
    private ImageButton eventDetailsBack;
    private Button reuseCheckInQR;
    private EditText eventTitleEditText;
    private EditText eventDateEditText;
    private EditText eventAddressEditText;
    private EditText eventDetailsEditText;
    private Switch switchAttendeeLimit;


    // Buttons on Upload QR page
    private Button uploadQRFromScan;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.organizer_flipper);
        viewFlipper = findViewById(R.id.organizerFlipper);
        Context context = getApplicationContext();
        recyclerView = findViewById(R.id.orgHomeRecyclerView);

        // Creates Organizer Object
        organizer = new Organizer(context);
        organizerID = organizer.getOrganizerID();

        // Firebase
        db = FirebaseFirestore.getInstance();
        orgEventRef = db.collection("Users").document(organizer.getOrganizerID()).collection("EventsByOrganizer");
        eventRef = db.collection("Events");
        imageRef = db.collection("Images");

        setButtons();
        setEditText();
        setupAttendeeLimitSwitch();

        eventDataList = new ArrayList<>();

        // Sets home page recyler view event data
        organizerEventAdapter = new OrganizerEventAdapter(eventDataList, this);
        recyclerView.setAdapter(organizerEventAdapter);
        recyclerView.setHasFixedSize(false);

        // Creates Poster Object
        posterHandler = new Poster();


        // Milestone Stuff
        ListView mileStoneListView = (ListView) findViewById(R.id.milestoneListView);
        mileStoneDataList = new ArrayList<MileStone>();
        milestoneAdapter = new MilestoneAdapter(this, R.layout.organizer_notification_list_item, mileStoneDataList);
        mileStoneListView.setAdapter(milestoneAdapter);
        notifBadge = findViewById(R.id.notifBadge);
        notifBadge.setVisibility(View.INVISIBLE);
        mileStoneListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MileStone mile = milestoneAdapter.getItem(position);

                new AlertDialog.Builder(OrganizerMainActivity.this)
                        .setTitle("Delete Notification")
                        .setMessage("Are you sure you want to delete this notification?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mile.hideMilestone();
                                mileStoneDataList.remove(position);
                                milestoneAdapter.notifyDataSetChanged();
                                notifBadge.setBadgeValue(mileStoneDataList.size());
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });



        // Adds events from database to the organizers home screen. Will only show events created by the organizer
        loadOrganizerEvents();


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



        // Opens page to create event
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImageUri = null;
                nextView(v);
            }
        });


        initializeCreateEvent();


        // Create event pages

        orgNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationView(v);
            }

        });

        backOrgNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeView(v);
            }
        });
    }



    //________________________________________Methods________________________________________






        private void showImageSourceDialog() {
        CharSequence[] items = {"Upload from Gallery", "Upload from Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Poster");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @SuppressLint("QueryPermissionsNeeded")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Upload from Gallery
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryLauncher.launch(intent);
                        break;
                    case 1: // Upload from Camera
                        Log.d(TAG, "Attempting to launch camera.");
                        if (ContextCompat.checkSelfPermission(OrganizerMainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "RequestCameraPermissionCalled");
                            requestCameraPermission();
                        } else {
                            Log.d(TAG, "Permission is already granted");
                            openCamera();
                        }
                        break;
                }
            }
        });
        builder.show();
    }






        private void requestCameraPermission() {
        ActivityCompat.requestPermissions(OrganizerMainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

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
    }

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
            Uri photoURI = FileProvider.getUriForFile(OrganizerMainActivity.this, "com.example.fiftysix.fileProvider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(photoURI);
        } else {
            Log.d(TAG, "No app can handle the camera intent.");
        }
    }


        private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        cameraImageUri = Uri.fromFile(image);
        return image;
        }


        public void homeView(View v){
            viewFlipper.setDisplayedChild(0);;
        }

        public void createEventView(View v){
            viewFlipper.setDisplayedChild(1);;
        }

        public void reuseQRView(View v){
            viewFlipper.setDisplayedChild(2);;
        }

        public void notificationView(View v){
            viewFlipper.setDisplayedChild(3);;
        }


        public void previousView(View v){
        //viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        //viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
        viewFlipper.showPrevious();
        }

        private void nextView(View v){
        //viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        //viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        viewFlipper.showNext();
        }


        private void setButtons() {
            // Home page buttons
            addEventButton = (ImageButton) findViewById(R.id.buttonAddEvent);
            orgProfileButton = (ImageButton) findViewById(R.id.buttonOrganizerProfile);
            orgNotificationButton = (ImageButton) findViewById(R.id.notification_button);
            orgHomeButton = (ImageButton) findViewById(R.id.button_organizer_home);
            // Create event page buttons
            reuseCheckInQR = (Button) findViewById(R.id.reuseCheckinQR);
            createEvent = (Button) findViewById(R.id.buttonCreateEvent);
            eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackCreateEvent);
            ImageButton eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackUploadQR);
            uploadQRFromScan = (Button) findViewById(R.id.EditEvent);
            switchAttendeeLimit = findViewById(R.id.switchAttendeeLimit);
            //eventPosterImage = findViewById(R.id.event_poster_image);

            backOrgNotif = (ImageButton) findViewById(R.id.backOrgNotif);

        }


        private void setEditText() {
            eventTitleEditText = (EditText) findViewById(R.id.eventNameEditText);
            eventDateEditText = (EditText) findViewById(R.id.eventDateEditText);
            eventAddressEditText = (EditText) findViewById(R.id.eventAddressEditText);
            eventDetailsEditText = (EditText) findViewById(R.id.eventDetailsEditText);
        }



        private void setupAttendeeLimitSwitch() {
            switchAttendeeLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    if (isChecked) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrganizerMainActivity.this);
                        builder.setTitle("Set Attendee Limit");

                        // Set up the input
                        final EditText input = new EditText(OrganizerMainActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_NUMBER);
                        builder.setView(input);

                        // Set up the buttons
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    int limit = Integer.parseInt(input.getText().toString());
                                    if (limit > 0) {
                                        attendeeLimit = limit;
                                    } else {
                                        // Invalid input, revert the switch to unchecked
                                        switchAttendeeLimit.setChecked(false);
                                        attendeeLimit = Integer.MAX_VALUE;
                                    }
                                } catch (NumberFormatException e) {
                                    switchAttendeeLimit.setChecked(false);
                                    attendeeLimit = Integer.MAX_VALUE;
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                // Revert the switch to unchecked since canceled
                                switchAttendeeLimit.setChecked(false);
                            }
                        });

                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialogInterface) {
                                // Handle the case where the user cancels the dialog (e.g., by pressing the back button)
                                switchAttendeeLimit.setChecked(false);
                            }
                        });

                        builder.show();
                    } else {
                        attendeeLimit = Integer.MAX_VALUE;
                    }
                }
            });
        }


        // "youtube - Implement Barcode QR Scanner in Android studio barcode reader | Cambo Tutorial" - youtube channel = Cambo Tutorial
        private void scanCode () {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Volume up to turn on flash");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);
            barLauncher.launch(options);
        }
        ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {

            if (result.getContents() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(OrganizerMainActivity.this);
                builder.setTitle("Result");
                builder.setMessage(result.getContents());
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reUseQRID = result.getContents().toString();
                        String eventTitle = eventTitleEditText.getText().toString();
                        String eventDate = eventDateEditText.getText().toString();
                        String eventAddress = eventAddressEditText.getText().toString();
                        String eventDetails = eventDetailsEditText.getText().toString();

                        //Integer eventAttendeeLimit = Integer.parseInt(eventAttendeeLimitEditText.getText().toString());
                        organizer.createEventReuseQRCode(eventDetails, eventAddress, 100, eventTitle, reUseQRID);

                        dialog.dismiss();
                    }
                }).show();
            }

        });


        private void loadOrganizerEvents(){

            eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("Firestore", error.toString());
                        return;
                    }
                    if (value != null){
                        eventRef.whereEqualTo("organizer", organizerID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (querySnapshot != null) {
                                    eventDataList.clear();
                                    for (QueryDocumentSnapshot doc : querySnapshot) {

                                        String eventID = doc.getId();
                                        String eventName = doc.getString("eventName");
                                        String posterID = doc.getString("posterID");
                                        Integer inAttendeeLimit = doc.getLong("attendeeLimit").intValue();
                                        Integer inAttendeeCount = doc.getLong("attendeeCount").intValue();
                                        Integer signUpCount = doc.getLong("attendeeSignUpCount").intValue();
                                        String inDate = doc.getString("date");
                                        String location = doc.getString("location");
                                        String details = doc.getString("details");
                                        addAtttendanceMilestoneUpdates(eventID, eventName, signUpCount, inAttendeeLimit);

                                        db.collection("PosterImages").whereEqualTo("poster", posterID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot querySnapshotImage) {
                                                for (QueryDocumentSnapshot doc : querySnapshotImage){

                                                    Log.e("Added to list", "onSuccess: Event has been added to eventDataList, OrganizerMain");
                                                    String posterURL = doc.getString("image");
                                                    eventDataList.add(new Event(eventID, eventName, location, inDate, details, inAttendeeCount, signUpCount, inAttendeeLimit, posterURL));
                                                    organizerEventAdapter.notifyDataSetChanged();
                                                }

                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            });

        }



        private void addAtttendanceMilestoneUpdates(String eventID, String eventName, Integer checkIns, Integer attendeeLimit){
            String message;

            // Makes milestones based on number of signups
            if (checkIns == 1){
                message = "Congratulations, your first attendee has arrived.";
                MileStone milestone = new MileStone(organizerID, eventID, eventName, message, checkIns.toString());
                milestone.addToDatabase();
                db.collection("Users").document(organizerID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(checkIns.toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null){
                            if (documentSnapshot.getBoolean("viewable") != null){
                                Boolean view = documentSnapshot.getBoolean("viewable").booleanValue();
                                if (view == true){
                                    mileStoneDataList.add(milestone);
                                    milestoneAdapter.notifyDataSetChanged();
                                    notifBadge.setBadgeValue(mileStoneDataList.size());
                                }
                            }
                        }
                    }
                });
            }
            else{
                int [] attendeeCountMilestones = { 25, 50, 100, 250, 500, 1000, 1500, 2000, 2500, 3000, 4000, 5000, 7500, 10000 };
                for (int i = 0; i < attendeeCountMilestones.length; i++ ){
                    if (checkIns == attendeeCountMilestones[i]){
                        notifBadge.setVisibility(View.VISIBLE);
                        message = "Congratulations, " + checkIns.toString() + "  attendees have arrived.";
                        MileStone milestone = new MileStone(organizerID, eventID, eventName, message, checkIns.toString());
                        milestone.addToDatabase();
                        db.collection("Users").document(organizerID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(checkIns.toString()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot != null){
                                    if (documentSnapshot.getBoolean("viewable") != null){
                                        Boolean view = documentSnapshot.getBoolean("viewable").booleanValue();
                                        if (view == true){
                                            mileStoneDataList.add(milestone);
                                            milestoneAdapter.notifyDataSetChanged();
                                            notifBadge.setBadgeValue(mileStoneDataList.size());
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }

            // Makes milestones based on percent of capacity filled by signups
            double [] capacityPercentMilestones = { (attendeeLimit*0.25), (attendeeLimit*0.5), (attendeeLimit*0.75)};
            String [] percentageFull = {"25%", "50%", "75%"};
            for (int i = 0; i < capacityPercentMilestones.length; i++ ){
                if (attendeeLimit > capacityPercentMilestones[i]){
                    notifBadge.setVisibility(View.VISIBLE);
                    message = "Congratulations, " + percentageFull[i] + "  of available spots have been filled.";
                    MileStone milestone = new MileStone(organizerID, eventID, eventName, message,  percentageFull[i]);
                    milestone.addToDatabase();
                    db.collection("Users").document(organizerID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document(percentageFull[i]).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null){
                                if (documentSnapshot.getBoolean("viewable") != null){
                                    Boolean view = documentSnapshot.getBoolean("viewable").booleanValue();
                                    if (view == true){
                                        mileStoneDataList.add(milestone);
                                        milestoneAdapter.notifyDataSetChanged();
                                        notifBadge.setBadgeValue(mileStoneDataList.size());
                                    }
                                }
                            }
                        }
                    });
                }
            }
            if (checkIns == attendeeLimit){
                notifBadge.setVisibility(View.VISIBLE);
                message = "Congratulations, your event has reached max capacity.";
                MileStone milestone = new MileStone(organizerID, eventID, eventName, message, "100%");
                milestone.addToDatabase();
                db.collection("Users").document(organizerID).collection("EventsByOrganizer").document(eventID).collection("Milestones").document("100%").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot != null){
                            if (documentSnapshot.getBoolean("viewable") != null){
                                Boolean view = documentSnapshot.getBoolean("viewable").booleanValue();
                                if (view == true){
                                    mileStoneDataList.add(milestone);
                                    milestoneAdapter.notifyDataSetChanged();
                                    notifBadge.setBadgeValue(mileStoneDataList.size());
                                }
                            }
                        }
                    }
                });
            }
        }



        
        private void initializeCreateEvent(){
            createEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String eventTitle = eventTitleEditText.getText().toString();
                    String eventDate = eventDateEditText.getText().toString();
                    String eventAddress = eventAddressEditText.getText().toString();
                    String eventDetails = eventDetailsEditText.getText().toString();
                    String posterID = organizer.createEventNewQRCode( eventDetails, eventAddress, attendeeLimit, eventTitle, eventDate);

                    posterHandler.uploadImageAndStoreReference(selectedImageUri, posterID, "Event", new Poster.PosterUploadCallback() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {}
                        @Override
                        public void onUploadFailure(Exception e) {
                            Log.e(TAG, "Failed to upload image for event: " + posterID, e);
                            // Handle failure, e.g., show a toast or alert dialog
                        }
                    });
                    previousView(v);
                }
            });
            reuseCheckInQR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextView(v);
                }
            });

            // Switchs layout to previous when user presses back in event details page
            eventDetailsBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    previousView(v);
                }
            });

            // TODO: fix QR reuse method createEventReuseQRCode
            uploadQRFromScan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanCode();

                    previousView(v);
                }
            });

            Button buttonUploadPoster = findViewById(R.id.buttonUploadPoster);
            buttonUploadPoster.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showImageSourceDialog();
                }
            });
        }
}




