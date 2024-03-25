// Generated by view binder compiler. Do not edit!
package com.example.fiftysix.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.fiftysix.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class UserSelectionViewBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final AppCompatButton buttonAdmin;

  @NonNull
  public final AppCompatButton buttonAttendee;

  @NonNull
  public final AppCompatButton buttonOrganizer;

  @NonNull
  public final TextView textViewBio;

  @NonNull
  public final TextView textViewTitle;

  @NonNull
  public final ConstraintLayout userSelection;

  private UserSelectionViewBinding(@NonNull ConstraintLayout rootView,
      @NonNull AppCompatButton buttonAdmin, @NonNull AppCompatButton buttonAttendee,
      @NonNull AppCompatButton buttonOrganizer, @NonNull TextView textViewBio,
      @NonNull TextView textViewTitle, @NonNull ConstraintLayout userSelection) {
    this.rootView = rootView;
    this.buttonAdmin = buttonAdmin;
    this.buttonAttendee = buttonAttendee;
    this.buttonOrganizer = buttonOrganizer;
    this.textViewBio = textViewBio;
    this.textViewTitle = textViewTitle;
    this.userSelection = userSelection;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static UserSelectionViewBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static UserSelectionViewBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.user_selection_view, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static UserSelectionViewBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.buttonAdmin;
      AppCompatButton buttonAdmin = ViewBindings.findChildViewById(rootView, id);
      if (buttonAdmin == null) {
        break missingId;
      }

      id = R.id.buttonAttendee;
      AppCompatButton buttonAttendee = ViewBindings.findChildViewById(rootView, id);
      if (buttonAttendee == null) {
        break missingId;
      }

      id = R.id.buttonOrganizer;
      AppCompatButton buttonOrganizer = ViewBindings.findChildViewById(rootView, id);
      if (buttonOrganizer == null) {
        break missingId;
      }

      id = R.id.textViewBio;
      TextView textViewBio = ViewBindings.findChildViewById(rootView, id);
      if (textViewBio == null) {
        break missingId;
      }

      id = R.id.textViewTitle;
      TextView textViewTitle = ViewBindings.findChildViewById(rootView, id);
      if (textViewTitle == null) {
        break missingId;
      }

      ConstraintLayout userSelection = (ConstraintLayout) rootView;

      return new UserSelectionViewBinding((ConstraintLayout) rootView, buttonAdmin, buttonAttendee,
          buttonOrganizer, textViewBio, textViewTitle, userSelection);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
