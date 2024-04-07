package com.example.fiftysix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.UriUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// This class Creates and Stores a QRCode for attendees to check into an event with.
// Requires a (String) eventID to instantiate this class.
// QRCode is uploaded to Firebase.

public class CheckInQRCode{

    // MIGHT need to add an organizer ID

    // Change values to edit QR code width and height.
    private Integer qrWidth = 400;
    private Integer qrHeight = 400;
    private String eventID;
    private Bitmap qrCode;
    private String qrCodeID;
    private String type = "checkIn";
    private FirebaseFirestore db;
    private CollectionReference qrRef;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String imagePath;
    private Context mContext;
    private Uri uriToBitMap;




    // Constructor: requires eventID to instantiate.
    public CheckInQRCode(String eventID, Context contextIN) {

        this.eventID = eventID;


        // Used as the Primary Key in firebase.
        this.qrCodeID = FirebaseDatabase.getInstance().getReference("CheckInQRCode").push().getKey();
        this.db = FirebaseFirestore.getInstance();
        this.qrRef = db.collection("CheckInQRCode");
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReference();
        this.imagePath = "images/checkInQRCode/" + qrCodeID;
        this.mContext = contextIN;
        this.eventID = eventID;
        this.uriToBitMap = null;

        this.generateQR();

    }

    public CheckInQRCode() {
    }

    public interface CheckInQRCodeCallback {
        void onSuccess(Boolean validQR);
        void onFailure(Exception e);
    }


    // Basic Getters & Setters.
    public String getEventID() {
        return eventID;
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    public Bitmap getQrCode() {
        return qrCode;
    }
    public String getQRCodeID(){ return qrCodeID; }


    /**
     * Checks if the QRCode is eligible for reuse. To be allowed it must belong to a previous event created in our app (exists in the database),
     * and the event must be atleast one of an event that isw finished (finish date has past by 1 day) or event was deleted by the admin.
     * Not allowed to reuse QRCode from active events.
     * @param reuseQRId String of the QR code id that we would like to check if it can be reviewed
     * @param callback Used as a function point to store a boolean, true if QR can be reused, false if not.
     */
    public void checkValidReuseQR(String reuseQRId, CheckInQRCodeCallback callback){
        qrRef.document(reuseQRId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if ( (documentSnapshot.exists()) && (documentSnapshot != null) && (documentSnapshot.getString("event") != null) ){
                    String oldEventId = documentSnapshot.getString("event");

                    db.collection("Events").document(oldEventId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot dSnapshot) {
                            if ( (dSnapshot.exists()) && (dSnapshot != null) && (dSnapshot.getString("endDate") != null) ){
                                String endDate = dSnapshot.getString("endDate");
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                                Date strDate = null;
                                try {
                                    strDate = sdf.parse(endDate);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                if (new Date().after(strDate)) {
                                    //Event is old and in our database. can be reused.
                                    callback.onSuccess(true);
                                } else {
                                    callback.onSuccess(false);
                                }
                            }

                            // Event was deletds by admin and can use QR code again
                            else{
                                callback.onSuccess(true);
                            }
                        }
                    });

                }
                else{
                    callback.onSuccess(false);
                }
            }
        });


    }



    // Generates a QR code (Bitmap) containing a string of the eventID. Adds image of qr code and event data to firebase.
    private void generateQR() {

        MultiFormatWriter writer = new MultiFormatWriter();
        try {

            // Generates QRCode Image.
            BitMatrix matrix = writer.encode(qrCodeID, BarcodeFormat.QR_CODE, qrWidth, qrHeight);
            BarcodeEncoder encoder = new BarcodeEncoder();
            this.qrCode = encoder.createBitmap(matrix);


            // Adds QRcode to firebase.
            Map<String,Object> qrData = new HashMap<>();
            qrData.put("event",this.eventID);
            qrData.put("type",this.type);
            qrRef
                    .document(this.qrCodeID)
                    .set(qrData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Firestore", "Check-in QR Code successfully written!");
                        }
                    });

            // Uploads Bitmap Image to cloud Storage.
            uploadData();

            // NOT NEEDED: Uploads qr image to cloud

            // Converts Bitmap to Uri
            Context context = this.mContext;
            uriToBitMap = getImageUri(context, this.qrCode);
            // Uploads Uri of Bitmap to firbase cloud.
            uploadImage(uriToBitMap);

            // Sets view image to QR code, Uncomment and update view ID to display
            //qrViewID.setImageBitmap(bitmap);

        } catch (WriterException e) {
            throw new RuntimeException(e);
        }


    }

    // Adds QR code data to firebase. Helper function for generateQR().
    private void uploadData(){
        Map<String,Object> qrData = new HashMap<>();
        qrData.put("event",this.eventID);
        qrData.put("type",this.type);

        this.qrRef
                .document(this.qrCodeID)
                .set(qrData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore", "Check-in QR Data successfully written!");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firestore", "ERROR: Check-in QR Data failed to upload.");
                    }
                });
    }

    // Adds QR code Image to firebase cloud. Helper function for generateQR().
    private void uploadImage(Uri image){
        // Create a reference to 'images/mountains.jpg'
        StorageReference qrImagesRef = storageRef.child(this.imagePath);
        qrImagesRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Cloud", "Check-in QR Code Image successfully uploaded!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Cloud", "ERROR: Check-in QR Code Image did not upload.");
            }
        });

    }

    // Converts Bitmap to Uri so it can be added to firebase cloud. Helper function for uploadImage().
    // TA said this was okay to copy, given it was a small function.
    // FOUND THIS ONLINE: https://stackoverflow.com/questions/8295773/how-can-i-transform-a-bitmap-into-a-uri
    // Original Source: https://colinyeoh.wordpress.com/2012/05/18/android-getting-image-uri-from-bitmap/
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,"IMG_" + Calendar.getInstance().getTime() , null);
        return Uri.parse(path);

    }


}
