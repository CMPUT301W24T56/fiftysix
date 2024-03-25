// Generated by view binder compiler. Do not edit!
package com.example.fiftysix.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.fiftysix.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class OrganizerHomePageCustomListViewBinding implements ViewBinding {
  @NonNull
  private final LinearLayoutCompat rootView;

  @NonNull
  public final TextView orgListTextEventName;

  @NonNull
  public final TextView orgListTextViewDate;

  @NonNull
  public final TextView orgListTextViewLocation;

  private OrganizerHomePageCustomListViewBinding(@NonNull LinearLayoutCompat rootView,
      @NonNull TextView orgListTextEventName, @NonNull TextView orgListTextViewDate,
      @NonNull TextView orgListTextViewLocation) {
    this.rootView = rootView;
    this.orgListTextEventName = orgListTextEventName;
    this.orgListTextViewDate = orgListTextViewDate;
    this.orgListTextViewLocation = orgListTextViewLocation;
  }

  @Override
  @NonNull
  public LinearLayoutCompat getRoot() {
    return rootView;
  }

  @NonNull
  public static OrganizerHomePageCustomListViewBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static OrganizerHomePageCustomListViewBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.organizer_home_page_custom_list_view, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static OrganizerHomePageCustomListViewBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.orgListTextEventName;
      TextView orgListTextEventName = ViewBindings.findChildViewById(rootView, id);
      if (orgListTextEventName == null) {
        break missingId;
      }

      id = R.id.orgListTextViewDate;
      TextView orgListTextViewDate = ViewBindings.findChildViewById(rootView, id);
      if (orgListTextViewDate == null) {
        break missingId;
      }

      id = R.id.orgListTextViewLocation;
      TextView orgListTextViewLocation = ViewBindings.findChildViewById(rootView, id);
      if (orgListTextViewLocation == null) {
        break missingId;
      }

      return new OrganizerHomePageCustomListViewBinding((LinearLayoutCompat) rootView,
          orgListTextEventName, orgListTextViewDate, orgListTextViewLocation);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}