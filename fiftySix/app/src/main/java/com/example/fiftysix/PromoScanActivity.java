package com.example.fiftysix;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class PromoScanActivity extends AppCompatActivity {

    private TextView eventTitleTextView, eventLocationTextView, eventDateTextView, eventDetailsTextView;
    private ImageView eventPosterImageView;
    private String eventID, eventName, eventDescription, eventDate, eventTime, eventLocation, posterID, imageUrl;
    private Button buttonSignUpEvent;
    private ImageButton buttonBack;
    private Attendee attendee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_scan);

        eventTitleTextView = (TextView) findViewById(R.id.eventTitleTextView);
        eventLocationTextView = (TextView) findViewById(R.id.eventLocationTextView);
        eventDateTextView = (TextView) findViewById(R.id.eventDateTextView);
        eventDetailsTextView = (TextView) findViewById(R.id.eventDetailsTextView);
        eventPosterImageView = (ImageView) findViewById(R.id.eventPosterImageView);
        buttonSignUpEvent = (Button) findViewById(R.id.buttonSignUpEvent);
        buttonBack = (ImageButton) findViewById(R.id.buttonBack);


        Bundle bundle = getIntent().getExtras();
        eventID = bundle.getString("eventID");
        attendee = new Attendee(getApplicationContext());


        FirebaseFirestore.getInstance().collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null){

                    eventName = documentSnapshot.getString("eventName");
                    eventLocation = documentSnapshot.getString("location");
                    eventTime = documentSnapshot.getString("startTime");
                    eventDate = documentSnapshot.getString("startDate");
                    eventDescription = documentSnapshot.getString("details");
                    posterID = documentSnapshot.getString("posterID");

                    FirebaseFirestore.getInstance().collection("PosterImages").document(posterID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {



                            eventTitleTextView.setText(eventName);
                            eventLocationTextView.setText(eventLocation);
                            eventDateTextView.setText(eventTime + ", " + eventDate);
                            eventDetailsTextView.setText(eventDescription);

                            imageUrl = documentSnapshot.getString("image");

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get()
                                        .load(imageUrl)
                                        .fit()
                                        .into(eventPosterImageView); // Your ImageView
                            }
                        }
                    });
                }
            }
        });

        buttonSignUpEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                attendee.alreadySignedUp(eventID, new Attendee.AttendeeCallBack() {
                    @Override
                    public void checkInSuccess(Boolean checkinSuccess, String eventName) {

                        // Not already Signed Up
                        if (!checkinSuccess){

                            attendee.hasSignUpSpace(eventID, new Attendee.AttendeeCallBack() {
                                @Override
                                public void checkInSuccess(Boolean checkinSuccess, String eventName) {

                                    if (!checkinSuccess){
                                        attendee.signUpForEvent(eventID);
                                        finish();
                                    }
                                    else{
                                        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(PromoScanActivity.this);
                                        builder.setTitle("Sign-up Failed");
                                        builder.setMessage("event is at max sign-up capacity.");
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
                            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(PromoScanActivity.this);
                            builder.setTitle("Sign-up Failed");
                            builder.setMessage("User already signed-up for this event.");
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
        });

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}