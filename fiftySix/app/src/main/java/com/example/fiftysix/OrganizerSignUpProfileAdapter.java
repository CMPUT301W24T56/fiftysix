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
/**
 * Adapter to display attendees signed up to the organizers event, used in OrganizerSignUpDataActivity
 *
 * @author Brady.
 * @version 1
 * @since SDK34
 */
public class OrganizerSignUpProfileAdapter extends RecyclerView.Adapter<OrganizerSignUpProfileAdapter.EventVH> {

    List<Profile> profileList;
    private Context context;

    public OrganizerSignUpProfileAdapter(ArrayList<Profile> eventList, Context context) {
        this.profileList = eventList;
        this.context = context;
    }


    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_signup_data_row, parent, false);
        return new EventVH(view);
    }

    /**
     * Sets the attendee info
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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

        boolean isExpandable = profileList.get(position).getExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    /**
     * gets the attendee info and finds views
     */
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

                    Profile profile = profileList.get(getAdapterPosition());
                    profile.setExpandable(!profile.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}
