package com.example.fiftysix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class AttendeeCheckinEventAdapter extends RecyclerView.Adapter<AttendeeCheckinEventAdapter.EventVH> {

    List<Event> eventList;
    Context mContext;

    public AttendeeCheckinEventAdapter(ArrayList<Event> eventList, Context mContext) {
        this.eventList = eventList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public AttendeeCheckinEventAdapter.EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendee_signups_row, parent, false);
        return new AttendeeCheckinEventAdapter.EventVH(view);
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
            holder.descriptionEvent.setText("Sign-Up Limit: " + event.getAttendeeLimit().toString());
        }

        holder.attendeeCount.setText("People Signed-Up: " + event.getSignUpCount().toString());



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
        Button cancelSignupButton, checkinButton;

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
            cancelSignupButton = itemView.findViewById(R.id.cancel_signup);
            checkinButton = itemView.findViewById(R.id.checkin_from_signup);




            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Event event = eventList.get(getAdapterPosition());
                    event.setExpandable(!event.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });






            cancelSignupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Attendee attendee = new Attendee(mContext);
                    Event event = eventList.get(getAdapterPosition());
                    attendee.leaveSignUp(event.getEventID());
                    eventList.clear();
                }
            });



            checkinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Attendee attendee = new Attendee(mContext);
                    Event event = eventList.get(getAdapterPosition());
                    attendee.checkInToEventID(event.getEventID());
                }
            });







        }
    }

}

