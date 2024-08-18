package com.all.document.reader.pdf.ppt.world.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.all.document.reader.pdf.ppt.world.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class TxtViewerActivity extends AppCompatActivity {

    private TextView tvText;
    private TextView loading;
    private ProgressBar progressBar;

    private int txtSize = 1;

    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_txt_viewer);

        try {
            BannerAD();

            tvText = (TextView) findViewById(R.id.tv_text);
            loading = (TextView) findViewById(R.id.loading);
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);

            TxtAsyncTask asyncTask = new TxtAsyncTask();
            asyncTask.execute();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public class TxtAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                loading.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                tvText.setVisibility(View.GONE);
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
                    Uri uri = getIntent().getData();
                    getFileName(uri);
                    readTxtFile(new File(uri.getPath()));
                } else {
                    finish();
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            return "txt";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                loading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                tvText.setVisibility(View.VISIBLE);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void readTxtFile(File file) {
        try {
            FileInputStream is = new FileInputStream(file);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            tvText.setText(new String(buffer));
        } catch (IOException e) {
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
        if (getSupportActionBar().isShowing()) {
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
                        }
                    })
                    .build();
        } else {
            getSupportActionBar().show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_txt, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_expand:
                getSupportActionBar().hide();
                break;
            case R.id.action_rendering_text:

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    Toast.makeText(this, "Not Supported on this device", Toast.LENGTH_SHORT).show();
                } else {

                    switch (txtSize) {
                        case 0:
                            tvText.setTextAppearance(android.R.style.TextAppearance_Medium);
                            txtSize = 1;
                            break;

                        case 1:
                            tvText.setTextAppearance(android.R.style.TextAppearance_Large);
                            txtSize = 2;
                            break;

                        case 2:
                            tvText.setTextAppearance(android.R.style.TextAppearance_Small);
                            txtSize = 0;
                            break;
                    }
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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
}
