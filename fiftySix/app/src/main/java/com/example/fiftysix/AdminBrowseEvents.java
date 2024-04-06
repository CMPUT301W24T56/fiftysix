package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

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
        db.collection("Events").document(eventList.remove(i).getEventID()).delete();
        adapter.notifyDataSetChanged();
    }

    private Event doc2event(DocumentSnapshot d) {
        return new Event(
                d.getId(),
                d.getString("eventName"),
                d.getString("location"),
                d.getString("date"),
                d.getString("details"),
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
