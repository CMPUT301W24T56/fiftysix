package com.example.fiftysix;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import static android.content.ContentValues.TAG;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TimePicker;
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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import ru.nikartm.support.ImageBadgeView;
/**
 * Creates an Organizer. Completes Organizer tasks such as
 * 1. Create/ Edit event, Upload poster, Save QR Code.
 * 2. View event data such as attendance.
 * 3. Send notification to attendee.
 * 4. Create Promo/Checkin QR code.
 *
 * @author Rakshit, Brady, Arsh.
 * @version 1
 * @since SDK34
 */
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
    private int attendeeSignUpLimit = Integer.MAX_VALUE;
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
    private ImageButton buttonOrgHomeBack;

    private ImageButton backOrgNotif;
    private ImageBadgeView notifBadge;

    // Buttons on Create event page
    private Button createEvent;
    private Button generatePromoQR;
    private ImageButton eventDetailsBack;
    private Button reuseCheckInQR;
    private EditText eventTitleEditText;
    private EditText eventAddressEditText;
    private EditText eventDetailsEditText;
    private Switch switchAttendeeLimit, switchSignUpLimit;
    private int mYear, mMonth, mDay;
    private PromoQRCode promoQRCode;
    private String reuseQRID;
    String scanQRID;

    // Create event Buttons
    private Button eventDateButton, eventStartTimeButton, eventEndDateButton, eventEndTimeButton;

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

        promoQRCode = null;
        reuseQRID = null;

        // Firebase
        db = FirebaseFirestore.getInstance();
        orgEventRef = db.collection("Users").document(organizer.getOrganizerID()).collection("EventsByOrganizer");
        eventRef = db.collection("Events");
        imageRef = db.collection("Images");

        setButtons();
        setEditText();
        setupAttendeeLimitSwitch();
        setupSignUpLimitSwitch();

        eventDataList = new ArrayList<>();

        // Sets home page recycler view event data
        organizerEventAdapter = new OrganizerEventAdapter(eventDataList, this);
        recyclerView.setAdapter(organizerEventAdapter);
        recyclerView.setHasFixedSize(false);

        // Creates Poster Object
        posterHandler = new Poster();

        // sets-up milestones
        setMilestones();

        // Adds events from database to the organizers home screen. Will only show events created by the organizer
        loadOrganizerEvents();

        // sets gallery and camera launchers
        setLaunches();

        // Sets up date and time input for create event page
        initializeDateTime();

        // Sets up create event page
        initializeCreateEvent();


        // Opens organizer milestones page
        orgNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationView(v);
            }

        });

        // Opens organizer home page
        backOrgNotif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeView(v);
            }
        });



        // Sends user to user selection screen
        buttonOrgHomeBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });


        // Opens page to create event
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedImageUri = null;
                nextView(v);
            }
        });
    }



    //________________________________________Methods________________________________________

    /**
     * Sets up gallery and camera launchers
     */
    private void setLaunches(){
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
     * Setup milestones for signups
     */
    private void setMilestones(){
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
    }


    /**
     * Sets up date and time selection for event creation
     */
    private void initializeDateTime(){
        eventDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // From https://stackoverflow.com/questions/21408050/best-way-to-select-date-from-a-calendar-and-display-it-in-the-textview
                Calendar mcurrentDate = Calendar.getInstance();
                mYear = mcurrentDate.get(Calendar.YEAR);
                mMonth = mcurrentDate.get(Calendar.MONTH);
                mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(OrganizerMainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, selectedyear);
                        myCalendar.set(Calendar.MONTH, selectedmonth);
                        myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                        String myFormat = "dd/MM/yy"; //Change as you need
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
                        eventDateButton.setText("Start Date: " +sdf.format(myCalendar.getTime()));

                        mDay = selectedday;
                        mMonth = selectedmonth;
                        mYear = selectedyear;
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Event Start Date");
                mDatePicker.show();
            }
        });

        eventEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // From https://stackoverflow.com/questions/21408050/best-way-to-select-date-from-a-calendar-and-display-it-in-the-textview
                Calendar mcurrentDate = Calendar.getInstance();
                mYear = mcurrentDate.get(Calendar.YEAR);
                mMonth = mcurrentDate.get(Calendar.MONTH);
                mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(OrganizerMainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        Calendar myCalendar = Calendar.getInstance();
                        myCalendar.set(Calendar.YEAR, selectedyear);
                        myCalendar.set(Calendar.MONTH, selectedmonth);
                        myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                        String myFormat = "dd/MM/yy"; //Change as you need
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
                        eventEndDateButton.setText("End Date: " + sdf.format(myCalendar.getTime()));

                        mDay = selectedday;
                        mMonth = selectedmonth;
                        mYear = selectedyear;
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select Event End Date");
                mDatePicker.show();
            }
        });

        eventStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(OrganizerMainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        String min = "00";
                        if (selectedMinute == 0){
                        }
                        else if(selectedMinute < 10){
                            min = "0" + String.valueOf(selectedMinute);
                        }
                        else{
                            min = String.valueOf(selectedMinute);
                        }

                        eventStartTimeButton.setText( "Start Time: " + selectedHour + ":" + min);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Start Time");
                mTimePicker.show();

            }
        });

        eventEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // https://stackoverflow.com/questions/17901946/timepicker-dialog-from-clicking-edittext

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(OrganizerMainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                        String min = "00";
                        if (selectedMinute == 0){
                        }
                        else if(selectedMinute < 10){
                            min = "0" + String.valueOf(selectedMinute);
                        }
                        else{
                            min = String.valueOf(selectedMinute);
                        }

                        eventEndTimeButton.setText("End Time: " + selectedHour + ":" + min);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select End Time");
                mTimePicker.show();
            }
        });

    }


    /**
     * Shows dialog to select either camera role or gallery as a method to upload images.  Used for adding event poster.
     * If user selects camera it checks the permissions, either calls open camera or requestCameraPermission().
     * If user selects galley it opens gallery using galleryLauncher.
     */
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


    /**
     * Requests camera permission, called by showImageSourceDialog() if device has not given permission
     */
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(OrganizerMainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    /**
     * Checks if the user has just given camera permission, If so then calls openCamera().
     * @param requestCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
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
    }

    /**
     * Opens camera and saves the Uri image in the class attribute photoURI.
     * Called by the methods onRequestPermissionsResult() and showImageSourceDialog().
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
            Uri photoURI = FileProvider.getUriForFile(OrganizerMainActivity.this, "com.example.fiftysix.provider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            cameraLauncher.launch(photoURI);
        } else {
            Log.d(TAG, "No app can handle the camera intent.");
        }
    }


    /**
     * Creates a file/location for images captured by the camera to be stored temporarily.
     * @return File image
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
     * Sets view flipper to display Organizer home layout.
      * @param v View
     */
    private void homeView(View v){
        viewFlipper.setDisplayedChild(0);;
    }

    /**
     * Sets view flipper to display the create event layout.
     * @param v View
     */
    private void createEventView(View v){
        viewFlipper.setDisplayedChild(1);;
    }

    /**
     * Sets view flipper to display the reuse QR layout.
     * @param v View
     */
    private void reuseQRView(View v){
        viewFlipper.setDisplayedChild(2);;
    }

    /**
     * Sets view flipper to display the milestones/notifications layout.
     * @param v View
     */
    private void notificationView(View v){
        viewFlipper.setDisplayedChild(3);;
    }
    /**
     * Sets view flipper to display the previous layout.
     * @param v View
     */
    private void previousView(View v){
        //viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
        //viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
        viewFlipper.showPrevious();
        }
    /**
     * Sets view flipper to display the next layout.
     * @param v View
     */
    private void nextView(View v){
        //viewFlipper.setInAnimation(this, R.anim.slide_in_right);
        //viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
        viewFlipper.showNext();
        }

    /**
     * initializes all button attributes, button = (Button) findViewById...
     */
    private void setButtons() {
            // Home page buttons
            addEventButton = (ImageButton) findViewById(R.id.buttonAddEvent);
            orgProfileButton = (ImageButton) findViewById(R.id.buttonOrganizerProfile);
            orgNotificationButton = (ImageButton) findViewById(R.id.notification_button);
            buttonOrgHomeBack = (ImageButton) findViewById(R.id.buttonOrgHomeBack);
            // Create event page buttons
            reuseCheckInQR = (Button) findViewById(R.id.reuseCheckinQR);
            createEvent = (Button) findViewById(R.id.buttonCreateEvent);
            eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackCreateEvent);
            ImageButton eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackUploadQR);
            uploadQRFromScan = (Button) findViewById(R.id.EditEvent);
            switchAttendeeLimit = findViewById(R.id.switchAttendeeLimit);
            switchSignUpLimit = findViewById(R.id.switchSignUpLimit);
            //eventPosterImage = findViewById(R.id.event_poster_image);
            generatePromoQR = findViewById(R.id.generatePromoQR);

            backOrgNotif = (ImageButton) findViewById(R.id.backOrgNotif);

            eventDateButton = (Button) findViewById(R.id.eventStartDateButton);
            eventStartTimeButton = (Button) findViewById(R.id.eventStartTimeButton);
            eventEndDateButton = (Button) findViewById(R.id.eventEndDateButton);
            eventEndTimeButton = (Button) findViewById(R.id.eventEndTimeButton);

        }

    /**
     * initializes all editText attributes, editText = (EditText) findViewById...
     */
    private void setEditText() {
            eventTitleEditText = (EditText) findViewById(R.id.eventNameEditText);
            eventAddressEditText = (EditText) findViewById(R.id.eventAddressEditText);
            eventDetailsEditText = (EditText) findViewById(R.id.eventDetailsEditText);
        }

    /**
     * Sets up the checkin limit switch for when the organizer wants to limit checkins to the event.
     * Builds alert dialog when switch is flipped on and gets input number.
     * If switch is not flipped checkin limit = max int value.
     */
    private void setupAttendeeLimitSwitch() {
            switchAttendeeLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    if (isChecked) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrganizerMainActivity.this);
                        builder.setTitle("Set Attendee Check-in Limit");

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

    /**
     * Sets up the signup limit switch for when the organizer wants to limit signups to the event.
     * Builds alert dialog when switch is flipped on and gets input number.
     * If switch is not flipped checkin limit = max int value.
     */
    private void setupSignUpLimitSwitch() {
        switchSignUpLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(OrganizerMainActivity.this);
                    builder.setTitle("Set Attendee Sign-up Limit");

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
                                    attendeeSignUpLimit = limit;
                                } else {
                                    // Invalid input, revert the switch to unchecked
                                    switchSignUpLimit.setChecked(false);
                                    attendeeSignUpLimit = Integer.MAX_VALUE;
                                }
                            } catch (NumberFormatException e) {
                                switchSignUpLimit.setChecked(false);
                                attendeeSignUpLimit = Integer.MAX_VALUE;
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            // Revert the switch to unchecked since canceled
                            switchSignUpLimit.setChecked(false);
                        }
                    });

                    builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            // Handle the case where the user cancels the dialog (e.g., by pressing the back button)
                            switchSignUpLimit.setChecked(false);
                        }
                    });

                    builder.show();
                } else {
                    attendeeSignUpLimit = Integer.MAX_VALUE;
                }
            }
        });
    }


    /**
     *  Reference - "youtube - Implement Barcode QR Scanner in Android studio barcode reader | Cambo Tutorial" - youtube channel = Cambo Tutorial
     *  Scans QR code, gets the string. This string returned as a unique QRCode id. It then checks if the QR code is elidible for reuse as a checckin QR Code.
     *  Criteria for eligibility is:
     *  1. Belongs to an INACTIVE event.
     *  2. Was a CHECK-IN QR code on this app.
     */
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
            scanQRID = result.getContents().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
            Date strDate = null;
            new CheckInQRCode().checkValidReuseQR(scanQRID, new CheckInQRCode.CheckInQRCodeCallback() {
                @Override
                public void onSuccess(Boolean validQR) {
                    if (validQR){
                        reuseQRID = scanQRID;
                        new AlertDialog.Builder(OrganizerMainActivity.this)
                                .setTitle("QR Code Set")
                                .setMessage("Your QR code was successfully set.")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }else{
                        new AlertDialog.Builder(OrganizerMainActivity.this)
                                .setTitle("QR Code Not Eligible")
                                .setMessage("A QR code is not eligible if:\n1. Belongs to an active or upcoming event.\n2. Was never used as a CHECK-IN QR code on this app, ALL Promo QR Codes are ineligible.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();
                    }
                }
                @Override
                public void onFailure(Exception e) {
                }
            });
        }


    });

    /**
     * Loads organizers events from firebase
     */
    private void loadOrganizerEvents(){
            eventRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (error != null) {
                        Log.e("Firestore", error.toString());
                        return;
                    }
                    if (value.isEmpty()){
                        if (eventDataList.isEmpty()){
                            String hardcode = "No Events to Display";
                            eventDataList.add(new Event(hardcode, "Please make your first event!", hardcode, hardcode, hardcode, hardcode, hardcode, hardcode, 0, 0, 0, 0,hardcode));
                        }
                    }
                    else{
                        eventRef.whereEqualTo("organizer", organizerID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (querySnapshot != null) {
                                    eventDataList.clear();
                                    mileStoneDataList.clear();
                                    for (QueryDocumentSnapshot doc : querySnapshot) {
                                        String eventID = doc.getId();
                                        String eventName = doc.getString("eventName");
                                        String posterID = doc.getString("posterID");
                                        Integer attendeeCheckinLimitIn = doc.getLong("attendeeLimit").intValue();
                                        Integer attendeeSignUpLimitIn = doc.getLong("attendeeSignUpLimit").intValue();
                                        Integer inAttendeeCount = doc.getLong("attendeeCount").intValue();
                                        Integer signUpCount = doc.getLong("attendeeSignUpCount").intValue();

                                        String startDate = doc.getString("startDate");
                                        String startTime = doc.getString("startTime");
                                        String endDate = doc.getString("endDate");
                                        String endTime = doc.getString("endTime");

                                        String location = doc.getString("location");
                                        String details = doc.getString("details");
                                        addAtttendanceMilestoneUpdates(eventID, eventName, signUpCount, attendeeSignUpLimitIn);
                                        db.collection("PosterImages").whereEqualTo("poster", posterID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot querySnapshotImage) {
                                                for (QueryDocumentSnapshot doc : querySnapshotImage){
                                                    Log.e("Added to list", "onSuccess: Event has been added to eventDataList, OrganizerMain");
                                                    String posterURL = doc.getString("image");
                                                    eventDataList.add(new Event(eventID, eventName, location, startDate, endDate, startTime, endTime, details, inAttendeeCount, signUpCount, attendeeCheckinLimitIn, attendeeSignUpLimitIn, posterURL));
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

    /**
     * Checks signups ans signup limits to see if they meet criteria for a milestone, if so a Milestone is created, added to database and presented to the organizer.
     *
     * @param eventID String of event ID to check milestones
     * @param eventName String of event name to check milestones
     * @param checkIns Integer of number of event signups
     * @param attendeeLimit  Integer of number of event signup limit
     */
    private void addAtttendanceMilestoneUpdates(String eventID, String eventName, Integer checkIns, Integer attendeeLimit){
            notifBadge.setVisibility(View.VISIBLE);
            String message;

            // Makes milestones based on number of signups
            if (checkIns == 1){
                message = "Congratulations, your first attendee has signed-up.";
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
                        message = "Congratulations, " + checkIns.toString() + "  attendees have signed-up.";
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

            if (checkIns > 0){
                // Makes milestones based on percent of capacity filled by signups
                double [] capacityPercentMilestones = { (attendeeLimit*0.25), (attendeeLimit*0.5), (attendeeLimit*0.75)};
                String [] percentageFull = {"25%", "50%", "75%"};
                for (int i = 0; i < capacityPercentMilestones.length; i++ ){
                    if (checkIns > capacityPercentMilestones[i]){
                        notifBadge.setVisibility(View.VISIBLE);
                        message = "Congratulations, " + percentageFull[i] + "  of available sign-ups have been filled.";
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
            }

            if (checkIns == attendeeLimit){
                notifBadge.setVisibility(View.VISIBLE);
                message = "Congratulations, your event has reached max sign-up capacity.";
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

    /**
     * Ensure data input and validation for create event, sets up buttons, creates new event. Adds Reuse QR, Upload Poster,
      */
    private void initializeCreateEvent(){
            createEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    String eventTitle = eventTitleEditText.getText().toString();
                    String eventStartDate = eventDateButton.getText().toString();
                    String eventEndDate = eventEndDateButton.getText().toString();
                    String eventStartTime = eventStartTimeButton.getText().toString();
                    String eventEndTime = eventEndTimeButton.getText().toString();
                    String eventAddress = eventAddressEditText.getText().toString();
                    String eventDetails = eventDetailsEditText.getText().toString();

                    // Require Event Input
                    if (eventTitle.trim().length() == 0){
                        Log.d("Null event Title", "onClick: ");
                        Toast.makeText(OrganizerMainActivity.this, "Please enter Event Name!",
                                Toast.LENGTH_LONG).show();

                    }
                    else if (eventStartDate.trim().length() == 0){
                        Toast.makeText(OrganizerMainActivity.this, "Please select Event Start Date!",
                                Toast.LENGTH_LONG).show();
                    }
                    else if (eventStartTime.trim().length() == 0){
                        Toast.makeText(OrganizerMainActivity.this, "Please select Event Start Time!",
                                Toast.LENGTH_LONG).show();
                    }
                    else if (eventEndDate.trim().length() == 0){
                        Toast.makeText(OrganizerMainActivity.this, "Please select Event End Date!",
                                Toast.LENGTH_LONG).show();
                    }
                    else if (eventEndTime.trim().length() == 0){
                        Toast.makeText(OrganizerMainActivity.this, "Please select Event End Time!",
                                Toast.LENGTH_LONG).show();
                    }
                    else if (eventAddress.trim().length() == 0){
                        Toast.makeText(OrganizerMainActivity.this, "Please select Event Address!",
                                Toast.LENGTH_LONG).show();
                    }
                    else if (eventDetails.trim().length() == 0){
                        Toast.makeText(OrganizerMainActivity.this, "Please select Event Details!",
                                Toast.LENGTH_LONG).show();
                    }

                    // Can create event, no empty fields
                    else{

                        // Removes "Start time:"
                        eventStartDate = eventStartDate.split(" ")[2];
                        eventEndDate = eventEndDate.split(" ")[2];
                        eventStartTime = eventStartTime.split(" ")[2];
                        eventEndTime = eventEndTime.split(" ")[2];


                        // Creates Event
                        String posterID = organizer.createEventNewQRCode( eventDetails, eventAddress, attendeeLimit, attendeeSignUpLimit, eventTitle, eventStartDate, eventEndDate, eventStartTime, eventEndTime, promoQRCode, reuseQRID);
                        // Adds poster image
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

                        // resets text displayed
                        eventTitleEditText.setText(null);
                        eventDateButton.setText(null);
                        eventEndDateButton.setText(null);
                        eventStartTimeButton.setText(null);
                        eventEndTimeButton.setText(null);
                        eventAddressEditText.setText(null);
                        eventDetailsEditText.setText(null);
                        promoQRCode = null;
                        reuseQRID = null;
                    }
                }
            });


            reuseCheckInQR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    scanCode();
                }
            });

            // Switchs layout to previous when user presses back in event details page
            eventDetailsBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    previousView(v);
                    promoQRCode = null;
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

            generatePromoQR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO:
                    promoQRCode = new PromoQRCode(getApplicationContext());
                }
            });
        }


}




