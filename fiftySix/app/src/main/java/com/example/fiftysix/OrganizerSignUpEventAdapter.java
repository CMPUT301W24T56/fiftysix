package com.example.fiftysix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class OrganizerSignUpEventAdapter extends RecyclerView.Adapter<OrganizerSignUpEventAdapter.EventVH> {

    List<Profile> eventList;
    private Context context;

    public OrganizerSignUpEventAdapter(ArrayList<Profile> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }


    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_signup_data_row, parent, false);
        return new EventVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {

        Profile profile = eventList.get(position);
        holder.attendeeName.setText(profile.getName());
        holder.phoneNumber.setText(profile.getPhoneNumber());
        holder.email.setText(profile.getEmail());



        String imageUrl = profile.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .into(holder.attendeeImage); // Your ImageView
        }

        boolean isExpandable = eventList.get(position).getExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventVH extends RecyclerView.ViewHolder {

        TextView attendeeName, checkinTime,  phoneNumber, email;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView attendeeImage;


        public EventVH(@NonNull View itemView) {
            super(itemView);

            attendeeName = itemView.findViewById(R.id.attendee_name);
            checkinTime = itemView.findViewById(R.id.checkin_time);
            phoneNumber = itemView.findViewById(R.id.phone_number);
            email = itemView.findViewById(R.id.email);


            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            attendeeImage = itemView.findViewById(R.id.event_poster_image);


            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Profile profile = eventList.get(getAdapterPosition());
                    profile.setExpandable(!profile.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });

        }
    }

}
