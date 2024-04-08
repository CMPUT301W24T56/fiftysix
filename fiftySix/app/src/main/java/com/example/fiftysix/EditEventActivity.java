package com.example.fiftysix;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Activity to allow organizers to edit some event details
 * @author Brady.
 * @version 1
 * @since SDK34
 */

public class EditEventActivity extends AppCompatActivity {

    //Organizer Data
    private FirebaseFirestore db;
    private Organizer organizer;
    private String eventID;
    private int attendeeLimit = Integer.MAX_VALUE;
    private int attendeeSignUpLimit = Integer.MAX_VALUE;

    // Buttons on Create event page
    private Button createEvent;
    private EditText eventTitleEditText;
    private ImageButton eventDetailsBack;
    private EditText eventAddressEditText;
    private EditText eventDetailsEditText;
    private int mYear, mMonth, mDay;

    // Create event Buttons
    private Button eventDateButton, eventStartTimeButton, eventEndDateButton, eventEndTimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        db = FirebaseFirestore.getInstance();

        eventTitleEditText = (EditText) findViewById(R.id.eventNameEditText);
        eventAddressEditText = (EditText) findViewById(R.id.eventAddressEditText);
        eventDetailsEditText = (EditText) findViewById(R.id.eventDetailsEditText);

        organizer = new Organizer(getApplicationContext());
        Bundle bundle = getIntent().getExtras();
        eventID = bundle.getString("eventID");

        createEvent = (Button) findViewById(R.id.buttonCreateEvent);
        eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackCreateEvent);

        eventDateButton = (Button) findViewById(R.id.eventStartDateButton);
        eventStartTimeButton = (Button) findViewById(R.id.eventStartTimeButton);
        eventEndDateButton = (Button) findViewById(R.id.eventEndDateButton);
        eventEndTimeButton = (Button) findViewById(R.id.eventEndTimeButton);

        setValues();
        getInput();

        initializeSaveEvent();

        eventDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Sets event details to the current event details
     */
    private void setValues(){

        db.collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String title = documentSnapshot.getString("eventName");
                    String address = documentSnapshot.getString("location");
                    String details = documentSnapshot.getString("details");
                    String endDate = documentSnapshot.getString("endDate");
                    String endTime = documentSnapshot.getString("endTime");
                    String startDate = documentSnapshot.getString("startDate");
                    String startTime = documentSnapshot.getString("startTime");
                    eventTitleEditText.setText(title);
                    eventAddressEditText.setText(address);
                    eventDetailsEditText.setText(details);
                    eventDateButton.setText("Start Date: " + startDate);
                    eventEndDateButton.setText("End Date: " + endDate);
                    eventStartTimeButton.setText("Start Time: " + startTime);
                    eventEndTimeButton.setText("End Time: " + endTime);
                }
            }
        });
    }


    /**
     * Gets time and date inputs
     */
    private void getInput(){
        eventDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // From https://stackoverflow.com/questions/21408050/best-way-to-select-date-from-a-calendar-and-display-it-in-the-textview
                Calendar mcurrentDate = Calendar.getInstance();
                mYear = mcurrentDate.get(Calendar.YEAR);
                mMonth = mcurrentDate.get(Calendar.MONTH);
                mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(EditEventActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                    }}, mYear, mMonth, mDay);
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
                DatePickerDialog mDatePicker = new DatePickerDialog(EditEventActivity.this, new DatePickerDialog.OnDateSetListener() {
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
                mTimePicker = new TimePickerDialog(EditEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                mTimePicker = new TimePickerDialog(EditEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
     * Ensures all fields have input then updates event data in database with the new details.
     */
    private void initializeSaveEvent() {
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
                if (eventTitle.trim().length() == 0) {
                    Log.d("Null event Title", "onClick: ");
                    Toast.makeText(EditEventActivity.this, "Please enter Event Name!",
                            Toast.LENGTH_LONG).show();

                } else if (eventStartDate.trim().length() == 0) {
                    Toast.makeText(EditEventActivity.this, "Please select Event Start Date!",
                            Toast.LENGTH_LONG).show();
                } else if (eventStartTime.trim().length() == 0) {
                    Toast.makeText(EditEventActivity.this, "Please select Event Start Time!",
                            Toast.LENGTH_LONG).show();
                } else if (eventEndDate.trim().length() == 0) {
                    Toast.makeText(EditEventActivity.this, "Please select Event End Date!",
                            Toast.LENGTH_LONG).show();
                } else if (eventEndTime.trim().length() == 0) {
                    Toast.makeText(EditEventActivity.this, "Please select Event End Time!",
                            Toast.LENGTH_LONG).show();
                } else if (eventAddress.trim().length() == 0) {
                    Toast.makeText(EditEventActivity.this, "Please select Event Address!",
                            Toast.LENGTH_LONG).show();
                } else if (eventDetails.trim().length() == 0) {
                    Toast.makeText(EditEventActivity.this, "Please select Event Details!",
                            Toast.LENGTH_LONG).show();
                }

                // Can create event, no empty fields
                else {

                    // Removes "Start time:"
                    eventStartDate = eventStartDate.split(" ")[2];
                    eventEndDate = eventEndDate.split(" ")[2];
                    eventStartTime = eventStartTime.split(" ")[2];
                    eventEndTime = eventEndTime.split(" ")[2];

                    FirebaseFirestore.getInstance().collection("Events").document(eventID).update("details", eventDetails);
                    FirebaseFirestore.getInstance().collection("Events").document(eventID).update("startDate", eventStartDate);
                    FirebaseFirestore.getInstance().collection("Events").document(eventID).update("endDate", eventEndDate);
                    FirebaseFirestore.getInstance().collection("Events").document(eventID).update("startTime", eventStartTime);
                    FirebaseFirestore.getInstance().collection("Events").document(eventID).update("endTime", eventEndTime);
                    FirebaseFirestore.getInstance().collection("Events").document(eventID).update("location", eventAddress);
                    FirebaseFirestore.getInstance().collection("Events").document(eventID).update("eventName", eventTitle);

                    finish();
                }
            }
        });
    }

}





