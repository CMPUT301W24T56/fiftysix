package com.example.fiftysix;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;


/**
 * MilestoneAdapter, used to display milestone data inside of listView.
 * Reference: https://stackoverflow.com/questions/8166497/custom-adapter-for-list-view
 *
 * @author Brady.
 * @version 1
 * @since SDK34
 */
public class MilestoneAdapter extends ArrayAdapter<MileStone> {

    private Context mContext;
    private int mResource;
    private String title;
    private String description;

    public MilestoneAdapter(Context context, int resource, ArrayList<MileStone> items) {
        super(context, resource, items);
        this.mResource = resource;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);

        convertView = inflater.inflate(mResource, parent, false );
        title = getItem(position).getTitle();
        description = getItem(position).getMessage();
        TextView titleTextView = (TextView) convertView.findViewById(R.id.milestoneTitle);
        TextView descriptionTextView = (TextView) convertView.findViewById(R.id.milestoneDescription);

        MileStone mileStone = getItem(position);
        if (mileStone != null) {
            if (titleTextView != null) {
                titleTextView.setText(title);
            }
            if (descriptionTextView != null) {
                descriptionTextView.setText(description);
            }
        }
        String eventID = mileStone.getEventID();
        String userID = mileStone.getUserID();
        String attendees = mileStone.getAttendeeCount();

        return convertView;
        }
}

