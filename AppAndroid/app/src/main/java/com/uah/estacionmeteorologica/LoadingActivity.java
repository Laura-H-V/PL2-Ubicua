package com.uah.estacionmeteorologica;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;


public class LoadingActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        ImageView imgLogo = findViewById(R.id.imgLogo);
        ProgressBar progress = findViewById(R.id.progressCircular);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            imgLogo.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction(() -> {
                        startActivity(new Intent(LoadingActivity.this, MainMenuActivity.class));
                        finish();
                    })
                    .start();

            progress.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .start();

        }, SPLASH_DURATION);
    }
}
