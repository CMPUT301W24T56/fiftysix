package com.example.fiftysix;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
/**
 * Profile adapter used to display profile data in AdminBrowseProfiles
 * @author Rakshit, Arsh
 * @version 1
 * @since SDK34
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {
    private final List<Profile> profileList;
    private OnItemClickListener onItemClickListener;

    public AdminProfileAdapter(List<Profile> profileList, OnItemClickListener onItemClickListener) {
        this.profileList = profileList;
        this.onItemClickListener = onItemClickListener; // Set the listener
    }

    /**
     * Used to save the profile clicked on and pass it to AdminBrowseProfiles
     */
    public interface OnItemClickListener {
        void onItemClick(Profile profile);
    }


    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView nameView, emailView, phoneView;

        /**
         * Sets text in textview
         * @param itemView View
         */
        public ProfileViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.tvProfileName);
            emailView = itemView.findViewById(R.id.tvProfileEmail);
            phoneView = itemView.findViewById(R.id.tvProfilePhoneNumber);
        }
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        Profile profile = profileList.get(position);
        Log.d("profilesid", profile.getProfileID());
        Log.d("AdminProfileAdapter", "Binding profile: " + profile.getName());
        holder.nameView.setText(profile.getName());
        holder.emailView.setText(profile.getEmail());
        holder.phoneView.setText(profile.getPhoneNumber());
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(profile));
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }


    /**
     * Update method to refresh the list
     * @param profiles List of all profiles in the database
     */
    public void updateProfiles(List<Profile> profiles) {
        profileList.clear();
        profileList.addAll(profiles);
        notifyDataSetChanged();
    }
}
