package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminBrowseEvents extends AppCompatActivity {

    private EventAdapter adapter = new EventAdapter(this, eventList);
    private ArrayList<Event> eventList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        ListView ls = findViewById(R.id.abe_list);
        ls.setAdapter(adapter);

        fetchEvents();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchEvents() {
        db.collection("Events")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        return;
                    }

                    Log.d(TAG, "Number of events fetched: " + task.getResult().size());
                    eventList.clear();

                    for (QueryDocumentSnapshot d : task.getResult())
                        eventList.add(new Event(
                                d.getId(),
                                d.getString("eventName"),
                                d.getString("location"),
                                d.getString("date"),
                                d.getString("details"),
                                100,
                                1000,
                                1000,
                                d.getString("posterURL")
                        ));
                    adapter.notifyDataSetChanged();
                });
    }
}
