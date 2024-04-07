package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import static java.lang.System.in;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.*;

import java.util.ArrayList;

public class AdminBrowseEvents extends AppCompatActivity {

    private EventAdapter adapter;
    private ArrayList<Event> eventList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        ListView ls = findViewById(R.id.abe_list);
        adapter = new EventAdapter(this, eventList);
        ls.setAdapter(adapter);
        // TODO: possibly enable view evt info
        ls.setOnItemClickListener((par, v, i, id) -> rm(i));
        fetchEvents();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void rm(int i) {
        // TODO: mk confirm dialog
        String eventName = eventList.get(i).getEventName();
        String eventID = eventList.get(i).getEventID();


        new AlertDialog.Builder(this)
                .setTitle("Delete " + '"' + eventName + '"')
                .setMessage("Are you sure you want to delete " + '"' + eventName + '"' + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        eventList.remove(i);

                        db.collection("Events").document(eventID).collection("Milestones").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (!querySnapshot.isEmpty()){
                                    for (DocumentSnapshot doc : querySnapshot){
                                        db.collection("Events").document(eventID).collection("Milestones").document(doc.getId()).delete();
                                    }
                                }
                            }
                        });

                        // Deletes Notifications collection
                        db.collection("Events").document(eventID).collection("Notifications").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (!querySnapshot.isEmpty()){
                                    for (DocumentSnapshot doc : querySnapshot){
                                        db.collection("Events").document(eventID).collection("Notifications").document(doc.getId()).delete();
                                    }
                                }
                            }
                        });


                        // Deletes Signup collection & removes event from attendees signup collection
                        db.collection("Events").document(eventID).collection("attendeeSignUps").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (!querySnapshot.isEmpty()){
                                    for (DocumentSnapshot doc : querySnapshot){
                                        String attendeeID = doc.getId();
                                        db.collection("Users").document(attendeeID).collection("SignedUpEvents").document(eventID).delete();
                                        db.collection("Events").document(eventID).collection("attendeeSignUps").document(attendeeID).delete();
                                    }
                                }
                            }
                        });


                        // Deletes CheckIn collection & removes event from attendees CheckIn collection
                        db.collection("Events").document(eventID).collection("attendeesAtEvent").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot querySnapshot) {
                                if (!querySnapshot.isEmpty()){
                                    for (DocumentSnapshot doc : querySnapshot){
                                        String attendeeID = doc.getId();
                                        db.collection("Users").document(attendeeID).collection("UpcomingEvents").document(eventID).delete();
                                        db.collection("Events").document(eventID).collection("attendeesAtEvent").document(attendeeID).delete();
                                    }
                                }
                            }
                        });

                        // Deletes Poster
                        db.collection("Events").document(eventID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()){
                                    String posterID = documentSnapshot.getString("posterID");
                                    db.collection("PosterImages").document(posterID).delete();
                                }
                            }
                        });


                        // Deletes event
                        db.collection("Events").document(eventID).delete();
                        adapter.notifyDataSetChanged();

                    }
                })

                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


        adapter.notifyDataSetChanged();
    }

    private Event doc2event(DocumentSnapshot d) {
        return new Event(
                d.getId(),
                d.getString("eventName"),
                d.getString("location"),
                d.getString("startDate"),
                d.getString("endDate"),
                d.getString("startTime"),
                d.getString("endTime"),
                d.getString("details"),
                100,
                100,
                1000,
                1000,
                d.getString("posterURL"));
    }

    private void fetchEvents() {
        db.collection("Events")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        return;
                    }
                    QuerySnapshot res = task.getResult();
                    Log.d(TAG, "Number of events fetched: " + res.size());
                    eventList.clear();
                    for (DocumentSnapshot d : res)
                        eventList.add(doc2event(d));
                    adapter.notifyDataSetChanged();
                });

    }
}
