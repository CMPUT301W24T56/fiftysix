package com.example.fiftysix;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
/**
 * Gives the admin the option to browse proster or profile images, launches ImageDisplayActivity with a different putExtra depending if the admin selects poster or profile images.
 *
 * @author Rakshit
 * @version 1
 * @since SDK34
 */
public class AdminBrowseImages extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_images);

        Button btnProfileImages = findViewById(R.id.btnProfileImages);
        Button btnPosterImages = findViewById(R.id.btnPosterImages);
        ImageButton backButton = findViewById(R.id.backButton);

        btnProfileImages.setOnClickListener(v -> {
            navigateToImageDisplay("ProfileImages");
        });

        btnPosterImages.setOnClickListener(v -> {
            navigateToImageDisplay("PosterImages");
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * launches ImageDisplayActivity with a different putExtra depending if the admin selects poster or profile images.
     * Resulting in either displaying only profile images or poster images.
     *
     * @param imageType String, can either be "ProfileImages" or "PosterImages" depending on what you'd like to view in ImageDisplayActivity()
     */
    private void navigateToImageDisplay(String imageType) {
        Intent intent = new Intent(AdminBrowseImages.this, ImageDisplayActivity.class);
        intent.putExtra("ImageType", imageType);
        startActivity(intent);
    }
}