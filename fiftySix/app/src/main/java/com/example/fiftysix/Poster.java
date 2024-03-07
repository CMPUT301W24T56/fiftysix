package com.example.fiftysix;

import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Poster {
    private static final String TAG = "Poster";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String DEFAULT_IMAGE_URL = "https://firebasestorage.googleapis.com/v0/b/fiftysix-a4bcf.appspot.com/o/images%2FPosters%2Fno-photos.png?alt=media";



    // Method to upload the image to Firebase Storage and store its reference in Firestore
    public void uploadImageAndStoreReference(Uri imageUri, String posterName, String eventType) {
        if (imageUri != null) {
            StorageReference fileReference = FirebaseStorage.getInstance().getReference("images/Posters/" + posterName + ".jpg");
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Log.d(TAG, "Image Upload Successful. Image Uri: " + downloadUri.toString());
                        storeImageReferenceInFirestore(downloadUri.toString(), posterName, eventType);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "URL retrieval failed.", e);
                    }))
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Image Upload Failed.", e);
                    });
        } else {
            Log.d(TAG, "No Image Selected! Using default poster.");
            storeImageReferenceInFirestore(DEFAULT_IMAGE_URL, posterName, eventType);
        }
    }

    // Method to store the image reference in Firestore
    private void storeImageReferenceInFirestore(String imageUrl, String posterName, String eventType) {
        Map<String, Object> posterData = new HashMap<>();
        posterData.put("image", imageUrl);
        posterData.put("poster", posterName);
        posterData.put("type", eventType);

        db.collection("Images").document(posterName).set(posterData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Firestore update successful for poster: " + posterName);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore update failed for poster: " + posterName, e);
                });
    }
}
