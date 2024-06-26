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
/**
 * Adapted to display event data in attendee browse checkins page expanding recycler view. Allows attendee to cancel checkin and view event announcements.
 * @author Rakshit, Brady.
 * @version 1
 * @since SDK34
 */
public class AttendeeCheckInsEventAdapter extends RecyclerView.Adapter<AttendeeCheckInsEventAdapter.EventVH> {

    List<Event> eventList;
    Context mContext;

    public AttendeeCheckInsEventAdapter(ArrayList<Event> eventList, Context mContext) {
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
        holder.eventLocation.setText(event.getLocation());
        holder.descriptionTxt.setText(event.getDetails());

        String endDay = event.getEndDate();
        String endTime = event.getEndTime();
        String startDay = event.getStartDate();
        String startTime = event.getStartTime();

        if (endDay != null){
            String start = "Event Start: " + startTime.toString() + ",  " + startDay.toString();
            String end =   "Event End:  " + endTime.toString() + ",  " + endDay.toString();
            holder.startDate.setText(start);
            holder.endDate.setText(end);

        }

        if(event.getCheckInLimit() == 2147483647){
            holder.currentAttendees.setText("Check-in Limit: Unlimited");
        }
        else{
            holder.currentAttendees.setText("Check-in Limit: " + event.getCheckInLimit().toString());
        }

        holder.attendeeCapacity.setText("Check-ins: " + event.getAttendeeCount().toString());


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


    /**
     *  Event View Holder for Expanding RecyclerView. Allows attendee to cancel checkin or view event announcement and view event details.
     */
    public class EventVH extends RecyclerView.ViewHolder {

        TextView eventName, startDate, endDate, eventLocation, descriptionTxt, attendeeCapacity, currentAttendees;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView eventImage;
        Button leaveEvent, viewAnnouncements;

        public EventVH(@NonNull View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.event_name);
            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);

            eventLocation = itemView.findViewById(R.id.locationOfEvent);
            descriptionTxt = itemView.findViewById(R.id.description);
            currentAttendees = itemView.findViewById(R.id.attendeeCapacity);
            attendeeCapacity = itemView.findViewById(R.id.currentAttendees);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            eventImage = itemView.findViewById(R.id.event_poster_image);
            leaveEvent = itemView.findViewById(R.id.leave_event);
            viewAnnouncements = itemView.findViewById(R.id.view_announcements);


            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Event event = eventList.get(getAdapterPosition());
                    if (event.getEventID() == null){
                        event.setExpandable(false);
                        notifyItemChanged(getAdapterPosition());
                   }
                    else{
                        event.setExpandable(!event.getExpandable());
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            });

            leaveEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Attendee attendee = new Attendee(mContext);
                    Event event = eventList.get(getAdapterPosition());
                    attendee.leaveEvent(event.getEventID());
                    notifyItemChanged(getAdapterPosition());

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

    /**
     * Fetches event announcements to the attendee.
     *
     * @param eventId String of event ID to get announcements for.
     * @param context application Context
     */
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


    /**
     * Displays all event announcements to the attendee.
     *
     * @param context application Context
     * @param announcements List<String> containing all announcements for the event.
     */
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
