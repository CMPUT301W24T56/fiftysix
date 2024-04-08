package com.example.fiftysix;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class  AdminBrowseProfiles extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminProfileAdapter adapter;
    private FirebaseFirestore db;

    private void deleteProfile(Profile profile) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("bio", "unknown");
        updates.put("email", "unknown");
        updates.put("phone", "unknown");
        updates.put("name", "unknown");
        updates.put("profileImageURL", "unknown");
        updates.put("userID", "unknown");
        db.collection("Profiles").document(profile.getProfileID())
                .update(updates)
                .addOnSuccessListener(aVoid -> Log.d("UpdateProfile", "Profile successfully updated!"))
                .addOnFailureListener(e -> Log.w("UpdateProfile", "Error updating profile", e));

        db.collection("Users").document(profile.getProfileID())
                .update(updates);

        fetchProfiles();
        adapter.notifyDataSetChanged();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_browse_profiles);

        recyclerView = findViewById(R.id.profilesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminProfileAdapter(new ArrayList<>(),new AdminProfileAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Profile profile) {
                ConfirmDeleteDialogFragment dialogFragment = ConfirmDeleteDialogFragment.newInstance(profile);
                dialogFragment.setConfirmDeleteListener(profileToDelete -> {
                    // Call method to delete the profile from Firestore
                    deleteProfile(profileToDelete);
                });
                dialogFragment.show(getSupportFragmentManager(), "confirmDelete");
            }

        });
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fetchProfiles();

    }

    private void fetchProfiles() {
        db.collection("Profiles")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Profile> profiles = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {


                            Profile profile = new Profile(
                                    document.getId(),
                                    document.getString("name"),
                                    document.getString("email"),
                                    document.getString("phone")
                            );
                            profiles.add(profile);
                        }
                        adapter.updateProfiles(profiles);
                        Log.d("AdminBrowseProfiles", "Number of profiles fetched: " + profiles.size());
                    } else {
                        Log.e("AdminBrowseProfiles", "Error getting profiles: ", task.getException());
                    }
                });
    }

}
