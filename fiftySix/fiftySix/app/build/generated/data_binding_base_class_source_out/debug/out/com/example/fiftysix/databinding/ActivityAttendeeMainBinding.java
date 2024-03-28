// Generated by view binder compiler. Do not edit!
package com.example.fiftysix.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.fiftysix.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityAttendeeMainBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageButton attendeeProfile;

  @NonNull
  public final ImageButton buttonAttendeeHome;

  @NonNull
  public final ImageButton notificationButton;

  @NonNull
  public final ImageButton qrCodeButton;

  @NonNull
  public final SearchView searchView;

  @NonNull
  public final ListView view;

  private ActivityAttendeeMainBinding(@NonNull ConstraintLayout rootView,
      @NonNull ImageButton attendeeProfile, @NonNull ImageButton buttonAttendeeHome,
      @NonNull ImageButton notificationButton, @NonNull ImageButton qrCodeButton,
      @NonNull SearchView searchView, @NonNull ListView view) {
    this.rootView = rootView;
    this.attendeeProfile = attendeeProfile;
    this.buttonAttendeeHome = buttonAttendeeHome;
    this.notificationButton = notificationButton;
    this.qrCodeButton = qrCodeButton;
    this.searchView = searchView;
    this.view = view;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityAttendeeMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityAttendeeMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_attendee_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityAttendeeMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.attendee_profile;
      ImageButton attendeeProfile = ViewBindings.findChildViewById(rootView, id);
      if (attendeeProfile == null) {
        break missingId;
      }

      id = R.id.button_attendee_home;
      ImageButton buttonAttendeeHome = ViewBindings.findChildViewById(rootView, id);
      if (buttonAttendeeHome == null) {
        break missingId;
      }

      id = R.id.notification_button;
      ImageButton notificationButton = ViewBindings.findChildViewById(rootView, id);
      if (notificationButton == null) {
        break missingId;
      }

      id = R.id.qr_code_button;
      ImageButton qrCodeButton = ViewBindings.findChildViewById(rootView, id);
      if (qrCodeButton == null) {
        break missingId;
      }

      id = R.id.searchView;
      SearchView searchView = ViewBindings.findChildViewById(rootView, id);
      if (searchView == null) {
        break missingId;
      }

      id = R.id.view;
      ListView view = ViewBindings.findChildViewById(rootView, id);
      if (view == null) {
        break missingId;
      }

      return new ActivityAttendeeMainBinding((ConstraintLayout) rootView, attendeeProfile,
          buttonAttendeeHome, notificationButton, qrCodeButton, searchView, view);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
