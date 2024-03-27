package com.example.fiftysix;

import static androidx.databinding.DataBindingUtil.setContentView;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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


        if(event.getAttendeeLimit() == 2147483647){
            holder.descriptionEvent.setText("Capacity: Unlimited");
        }
        else{
            holder.descriptionEvent.setText("Capacity: " + event.getAttendeeLimit().toString());
        }

        holder.attendeeCount.setText("Current Attendees: " + event.getAttendeeCount().toString());



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

        TextView codeName, versionTxt, apiLevelTxt, descriptionTxt, attendeeCount, descriptionEvent;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView eventImage;
        Button send_notification, edit_event, attendees,share_qr_code;
        Event event;
        String eventID;

        public EventVH(@NonNull View itemView) {
            super(itemView);

            codeName = itemView.findViewById(R.id.code_name);
            versionTxt = itemView.findViewById(R.id.version);
            apiLevelTxt = itemView.findViewById(R.id.apiLevel);
            descriptionTxt = itemView.findViewById(R.id.description);
            attendeeCount = itemView.findViewById(R.id.attendeeCapacity);
            descriptionEvent = itemView.findViewById(R.id.currentAttendees);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            eventImage = itemView.findViewById(R.id.event_poster_image);
            send_notification = itemView.findViewById(R.id.notify);
            attendees = itemView.findViewById(R.id.attendeeDetails);
            edit_event = itemView.findViewById(R.id.EditEvent);
            share_qr_code = itemView.findViewById(R.id.share_qr_code);


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
                    Intent intent2 = new Intent(context, OrganizerAttendeeDataActivity.class);
                    intent2.putExtra("eventID", eventID);
                    context.startActivity(intent2);
                }
            });

            share_qr_code.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // sending qrcode
                    event = eventList.get(getAdapterPosition());
                    eventID = event.getEventID();
                    // need to  fetch the qrcode details
                    String qr_code =  event.getCheckInQRCodeID();
                    // now we have the qr_code . now we need to save this
                    //
                    int qrCodeWidth = 500;
                    int qrCodeHeight = 500;

                    // Generate QR code
                    Bitmap bitmap = generateQRCode(qr_code, qrCodeWidth, qrCodeHeight);
                    // now we need to share the bitmap
                    // https://developer.android.com/training/sharing/send?_gl=1*1iar2kj*_up*MQ..*_ga*MTczODQ1Mzk4MC4xNzExNTI2MDU1*_ga_6HH9YJMN9M*MTcxMTUyNjA1NC4xLjAuMTcxMTUyNjA1NC4wLjAuMA..

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);

                }
            });

        }
        // author: openai
        // promptts how to share qrocde / data via different apps
        // taking reference from the chatgpt on using lilbrary to conver string to qrcode
        // https://chat.openai.com/c/8caccfbf-6f10-4986-8961-3537b0fe6701
        private Bitmap generateQRCode(String qrCodeData, int width, int height) {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            try {
                BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, width, height);
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
                return bitmap;
            } catch (WriterException e) {
                e.printStackTrace();
//                Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                return null;
            }
        }
    }

}
