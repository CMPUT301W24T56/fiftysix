package com.example.fiftysix;

import android.content.Context;
import android.view.*;
import android.widget.*;

import java.util.List;
/**
 * Adapter used by AdminBrowseEvents
 * @author Bruce.
 * @version 1
 * @since SDK34
 */
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

