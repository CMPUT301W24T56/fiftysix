package com.example.fiftysix;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PromotionQRCode extends AppCompatActivity {
    private Button scan_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.attendee_qr_code);

         scan_code = findViewById(R.id.attendee_qr_code);
         scan_code.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {

             }
       });


    }
}
