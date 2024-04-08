package com.example.fiftysix;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * PromoQRCode class, creates and stores a promotion QRCode for a given event.
 *
 * @author Rakshit, Arsh, Bruce, Brady.
 * @version 1
 * @since SDK34
 */
public class PromoQRCode {

    private Integer qrWidth = 400; // Change values to edit QR code width and height.
    private Integer qrHeight = 400;
    private String type;
    private String eventID;
    private Bitmap qrCode;
    private String qrCodeID;
    private FirebaseFirestore db;
    private CollectionReference qrRef;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String imagePath;
    private Context mContext;
    private Uri uriToBitMap;


    /**
     * Creates a Promo QRCode
     * @param contextIN Context application context
     */
    public PromoQRCode( Context contextIN ) {
        this.eventID = eventID;
        this.qrCodeID = FirebaseDatabase.getInstance().getReference("CheckInQRCode").push().getKey(); // Used as the Primary Key in firebase.
        this.db = FirebaseFirestore.getInstance();
        this.qrRef = db.collection("CheckInQRCode");
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = storage.getReference();
        this.imagePath = "images/promoQRCode/" + qrCodeID;
        this.mContext = contextIN;
        this.eventID = null;
        this.uriToBitMap = null;
        this.type = "promo";
    }


    /**
     * Sets the event used to create the promo QR Code
     * @param eventID
     */
    public void setEvent(String eventID){
        this.eventID = eventID;
        this.generateQR();
    }


    /**
     * Generates a Promo QR code (Bitmap) containing a string of the eventID. Adds image of qr code and event data to firebase.
     */
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

    /**
     * Adds QR code data to firebase. Helper function for generateQR().
     */
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


    /**
     * Adds QR code Image to firebase cloud. Helper function for generateQR().
     * @param image Uri image of qrcode to be uploaded
     */
    private void uploadImage(Uri image){
        // Create a reference to 'images/mountains.jpg'
        StorageReference qrImagesRef = storageRef.child(this.imagePath);
        qrImagesRef.putFile(image).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Cloud", "Promo QR Code Image successfully uploaded!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Cloud", "ERROR: Promo QR Code Image did not upload.");
            }
        });

    }


    /**
     * Converts Bitmap to Uri so it can be added to firebase cloud. Helper function for uploadImage().
     * Reference: https://colinyeoh.wordpress.com/2012/05/18/android-getting-image-uri-from-bitmap/
     * @param inContext Context application context
     * @param inImage Bitmap image to be converted to a Uri
     * @return Returns Uri of QR Code
     */
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage,"IMG_" + Calendar.getInstance().getTime() , null);
        return Uri.parse(path);

    }


    // Basic Getters & Setters.
    public String getEventID() {
        return eventID;
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    public String getQRCodeID(){ return qrCodeID; }

}
