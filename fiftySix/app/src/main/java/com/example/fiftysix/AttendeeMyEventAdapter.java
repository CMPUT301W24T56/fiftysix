package com.example.fiftysix;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AttendeeMyEventAdapter extends RecyclerView.Adapter<AttendeeMyEventAdapter.EventVH> {

    List<Event> eventList;
    Context mContext;

    public AttendeeMyEventAdapter(ArrayList<Event> eventList, Context mContext) {
        this.eventList = eventList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendee_my_events_row, parent, false);
        return new EventVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {

        Event event = eventList.get(position);
        holder.eventName.setText(event.getEventName());
        holder.eventDate.setText(event.getLocation());
        holder.eventLocation.setText(event.getDate());
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

        TextView eventName, eventDate, eventLocation, descriptionTxt, attendeeCount, descriptionEvent;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView eventImage;
        Button leaveEvent, viewAnnouncements;

        public EventVH(@NonNull View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.event_name);
            eventDate = itemView.findViewById(R.id.event_date);
            eventLocation = itemView.findViewById(R.id.event_location);
            descriptionTxt = itemView.findViewById(R.id.description);
            attendeeCount = itemView.findViewById(R.id.attendeeCapacity);
            descriptionEvent = itemView.findViewById(R.id.currentAttendees);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            eventImage = itemView.findViewById(R.id.event_poster_image);
            leaveEvent = itemView.findViewById(R.id.leave_event);
            viewAnnouncements = itemView.findViewById(R.id.view_announcements);


            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Event event = eventList.get(getAdapterPosition());
                    event.setExpandable(!event.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });

            leaveEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Attendee attendee = new Attendee(mContext);
                    Event event = eventList.get(getAdapterPosition());
                    attendee.leaveEvent(event.getEventID());

                }
            });

            viewAnnouncements.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Event event = eventList.get(position);
                        fetchAndDisplayAnnouncements(event.getEventID(), itemView.getContext());
                    }
                }
            });

        }
    }

    private void fetchAndDisplayAnnouncements(String eventId, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("announcements")) {
                        List<String> announcements = (List<String>) documentSnapshot.get("announcements");
                        assert announcements != null;
                        displayAnnouncements(context, announcements);
                    } else {
                        Toast.makeText(context, "No announcements available.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Error fetching announcements.", Toast.LENGTH_SHORT).show());
    }

    private void displayAnnouncements(Context context, List<String> announcements) {
        // Create and show the announcements dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Announcements");
        String[] items = announcements.toArray(new String[0]);
        builder.setItems(items, null);
        builder.setPositiveButton("Close", null);
        AlertDialog dialog = builder.create();

        // Show the dialog only if the activity is not finishing to avoid BadTokenException
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            dialog.show();
        }
    }

}
