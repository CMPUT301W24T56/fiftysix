package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import ru.nikartm.support.ImageBadgeView;

public class EditEventActivity extends AppCompatActivity {


    //Organizer Data
    private FirebaseFirestore db;
    private Organizer organizer;
    private String eventID;
    private String eventTitle,  eventStartDate, eventEndDate, eventStartTime, eventEndTime, eventAddress, eventDetails;



    private int attendeeLimit = Integer.MAX_VALUE;
    private int attendeeSignUpLimit = Integer.MAX_VALUE;

    // Buttons on Create event page
    private Button createEvent;
    private EditText eventTitleEditText;
    private ImageButton eventDetailsBack;


    private EditText eventAddressEditText;
    private EditText eventDetailsEditText;
    private Switch switchAttendeeLimit, switchSignUpLimit;
    private int mYear, mMonth, mDay;


    // Create event Buttons
    private Button eventDateButton, eventStartTimeButton, eventEndDateButton, eventEndTimeButton;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        db = FirebaseFirestore.getInstance();


        switchAttendeeLimit = findViewById(R.id.switchAttendeeLimit);
        switchSignUpLimit = findViewById(R.id.switchSignUpLimit);

        eventTitleEditText = (EditText) findViewById(R.id.eventNameEditText);
        eventAddressEditText = (EditText) findViewById(R.id.eventAddressEditText);
        eventDetailsEditText = (EditText) findViewById(R.id.eventDetailsEditText);

        organizer = new Organizer(getApplicationContext());
        Bundle bundle = getIntent().getExtras();
        eventID = bundle.getString("eventID");


        // Create event page buttons

        createEvent = (Button) findViewById(R.id.buttonCreateEvent);
        eventDetailsBack = (ImageButton) findViewById(R.id.buttonBackCreateEvent);

        switchAttendeeLimit = findViewById(R.id.switchAttendeeLimit);
        switchSignUpLimit = findViewById(R.id.switchSignUpLimit);


        eventDateButton = (Button) findViewById(R.id.eventStartDateButton);
        eventStartTimeButton = (Button) findViewById(R.id.eventStartTimeButton);
        eventEndDateButton = (Button) findViewById(R.id.eventEndDateButton);
        eventEndTimeButton = (Button) findViewById(R.id.eventEndTimeButton);



        setValues();
        getInput();
        //setupAttendeeLimitSwitch();
        //setupSignUpLimitSwitch();
        initializeSaveEvent();

        eventDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });





    }

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
                        eventStartTimeButton.setText( "Start Time: " + selectedHour + ":" + selectedMinute);
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
                        eventEndTimeButton.setText("End Time: " + selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select End Time");
                mTimePicker.show();
            }
        });
    }

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


                    // resets text displayed



                    //finish();
                }
            }
        });
    }

    // Wanted to add these but ran out of time.
    /*

    private void setupAttendeeLimitSwitch(){
        switchAttendeeLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditEventActivity.this);
                    builder.setTitle("Set Attendee Check-in Limit");

                    // Set up the input
                    final EditText input = new EditText(EditEventActivity.this);
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

    private void setupSignUpLimitSwitch(){
        switchSignUpLimit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                if (isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditEventActivity.this);
                    builder.setTitle("Set Attendee Sign-up Limit");

                    // Set up the input
                    final EditText input = new EditText(EditEventActivity.this);
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

     */
}





