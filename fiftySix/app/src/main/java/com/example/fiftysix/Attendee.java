package com.example.fiftysix;

// Has a profile they can view\edit.
// Can View event details and announcements within the app event.
// Check into an event by scanning the provided QR code.
// Recieve push notifications from event organizers.
// Can log in without username or password (Use device ID). GET DEVICE ID



// Can enable/disable geolocation tracking. (NOT FOR PART 3)


import static android.hardware.usb.UsbDevice.getDeviceId;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Attendee {
    private String attendeeID;
    private List<String> eventIDs;
    private Context mContext;
    private String eventID;

    private String userType = "attendee";
    private FirebaseFirestore db;
    private CollectionReference ref;
    public Attendee(){
        this.mContext = mContext;
        this.attendeeID = getDeviceId();
        this.db = FirebaseFirestore.getInstance();
        this.ref = db.collection("Users");
    }


    // Gets android ID to be used as attendee ID
    // Got from https://stackoverflow.com/questions/60503568/best-possible-way-to-get-device-id-in-android
    public String getDeviceId() {
        String id = Settings.Secure.getString(this.mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return id;
    }

    public void set_profile(String name,String email, int phone_number, Bitmap profile_image){

    }


}
