package com.example.fiftysix;

import static android.content.ContentValues.TAG;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class notification {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public void sendNotificationToEventAttendees(String eventId, String eventName, String message) {
        Log.d("even_id",eventId);
        Map<String,String> data = new HashMap<>();
        data.put("notification",message);
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("notification")) {
                                // Field exists
                                List<String> currentNotifications = (List<String>) documentSnapshot.get("notification");
                                // Add the new notification to the list
                                currentNotifications.add(message);

                                // now we have  to add the notificaitons on the list
                            } else {
                                List<String> notifications = new ArrayList<>();
                                notifications.add(message);
                                // Add the new field with the list of notifications
                                db.collection("Events").document(eventId).update("notification", notifications)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Notification field created successfully
                                                Log.d("notification_addition","added notification to the database");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle failures
                                                Log.d("notification-addition","addition failed due to some error");

                                            }
                                        });
                                // Field does not exist
                                // now we need to create the field notification.
                            }
                        } else {

                            // Document does not exist
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failures
                    }
                });
    }
}
