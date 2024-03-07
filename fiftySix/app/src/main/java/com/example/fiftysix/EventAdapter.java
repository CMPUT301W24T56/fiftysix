package com.example.fiftysix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventVH> {

    List<Event> eventList;

    public EventAdapter(ArrayList<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_row, parent, false);
        return new EventVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventVH holder, int position) {

        Event event = eventList.get(position);
        holder.codeName.setText(event.getEventName());
        holder.versionTxt.setText(event.getLocation());
        holder.apiLevelTxt.setText(event.getDate());
        holder.descriptionTxt.setText(event.getDetails());

        boolean isExpandable = eventList.get(position).getExpandable();
        holder.expandableLayout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);

    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public class EventVH extends RecyclerView.ViewHolder {

        TextView codeName, versionTxt, apiLevelTxt, descriptionTxt;
        LinearLayout linearLayout;
        RelativeLayout expandableLayout;

        public EventVH(@NonNull View itemView) {
            super(itemView);

            codeName = itemView.findViewById(R.id.code_name);
            versionTxt = itemView.findViewById(R.id.version);
            apiLevelTxt = itemView.findViewById(R.id.apiLevel);
            descriptionTxt = itemView.findViewById(R.id.description);

            linearLayout = itemView.findViewById(R.id.linear_layout);
            expandableLayout = itemView.findViewById(R.id.expandable_layout);


            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Event event = eventList.get(getAdapterPosition());
                    event.setExpandable(!event.getExpandable());
                    notifyItemChanged(getAdapterPosition());
                }
            });


        }
    }

}