// Generated by view binder compiler. Do not edit!
package com.example.fiftysix.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.fiftysix.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class AttendeeRowBinding implements ViewBinding {
  @NonNull
  private final CardView rootView;

  @NonNull
  public final TextView apiLevel;

  @NonNull
  public final TextView attendeeCount;

  @NonNull
  public final TextView codeName;

  @NonNull
  public final TextView description;

  @NonNull
  public final TextView descriptionEvent;

  @NonNull
  public final RelativeLayout expandableLayout;

  @NonNull
  public final LinearLayout linearLayout;

  @NonNull
  public final AppCompatButton uploadQRFromScan;

  @NonNull
  public final TextView version;

  private AttendeeRowBinding(@NonNull CardView rootView, @NonNull TextView apiLevel,
      @NonNull TextView attendeeCount, @NonNull TextView codeName, @NonNull TextView description,
      @NonNull TextView descriptionEvent, @NonNull RelativeLayout expandableLayout,
      @NonNull LinearLayout linearLayout, @NonNull AppCompatButton uploadQRFromScan,
      @NonNull TextView version) {
    this.rootView = rootView;
    this.apiLevel = apiLevel;
    this.attendeeCount = attendeeCount;
    this.codeName = codeName;
    this.description = description;
    this.descriptionEvent = descriptionEvent;
    this.expandableLayout = expandableLayout;
    this.linearLayout = linearLayout;
    this.uploadQRFromScan = uploadQRFromScan;
    this.version = version;
  }

  @Override
  @NonNull
  public CardView getRoot() {
    return rootView;
  }

  @NonNull
  public static AttendeeRowBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static AttendeeRowBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.attendee_row, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static AttendeeRowBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.apiLevel;
      TextView apiLevel = ViewBindings.findChildViewById(rootView, id);
      if (apiLevel == null) {
        break missingId;
      }

      id = R.id.attendeeCount;
      TextView attendeeCount = ViewBindings.findChildViewById(rootView, id);
      if (attendeeCount == null) {
        break missingId;
      }

      id = R.id.code_name;
      TextView codeName = ViewBindings.findChildViewById(rootView, id);
      if (codeName == null) {
        break missingId;
      }

      id = R.id.description;
      TextView description = ViewBindings.findChildViewById(rootView, id);
      if (description == null) {
        break missingId;
      }

      id = R.id.descriptionEvent;
      TextView descriptionEvent = ViewBindings.findChildViewById(rootView, id);
      if (descriptionEvent == null) {
        break missingId;
      }

      id = R.id.expandable_layout;
      RelativeLayout expandableLayout = ViewBindings.findChildViewById(rootView, id);
      if (expandableLayout == null) {
        break missingId;
      }

      id = R.id.linear_layout;
      LinearLayout linearLayout = ViewBindings.findChildViewById(rootView, id);
      if (linearLayout == null) {
        break missingId;
      }

      id = R.id.uploadQRFromScan;
      AppCompatButton uploadQRFromScan = ViewBindings.findChildViewById(rootView, id);
      if (uploadQRFromScan == null) {
        break missingId;
      }

      id = R.id.version;
      TextView version = ViewBindings.findChildViewById(rootView, id);
      if (version == null) {
        break missingId;
      }

      return new AttendeeRowBinding((CardView) rootView, apiLevel, attendeeCount, codeName,
          description, descriptionEvent, expandableLayout, linearLayout, uploadQRFromScan, version);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
