package com.all.document.reader.pdf.ppt.world.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.utils.NumberPickerDialog;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;

import java.io.File;

public class PdfViewerActivity extends AppCompatActivity implements OnPageChangeListener,
        NumberPicker.OnValueChangeListener {

    public PDFView pdfView;
    private TextView loading;
    private ProgressBar progressBar;
    private Menu menu;

    public File file;

    private String pages;
    private boolean isHorizontalView = false;

    private NumberPickerDialog numberPickerDialog;

    public SharedPreferences preferencesPdf;

    AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        try {
            BannerAD();

            pdfView = (PDFView) findViewById(R.id.pdfView);
            loading = (TextView) findViewById(R.id.loading);
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);

            numberPickerDialog = new NumberPickerDialog();
            numberPickerDialog.setValueChangeListener(PdfViewerActivity.this);

            preferencesPdf = getSharedPreferences("pdf", MODE_PRIVATE);

            DocAsyncTask asyncTask = new DocAsyncTask();
            asyncTask.execute();
        } catch (Exception e){
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
                pdfView.setVisibility(View.GONE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {

            try {
                if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
                    Uri uri = getIntent().getData();
                    getFileName(uri);
                    readPdf(uri);
                    file = new File(uri.getPath());
                } else {
                    finish();
                }
            } catch (Exception e){
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
                pdfView.setVisibility(View.VISIBLE);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void readPdf(Uri path) {
        try {
            pdfView.fromUri(path)
                    .onPageChange(this)
                    .pageSnap(true)
                    .pageFling(true)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load();
        } catch (Exception e){
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
        getMenuInflater().inflate(R.menu.menu_sub, menu);
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
                    numberPickerDialog.setMaxValue(pdfView.getPageCount());
                    numberPickerDialog.show(getSupportFragmentManager(), "goto page number picker");
                }
                break;

            case R.id.action_controls:
                if (pages != null) {
                    showControlsDialog();
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
        pdfView.jumpTo(picker.getValue() - 1, true);
    }

    private void showControlsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_controls);

        RelativeLayout rlTop = (RelativeLayout) dialog.findViewById(R.id.rl_top);
        rlTop.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        final Switch switchNightMode = (Switch) dialog.findViewById(R.id.switch_night_mode);
        RadioGroup rgViewMode = (RadioGroup) dialog.findViewById(R.id.rg_view_mode);
        RadioGroup rgScrollingMode = (RadioGroup) dialog.findViewById(R.id.rg_scrolling_mode);
        final RadioButton rbScrollView = (RadioButton) dialog.findViewById(R.id.rb_scroll_view);
        final RadioButton rbBookView = (RadioButton) dialog.findViewById(R.id.rb_book_view);
        final RadioButton rbVerticalView = (RadioButton) dialog.findViewById(R.id.rb_vertical_view);
        final RadioButton rbHorizontalView = (RadioButton) dialog.findViewById(R.id.rb_horizontal_view);

        Button btnOkPdf = (Button) dialog.findViewById(R.id.btn_pdf_ok);
        btnOkPdf.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        switchNightMode.setChecked(preferencesPdf.getBoolean("pdf_night_mode", false));
        rgViewMode.check(preferencesPdf.getInt("pdf_view_mode", R.id.rb_scroll_view));
        rgScrollingMode.check(preferencesPdf.getInt("pdf_scrolling_mode", R.id.rb_vertical_view));

        btnOkPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchNightMode.isChecked()) {
                    preferencesPdf.edit().putBoolean("pdf_night_mode", true).apply();
                } else {
                    preferencesPdf.edit().putBoolean("pdf_night_mode", false).apply();
                }

                if (rbScrollView.isChecked()) {
                    preferencesPdf.edit().putInt("pdf_view_mode", R.id.rb_scroll_view).apply();
                } else if (rbBookView.isChecked()) {
                    preferencesPdf.edit().putInt("pdf_view_mode", R.id.rb_book_view).apply();
                }

                if (rbVerticalView.isChecked()) {
                    preferencesPdf.edit().putInt("pdf_scrolling_mode", R.id.rb_vertical_view).apply();
                } else if (rbHorizontalView.isChecked()) {
                    preferencesPdf.edit().putInt("pdf_scrolling_mode", R.id.rb_horizontal_view).apply();
                }
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (preferencesPdf.getBoolean("pdf_night_mode", false)) {
                    pdfView.setNightMode(true);
                } else {
                    pdfView.setNightMode(false);
                }

                if (preferencesPdf.getInt("pdf_view_mode", R.id.rb_scroll_view) == R.id.rb_book_view) {
                    pdfView.setPageSnap(true);
                } else {
                    pdfView.setPageSnap(false);
                }

                if (preferencesPdf.getInt("pdf_scrolling_mode", R.id.rb_vertical_view) == R.id.rb_horizontal_view) {
                    isHorizontalView = true;
                    readPdfFileInHorizontalView(file, pdfView.getCurrentPage());
                } else {
                    isHorizontalView = false;
                    readPdfFileInHorizontalView(file, pdfView.getCurrentPage());
                }
            }
        });

        dialog.show();
    }

    private void readPdfFileInHorizontalView(File file, int currentPage) {
        if (isHorizontalView) {
            pdfView.fromFile(file)
                    .defaultPage(currentPage)
                    .swipeHorizontal(true)
                    .onPageChange(this)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load();

            if (preferencesPdf.getBoolean("pdf_night_mode", false)) {
                pdfView.setNightMode(true);
            } else {
                pdfView.setNightMode(false);
            }

            if (preferencesPdf.getInt("pdf_view_mode", R.id.rb_scroll_view) == R.id.rb_book_view) {
                pdfView.setPageSnap(true);
            } else {
                pdfView.setPageSnap(false);
            }
        } else {
            pdfView.fromFile(file)
                    .defaultPage(currentPage)
                    .onPageChange(this)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load();

            if (preferencesPdf.getBoolean("pdf_night_mode", false)) {
                pdfView.setNightMode(true);
            } else {
                pdfView.setNightMode(false);
            }

            if (preferencesPdf.getInt("pdf_view_mode", R.id.rb_scroll_view) == R.id.rb_book_view) {
                pdfView.setPageSnap(true);
            } else {
                pdfView.setPageSnap(false);
            }
        }
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
