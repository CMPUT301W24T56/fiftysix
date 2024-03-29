package com.example.fiftysix;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.EventVH> {

    List<Event> eventList;
    private Context context;

    public OrganizerEventAdapter(ArrayList<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }


    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_row, parent, false);
        return new EventVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {



        Event event = eventList.get(position);
        holder.codeName.setText(event.getEventName());
        holder.versionTxt.setText(event.getLocation());
        holder.apiLevelTxt.setText(event.getDate());
        holder.descriptionTxt.setText(event.getDetails());

        Integer checkInLimit = event.getAttendeeLimit();
        Integer signUpLimit = event.getAttendeeLimit();
        Integer checkins = event.getAttendeeCount();
        Integer signups = event.getSignUpCount();
        holder.signUpPieChart.clearChart();
        holder.checkinPieChart.clearChart();

        if(checkInLimit == 2147483647){
            holder.descriptionEvent.setText("Max: Unlimited");
            if (checkins != 0){
                checkInLimit = checkins;
            }
            if (signups != 0){
                signUpLimit = signups;
            }
        }
        else{
            holder.descriptionEvent.setText("Max: " + event.getAttendeeLimit().toString());
            checkInLimit -= checkins;
            signUpLimit -= signups;
        }


        holder.signUpPieChart.addPieSlice(new PieModel(
                "R",
                signUpLimit,
                Color.parseColor("#f3e7db")));
        holder.signUpPieChart.addPieSlice(new PieModel(
                "R",
                signups,
                Color.parseColor("#aa8565")));


        holder.checkinPieChart.addPieSlice(new PieModel(
                "R",
                checkInLimit,
                Color.parseColor("#f3e7db")));
        holder.checkinPieChart.addPieSlice(new PieModel(
                "R",
                checkins,
                Color.parseColor("#aa8565")));






        holder.attendeeCount.setText(event.getAttendeeCount().toString());
        holder.currentSignUps.setText(event.getSignUpCount().toString());



        String imageUrl = event.getPosterURL();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .into(holder.eventImage); // Your ImageView
        }

        boolean isExpandable = eventList.get(position).getExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventVH extends RecyclerView.ViewHolder {

        TextView codeName, versionTxt, apiLevelTxt, descriptionTxt, attendeeCount, descriptionEvent, currentSignUps;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView eventImage;
        Button send_notification, edit_event,Download_qr_code;
        ImageButton attendees, signUps;
        Event event;
        String eventID;
        PieChart signUpPieChart, checkinPieChart;

        public EventVH(@NonNull View itemView) {
            super(itemView);

            codeName = itemView.findViewById(R.id.code_name);
            versionTxt = itemView.findViewById(R.id.version);
            apiLevelTxt = itemView.findViewById(R.id.apiLevel);
            descriptionTxt = itemView.findViewById(R.id.description);
            attendeeCount = itemView.findViewById(R.id.currentAttendees);
            descriptionEvent = itemView.findViewById(R.id.attendeeCapacity);
            currentSignUps = itemView.findViewById(R.id.currentSignUps);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            eventImage = itemView.findViewById(R.id.event_poster_image);
            send_notification = itemView.findViewById(R.id.notify);
            attendees = itemView.findViewById(R.id.checkInsButton);
            signUps = itemView.findViewById(R.id.signUpsButton);
            edit_event = itemView.findViewById(R.id.EditEvent);

            signUpPieChart = itemView.findViewById(R.id.piechartSignUp);
            checkinPieChart = itemView.findViewById(R.id.piechartCheckIn);
            Download_qr_code = itemView.findViewById(R.id.share_qr_code);
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    event = eventList.get(getAdapterPosition());
                    eventID = event.getEventID();
                    event.setExpandable(!event.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                    Log.d("OrgEventAdapt", "EventID =  "+ event.getEventID());

                }
            });

            send_notification.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, send_notification.class); // Assuming SendNotificationActivity is your activity name
                    Log.d("TAG", "onClick: working now ");
                    context.startActivity(intent);
                }
            });

            edit_event.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Let organizer edit event Details
                }
            });

            attendees.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Let organizer edit event Details
                    event = eventList.get(getAdapterPosition());
                    eventID = event.getEventID();
                    Log.d("attendees", "EventID =  "+ eventID);
                    Intent intent2 = new Intent(context, OrganizerCheckInDataActivity.class);
                    intent2.putExtra("eventID", eventID);
                    context.startActivity(intent2);
                }
            });

            signUps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    event = eventList.get(getAdapterPosition());
                    eventID = event.getEventID();
                    Log.d("attendees", "EventID =  "+ eventID);
                    Intent intent3 = new Intent(context, OrganizerSignUpDataActivity.class);
                    intent3.putExtra("eventID", eventID);
                    context.startActivity(intent3);

                }
            });
            // converting the string of qrcode to bitmap . so that person can either download or share the qr_code image
            // https://easyonecoder.com/android/basic/GenerateQRCode
            // author- Easy One Coder
            Download_qr_code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     event = eventList.get(getAdapterPosition());
                     eventID = event.getEventID();

                    //  now we need to get the event qrcode
                    //  we don't have to promotional qrcode for now so I am using check in qr code to test for now.
                    // facing error in the function getCheckInQrCodeID();
                    String qr_code = "-Nt4abMIseYLp_n31B3S"; //event.getPromoQRCodeID();
                    Log.d("qr_code","qrcode string: " + qr_code);
                    // now we need to convert this qr code into  bit map image
                    MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                    // reference of data transfer to other apps.
                    // https://www.youtube.com/watch?v=qbtlrGHOVjg

                    try {
                        BitMatrix bitMatrix = multiFormatWriter.encode(qr_code, BarcodeFormat.QR_CODE,300,300);

                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                        Log.d("qr_code", "converting string to bitmap is working ");

                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        File qrCodeFile = saveBitmapToFile(bitmap);
                        Log.d("file","converted  image bitmap to file");
                        // Get content URI using FileProvider

                        Uri contentUri = FileProvider.getUriForFile(context, "com.example.fiftysix.provider", qrCodeFile);


                        Log.d("file_share","converted  file to uri  for sharing purpose");

                        intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        intent.setType("image/png");

                        if (intent.resolveActivity(context.getPackageManager())!= null ){
                            startActivity(intent);
                        }
                    }catch (WriterException e){
                        throw  new RuntimeException(e);
                    }
                }
            });

        }

        // storing the bitmap image in a file to share it to different apps
        public File saveBitmapToFile(Bitmap bitmap){
            File filesDir = context.getApplicationContext().getFilesDir();
            File qrCodeFile = new File(filesDir, "qr_code.png");
            try (FileOutputStream out = new FileOutputStream(qrCodeFile)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return qrCodeFile;
        }
    }

}
