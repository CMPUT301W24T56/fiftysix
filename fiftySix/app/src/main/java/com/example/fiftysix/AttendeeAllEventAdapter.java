package com.example.fiftysix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AttendeeAllEventAdapter extends RecyclerView.Adapter<AttendeeAllEventAdapter.EventVH> {

    List<Event> eventList;
    Context mContext;

    public AttendeeAllEventAdapter(ArrayList<Event> eventList, Context mContext) {
        this.eventList = eventList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendee_all_events_row, parent, false);
        return new EventVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {

        Event event = eventList.get(position);
        holder.eventName.setText(event.getEventName());
        holder.locationOfEvent.setText(event.getLocation());
        holder.descriptionTxt.setText(event.getDetails());



        Integer signUpLimit = event.getSignUpLimit();
        Integer signups = event.getSignUpCount();


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


        if(signUpLimit == 2147483647){
            holder.attendeeLimit.setText("Sign-up Limit: Unlimited");
        }
        else{
            holder.attendeeLimit.setText("Sign-up Limit: " + event.getSignUpLimit().toString());
        }



        holder.attendeeCount.setText("Sign-ups: " + signups.toString());


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

        TextView eventName, locationOfEvent, startDate, endDate, descriptionTxt, attendeeLimit, attendeeCount;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView eventImage;
        Button signupEvents;

        public EventVH(@NonNull View itemView) {
            super(itemView);

            eventName = itemView.findViewById(R.id.eventName);
            locationOfEvent = itemView.findViewById(R.id.locationOfEvent);

            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);
            descriptionTxt = itemView.findViewById(R.id.description);
            attendeeLimit = itemView.findViewById(R.id.attendeeCapacity);
            attendeeCount = itemView.findViewById(R.id.currentAttendees);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            eventImage = itemView.findViewById(R.id.event_poster_image);
            signupEvents = itemView.findViewById(R.id.SignupEvents);


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


            // Allows attendee to Sign-up for an event from browsing all events

            signupEvents.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Attendee attendee = new Attendee(mContext);
                    Event event = eventList.get(getAdapterPosition());
                    attendee.signUpForEvent(event.getEventID());


                }
            });




        }
    }

}
