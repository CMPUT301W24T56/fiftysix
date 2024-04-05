package com.example.fiftysix;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class OrganizerCheckInEventAdapter extends RecyclerView.Adapter<OrganizerCheckInEventAdapter.EventVH> {

    List<Profile> profileList;
    private Context context;

    public OrganizerCheckInEventAdapter(ArrayList<Profile> profileList, Context context) {
        this.profileList = profileList;
        this.context = context;
    }


    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_checkin_data_row, parent, false);
        return new EventVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {

        Profile profile = profileList.get(position);

        String imageUrl = profile.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .into(holder.attendeeImage); // Your ImageView
        }

        holder.attendeeName.setText(profile.getName());
        holder.phoneNumber.setText(profile.getPhoneNumber());
        holder.email.setText(profile.getEmail());
        holder.numberCheckins.setText("Times checked in: " + profile.getTimesCheckedIn());
        holder.checkinTime.setText("Check-in time: " + profile.getCheckInTime());



        boolean isExpandable = profileList.get(position).getExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    public class EventVH extends RecyclerView.ViewHolder {

        TextView attendeeName, checkinTime, numberCheckins, phoneNumber, email;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView attendeeImage;


        public EventVH(@NonNull View itemView) {
            super(itemView);

            attendeeName = itemView.findViewById(R.id.attendee_name);

            email = itemView.findViewById(R.id.checkin_time);
            phoneNumber = itemView.findViewById(R.id.number_checkins);
            numberCheckins = itemView.findViewById(R.id.phone_number);
            checkinTime = itemView.findViewById(R.id.email);


            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            attendeeImage = itemView.findViewById(R.id.event_poster_image);


            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Profile profile = profileList.get(getAdapterPosition());
                    profile.setExpandable(!profile.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });




        }
    }

}
