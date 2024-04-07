package com.example.fiftysix;

import android.content.Context;
import android.view.*;
import android.widget.*;

import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(Context ctx, List<Event> ls) {
        super(ctx, 0, ls);
    }
    public View getView(int i, View v, ViewGroup par) {
        if (v == null)
            v = LayoutInflater.from(getContext()).inflate(R.layout.admin_evt, par, false);
        Event e = getItem(i);
        ((TextView) v.findViewById(R.id.admin_evt_name)).setText(e.getEventName());
        return v;
    }
}

/*
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventVH> {
    private List<Event> eventList;
    private boolean isExpandableOnClick;

    public EventAdapter(ArrayList<Event> eventList, boolean isExpandableOnClick) {
        this.eventList = eventList;
        this.isExpandableOnClick = isExpandableOnClick;
    }

    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_row, parent, false);
        return new EventVH(view, isExpandableOnClick);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {

        Event event = eventList.get(position);
        holder.codeName.setText(event.getEventName());
        holder.versionTxt.setText(event.getLocation());
        holder.apiLevelTxt.setText(event.getDate());
        holder.descriptionTxt.setText(event.getDetails());

        String imageUrl = event.getPosterURL();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .fit()
                    .into(holder.eventImage);
        }

        if (!isExpandableOnClick) {
            holder.expandableLayout.setVisibility(View.GONE);
        } else {
            boolean isExpandable = eventList.get(position).getExpandable();
            holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventVH extends RecyclerView.ViewHolder {

        TextView codeName, versionTxt, apiLevelTxt, descriptionTxt;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;
        ImageView eventImage;

        public EventVH(@NonNull View itemView, boolean isExpandableOnClick) {
            super(itemView);

            codeName = itemView.findViewById(R.id.eventName);
            versionTxt = itemView.findViewById(R.id.startDate);
            apiLevelTxt = itemView.findViewById(R.id.locationOfEvent);
            descriptionTxt = itemView.findViewById(R.id.description);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);

            eventImage = itemView.findViewById(R.id.event_poster_image);

            if (isExpandableOnClick) {
                linearLayout.setOnClickListener(v -> {
                    Event event = eventList.get(getAdapterPosition());
                    event.setExpandable(!event.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                });
            }
        }
    }
}
*/
