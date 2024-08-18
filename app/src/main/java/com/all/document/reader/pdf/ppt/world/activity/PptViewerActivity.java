package com.all.document.reader.pdf.ppt.world.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.all.document.reader.pdf.ppt.world.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.itsrts.pptviewer.PPTViewer;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;

import java.io.File;

public class PptViewerActivity extends AppCompatActivity {

    PPTViewer pptViewer;
    String path = null;

    AdView adView;
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ppt_viewer);

        try {
            BannerAD();
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial));
            reqNewInterstitial();


            pptViewer = (PPTViewer) findViewById(R.id.pptviewer);

            if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
                Uri uri = getIntent().getData();
                getFileName(uri);

                readPptFile(new File(uri.getPath()));
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    getSupportActionBar().hide();
                }
            }, 2000);   //5 seconds
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void readPptFile(File file) {
        try {
            path = file.getPath();
            pptViewer.setNext_img(android.R.drawable.ic_media_next)
                    .setPrev_img(android.R.drawable.ic_media_previous)
                    .setSettings_img(android.R.drawable.ic_menu_info_details)
                    .setZoomin_img(android.R.drawable.ic_menu_zoom)
                    .setZoomout_img(android.R.drawable.ic_menu_zoom)
                    .loadPPT(this, path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getFileName(Uri uri) {
        File file = new File(uri.getPath());

        String title = file.getName().replaceAll("%20", " ");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {

        new FancyGifDialog.Builder(this)
                .setTitle("Close")
                .setMessage("Are you sure to close the file?")
                .setNegativeBtnText("Cancel")
                .setPositiveBtnBackground("#1659ef")
                .setPositiveBtnText("Close")
                .setNegativeBtnBackground("#FFA9A7A8")
                .setGifResource(R.drawable.header)   //Pass your Gif here
                .isCancellable(true)
                .OnPositiveClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        finish();
                    }
                })
                .OnNegativeClicked(new FancyGifDialogListener() {
                    @Override
                    public void OnClick() {
                        getSupportActionBar().hide();
                    }
                })
                .build();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public void BannerAD() {
        adView = findViewById(R.id.banner_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        adView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                adView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int error) {
                adView.setVisibility(View.GONE);
            }

        });
    }

    public void reqNewInterstitial() {
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void showInterstitialAd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        }
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                reqNewInterstitial();
            }
        });
    }

}
