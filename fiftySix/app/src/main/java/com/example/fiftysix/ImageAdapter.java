package com.example.fiftysix;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    List<AdminImage> imageList;
    Context mContext;

    public ImageAdapter(List<AdminImage> imageList, Context mContext) {
        this.imageList = imageList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        AdminImage posterImage = imageList.get(position);
        Picasso.get().load(posterImage.getImageLink()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();


    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AdminImage image = imageList.get(getAdapterPosition());


                    new AlertDialog.Builder(mContext)
                            .setTitle("Delete Image")
                            .setMessage("Are you sure you want to delete this image?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    // Is a poster image
                                    if (image.getDefaultLink().equals("https://firebasestorage.googleapis.com/v0/b/fiftysix-a4bcf.appspot.com/o/images%2FDoNotDeleteStockProfilePic%2Fno-photos.png?alt=media&token=52497ae1-5e13-49cb-a43b-379f85849c73")) {
                                        FirebaseFirestore.getInstance().collection("PosterImages").document(image.getPosterID()).update("image", image.getDefaultLink());
                                        notifyItemChanged(getAdapterPosition());
                                    } else {
                                        FirebaseFirestore.getInstance().collection("Users").document(image.getPosterID()).update("profileImageURL", image.getDefaultLink());
                                        FirebaseFirestore.getInstance().collection("Profiles").document(image.getPosterID()).update("profileImageURL", image.getDefaultLink());
                                        notifyItemChanged(getAdapterPosition());
                                    }

                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).show();
                }
            });

        }
    }

}
