package com.smacon.fish2marine;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class SplashActivity extends Activity implements Animation.AnimationListener {
    Animation animFadeIn;
    LinearLayout linearLayout;
    ImageView logo;
    SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "prefs";
    private static final String KEY_REMEMBER = "remember";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.animation_fade_in);
        animFadeIn.setAnimationListener(this);
        logo = (ImageView) findViewById(R.id.logo);
        logo.startAnimation(animFadeIn);
    }
    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    @Override
    public void onAnimationStart(Animation animation) {
        //under Implementation
    }

    public void onAnimationEnd(Animation animation) {
        if(sharedPreferences.getBoolean(KEY_REMEMBER, false))
        {
            // Start Main Screen
            Intent i = new Intent(SplashActivity.this, NavigationDrawerActivity.class);
            startActivity(i);
            this.finish();
        }
        else
        {
            // Start Main Screen
            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(i);
            this.finish();
        }
    }
    @Override
    public void onAnimationRepeat(Animation animation) {
        //under Implementation
    }

}