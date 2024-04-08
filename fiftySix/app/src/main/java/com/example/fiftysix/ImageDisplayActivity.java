package com.example.fiftysix;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ImageDisplayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<String> imageUrls = new ArrayList<>();
    private List<AdminImage> posterImages = new ArrayList<AdminImage>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);
        recyclerView = findViewById(R.id.imagesRecyclerView);
        ImageButton backButton = findViewById(R.id.backButton);



        // Get the image type passed from the AdminBrowseImages activity
        String imageType = getIntent().getStringExtra("ImageType");
        if (imageType.equals("PosterImages")) {
            createPosterImages();
        }
        else{
            createProfileImages();
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Change the number of columns if needed
        imageAdapter = new ImageAdapter(posterImages, ImageDisplayActivity.this);
        recyclerView.setAdapter(imageAdapter);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void createPosterImages(){

        String defaultURL = "https://firebasestorage.googleapis.com/v0/b/fiftysix-a4bcf.appspot.com/o/images%2FDoNotDeleteStockProfilePic%2Fno-photos.png?alt=media&token=52497ae1-5e13-49cb-a43b-379f85849c73";
        db.collection("PosterImages").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                posterImages.clear();

                if (!value.isEmpty()){
                    for (DocumentSnapshot d : value){

                        if (d.getString("image") != null){
                            String posterID = d.getId();
                            String imageURL = d.getString("image");

                            if (!imageURL.equals(defaultURL)){
                                posterImages.add(new AdminImage(posterID, imageURL, defaultURL));
                                //Log.d("ITEMADDEDD", "onEvent: ");
                            }
                        }
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }
        });

    }


    public void createProfileImages(){

        db.collection("Users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                posterImages.clear();

                if (!value.isEmpty()){
                    for (DocumentSnapshot d : value){
                        String name = d.getString("name");
                        if (name != null){
                            String imageURL = d.getString("profileImageURL");
                            String profileID = d.getId();

                            String defaultURL1 = "https://ui-avatars.com/api/?rounded=true&name="+ name +"&background=random&size=512";
                            String defaultURL2 = "https://ui-avatars.com/api/?rounded=true&name=NA&background=random&size=512";

                            if (!imageURL.equals(defaultURL1) && !imageURL.equals(defaultURL2)){

                                if (name.equals("unknown")){
                                    posterImages.add(new AdminImage(profileID, imageURL, defaultURL2));
                                }
                                else{
                                    posterImages.add(new AdminImage(profileID, imageURL, defaultURL1));
                                }
                            }
                        }
                    }
                }
                imageAdapter.notifyDataSetChanged();
            }
        });

    }




    private void fetchImages(String imageType) {



        String collectionPath = imageType.equals("ProfileImages") ? "ProfileImages" : "PosterImages";
        String imageUrlField = imageType.equals("ProfileImages") ? "imageURL" : "image";
        String defaultURL = "https://firebasestorage.googleapis.com/v0/b/fiftysix-a4bcf.appspot.com/o/images%2FDoNotDeleteStockProfilePic%2Fno-photos.png?alt=media&token=52497ae1-5e13-49cb-a43b-379f85849c73";

        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        imageUrls.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString(imageUrlField);
                            if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals(defaultURL)) {
                                imageUrls.add(imageUrl);
                            }
                        }
                        imageAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("ImageDisplayActivity", "Error getting images: ", task.getException());
                    }
                });
    }

}