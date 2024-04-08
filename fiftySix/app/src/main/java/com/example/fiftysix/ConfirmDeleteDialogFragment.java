package com.example.fiftysix;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;

import androidx.fragment.app.DialogFragment;

public class ConfirmDeleteDialogFragment extends DialogFragment {

    public interface ConfirmDeleteListener {
        void onConfirmDelete(Profile profile);
    }

    private ConfirmDeleteListener listener;
    private Profile profile;

    // Factory method to create a new instance of the dialog, passing the profile
    public static ConfirmDeleteDialogFragment newInstance(Profile profile) {
        ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
        fragment.profile = profile;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Do you want to delete this profile?")
                .setPositiveButton("Delete", (dialog, id) -> {
                    if (listener != null) {
                        listener.onConfirmDelete(profile);
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });
        return builder.create();
    }

    public void setConfirmDeleteListener(ConfirmDeleteListener listener) {
        this.listener = listener;
    }
}
