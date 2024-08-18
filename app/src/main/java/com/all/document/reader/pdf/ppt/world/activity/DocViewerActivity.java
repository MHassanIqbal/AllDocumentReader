package com.all.document.reader.pdf.ppt.world.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.utils.NumberPickerDialog;
import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;

import java.io.File;

public class DocViewerActivity extends AppCompatActivity implements OnPageChangeListener,
        NumberPicker.OnValueChangeListener {

    public PDFView pdfViewDoc;
    private TextView loading;
    private ProgressBar progressBar;
    private Menu menu;

    public File file;
    public ProgressDialog progressDialog;

    private String pages;

    private NumberPickerDialog numberPickerDialog;

    public SharedPreferences preferencesPdf;

    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_viewer);

        try {
            BannerAD();

            pdfViewDoc = (PDFView) findViewById(R.id.doc_pdfView);
            loading = (TextView) findViewById(R.id.loading);
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);

            numberPickerDialog = new NumberPickerDialog();
            numberPickerDialog.setValueChangeListener(DocViewerActivity.this);

            preferencesPdf = getSharedPreferences("doc", MODE_PRIVATE);

            File dir = new File(Environment.getExternalStorageDirectory() + "/All Document Reader/");
            if (!dir.exists()) {
                dir.mkdir();
            }
            progressDialog = new ProgressDialog(this);


            DocAsyncTask asyncTask = new DocAsyncTask();
            asyncTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class DocAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                loading.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                pdfViewDoc.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
                    Uri uri = getIntent().getData();
                    getFileName(uri);

                    file = new File(uri.getPath());
                    readDocFile(file);
                } else {
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "doc";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                loading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                pdfViewDoc.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void readDocFile(File file) {
        try {
            Document document = new Document(String.valueOf(file));

            File outputDir = getApplicationContext().getCacheDir();
            File outputFile = File.createTempFile("myTempDocFile", ".doc", outputDir);

            document.save(String.valueOf(outputFile), SaveFormat.PDF);

            pdfViewDoc.fromFile(outputFile)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .onPageChange(this)
                    .load();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_doc, menu);
        super.onCreateOptionsMenu(menu);
        this.menu = menu;
        return true;
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
            case R.id.action_pages:
                if (pages != null) {
                    numberPickerDialog.setMaxValue(pdfViewDoc.getPageCount());
                    numberPickerDialog.show(getSupportFragmentManager(), "goto page number picker");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.findItem(R.id.action_pages);
        if (pages != null) {
            menuItem.setTitle(pages);
        }
        return true;
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pages = String.valueOf(page + 1) + "/" + String.valueOf(pageCount);
        onPrepareOptionsMenu(menu);
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
                            preferencesPdf.edit().clear().apply();
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
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        numberPickerDialog.getValueChangeListener();
        pdfViewDoc.jumpTo(picker.getValue() - 1, true);
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
