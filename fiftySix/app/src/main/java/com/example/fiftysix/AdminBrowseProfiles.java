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
import java.util.List;

public class  AdminBrowseProfiles extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminProfileAdapter adapter;
    private FirebaseFirestore db;

    private void deleteProfile(Profile profile) {
        db.collection("Profiles").document(profile.getProfileID())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("DeleteProfile", "Profile successfully deleted!"))
                .addOnFailureListener(e -> Log.w("DeleteProfile", "Error deleting profile", e));
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
                                    document.getString("userID"),
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
