package com.example.fiftysix;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;
/**
 * Displays event signups on map using Google maps.
 *
 * @author Arsh, Brady.
 * @version 1
 * @since SDK34
 */
public class location extends AppCompatActivity {

    SupportMapFragment supportMapFragment;
    private ImageButton back;
    private String eventID;
    private List<LatLng> attendeeLocations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendees_location);

        Bundle bundle = getIntent().getExtras();
        eventID = bundle.getString("eventID");

        Log.d("location", "inflate success");

        // Initialize the map fragment
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.location_attendees);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FirebaseFirestore.getInstance().collection("Events").document(eventID).collection("attendeeSignUps").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null){
                    for (QueryDocumentSnapshot attendeeDoc : value){
                        String attendeeID = attendeeDoc.getId();
                        if (attendeeDoc.getString("latitude") != null){

                            String attendeeLat = attendeeDoc.getString("latitude").toString();
                            String attendeeLong = attendeeDoc.getString("longitude").toString();
                            if (attendeeLat != null && attendeeLong != null){
                                Float attendeeLatFloat = Float.parseFloat(attendeeLat);
                                Float attendeeLongFloat = Float.parseFloat(attendeeLong);

                                Log.d("location map", "Lat = " + attendeeLatFloat + " Float =" + attendeeLongFloat);
                                attendeeLocations.add(new LatLng( attendeeLatFloat, attendeeLongFloat));
                            }
                        }
                    }

                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(@NonNull GoogleMap googleMap) {
                            // Add markers for each attendee's geolocation
                            for (LatLng attendeeLocation : attendeeLocations) {
                                googleMap.addMarker(new MarkerOptions().position(attendeeLocation).title("Attendee"));
                            }
                        }
                    });
                    back = findViewById(R.id.back);
                    back.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
            }
        });
    }
}