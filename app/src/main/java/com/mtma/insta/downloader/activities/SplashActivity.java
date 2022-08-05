package com.mtma.insta.downloader.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mtma.insta.downloader.R;


public class SplashActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        View topShadow = findViewById(R.id.activity_splash_top_shadow_rectangle);
        View bottomShadow = findViewById(R.id.activity_splash_bottom_shadow_rectangle);

        LinearLayout appNameLayout = findViewById(R.id.activity_splash_app_name_layout_background);
        TextView hintText = findViewById(R.id.activity_splash_hint_text);

        TextView developerLogText = findViewById(R.id.activity_splash_developer_logo_text);
        ImageView developerLogoImage = findViewById(R.id.activity_splash_developer_logo_image);



        Animation animationFromLeftToRight = AnimationUtils.loadAnimation(this, R.anim.animation_from_left_to_right);
        Animation animationFromRightToLeft = AnimationUtils.loadAnimation(this, R.anim.animation_from_right_to_left);




        appNameLayout.setAnimation(animationFromLeftToRight);
        hintText.setAnimation(animationFromLeftToRight);

        developerLogText.startAnimation(animationFromRightToLeft);
        developerLogoImage.startAnimation(animationFromRightToLeft);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {


                appNameLayout.clearAnimation();
                hintText.clearAnimation();
                developerLogText.clearAnimation();
                developerLogoImage.clearAnimation();



                topShadow.setVisibility(View.VISIBLE);
                bottomShadow.setVisibility(View.VISIBLE);

                animationFromLeftToRight.setDuration(800);
                animationFromRightToLeft.setDuration(800);

                topShadow.startAnimation(animationFromLeftToRight);
                bottomShadow.startAnimation(animationFromRightToLeft);


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    }
                }, 1500);


            }
        }, 1000);



    }



}








