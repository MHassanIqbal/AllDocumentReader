package com.all.document.reader.pdf.ppt.world.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.all.document.reader.pdf.ppt.world.R;

public class PrivacyActivity extends AppCompatActivity {

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences("policy", MODE_PRIVATE);

        if (!preferences.getBoolean("accept", false)) {
            setContentView(R.layout.activity_privacy);
        } else {
            startActivity(new Intent(PrivacyActivity.this, MainActivity.class));
            finish();
        }
    }

    public void onDeclineClick(View view) {
        finish();
    }

    public void onAcceptClick(View view) {
        preferences.edit().putBoolean("accept", true).apply();
        startActivity(new Intent(PrivacyActivity.this, MainActivity.class));
        finish();
    }
}
