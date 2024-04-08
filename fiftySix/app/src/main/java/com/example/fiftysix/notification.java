package com.example.fiftysix;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;

/**
 * Notification Class, used to create and send notifications from organizer to attendee.
 *
 * @author Arsh.
 * @version 1
 * @since SDK34
 */
public class notification {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public void sendNotificationToEventAttendees(String eventId, String eventName, String message, Context context) throws JSONException {
        Log.d("even_id",eventId);
        Map<String,String> data = new HashMap<>();
        data.put("announcements",message);



        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            if (documentSnapshot.contains("announcements")) {
                                // Field exists
                                List<String> currentNotifications = (List<String>) documentSnapshot.get("announcements");
                                // Add the new notification to the list
                                currentNotifications.add(message);
                                db.collection("Events").document(eventId).update("announcements", currentNotifications)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Notification added successfully
                                                Log.d("notification_update", "Updated notifications in the database");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle failures
                                                Log.d("notification_update", "Failed to update notifications due to: " + e.getMessage());
                                            }
                                        });

                                // now we have  to add the notificaitons on the list
                            } else {
                                List<String> notifications = new ArrayList<>();
                                notifications.add(message);
                                // Add the new field with the list of notifications
                                db.collection("Events").document(eventId).update("announcements", notifications)
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
        // sendNotification(context, eventId,eventName, message);
        // need to store notifications in side the collection of events  event document
        Map<String, Object> notificationData = new HashMap<>();
        List<String> attendeesIds = new ArrayList<>(); // Empty for now, to be filled later
        notificationData.put("attendees", attendeesIds);
        notificationData.put("event_name",eventName);
        notificationData.put("notification",message);
        db.collection("Events").document(eventId).collection("Notifications").add(notificationData)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Notification added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding notification", e));

    }




    private void sendNotification(Context context, String eventId, String eventName, String message) {
        createNotificationChannel(context,eventId);

        // Get the notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Build the notification
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context,eventId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(eventName)
                    .setContentText(message)
                    .setPriority(Notification.PRIORITY_HIGH);
        }

        // Show the notification
        notificationManager.notify(100, builder.build());
    }
    private void createNotificationChannel(Context context,String eventId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(eventId, "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel Description");

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

//

}
