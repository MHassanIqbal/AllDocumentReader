package com.all.document.reader.pdf.ppt.world.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.utils.XlsSheetView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import jxl.read.biff.BiffException;

public class XlsViewerActivity extends AppCompatActivity {

    private TabHost tabHost;
    private TextView loading;
    private ProgressBar progressBar;

    Activity activity = new Activity();

    AdView adView;
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xls_viewer);

        try {
            BannerAD();
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial));
            reqNewInterstitial();

            tabHost = (TabHost) findViewById(R.id.sheets);
            loading = (TextView) findViewById(R.id.loading);
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);

            XlsAsyncTask asyncTask = new XlsAsyncTask();
            asyncTask.execute();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public class XlsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                loading.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                tabHost.setVisibility(View.GONE);
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
                    File file = new File(uri.getPath());
                    if (file.getName().endsWith(".xls")) {
                        readXlsFile(file);
                    } else {
                        readXlsxFile(file);
                    }

                } else {
                    finish();
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            return "xls";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                loading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                tabHost.setVisibility(View.VISIBLE);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void getFileName(Uri uri) {
        File file = new File(uri.getPath());

        String title = file.getName().replaceAll("%20", " ");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    private void readXlsFile(File file) {
        try {

            FileInputStream inputStream = new FileInputStream(file);

            jxl.Workbook xl = jxl.Workbook.getWorkbook(inputStream);
            tabHost.setup();
            for (final jxl.Sheet sheet : xl.getSheets()) {
                TabHost.TabSpec tabSpec = tabHost.newTabSpec(sheet.getName());
                tabSpec.setContent(new TabHost.TabContentFactory() {
                    @Override
                    public View createTabContent(String s) {
                        final XlsSheetView view = new XlsSheetView(XlsViewerActivity.this);
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                view.setSheet(sheet);
                            }
                        });
                        return view;
                    }
                });
                tabSpec.setIndicator("    " + sheet.getName() + "    ");
                tabHost.addTab(tabSpec);
            }

        } catch (IOException | BiffException e) {
            e.printStackTrace();
        }
    }

    private void readXlsxFile(File file) {
        try {

            FileInputStream inputStream = new FileInputStream(file);

            XSSFWorkbook wbIn = new XSSFWorkbook(inputStream);

            Workbook wbOut = new HSSFWorkbook();

            int sheetCnt = wbIn.getNumberOfSheets();
            for (int i = 0; i < sheetCnt; i++) {
                Sheet sIn = wbIn.getSheetAt(0);
                Sheet sOut = wbOut.createSheet(sIn.getSheetName());
                Iterator<Row> rowIt = sIn.rowIterator();
                while (rowIt.hasNext()) {
                    Row rowIn = rowIt.next();
                    Row rowOut = sOut.createRow(rowIn.getRowNum());

                    Iterator<Cell> cellIt = rowIn.cellIterator();
                    while (cellIt.hasNext()) {
                        Cell cellIn = cellIt.next();
                        Cell cellOut = rowOut.createCell(cellIn.getColumnIndex(), cellIn.getCellType());

                        switch (cellIn.getCellType()) {
                            case Cell.CELL_TYPE_BLANK:
                                break;

                            case Cell.CELL_TYPE_BOOLEAN:
                                cellOut.setCellValue(cellIn.getBooleanCellValue());
                                break;

                            case Cell.CELL_TYPE_ERROR:
                                cellOut.setCellValue(cellIn.getErrorCellValue());
                                break;

                            case Cell.CELL_TYPE_FORMULA:
                                cellOut.setCellFormula(cellIn.getCellFormula());
                                break;

                            case Cell.CELL_TYPE_NUMERIC:
                                cellOut.setCellValue(cellIn.getNumericCellValue());
                                break;

                            case Cell.CELL_TYPE_STRING:
                                cellOut.setCellValue(cellIn.getStringCellValue());
                                break;
                        }

                        {
                            CellStyle styleIn = cellIn.getCellStyle();
                            CellStyle styleOut = cellOut.getCellStyle();
                            styleOut.setDataFormat(styleIn.getDataFormat());
                        }
                        cellOut.setCellComment(cellIn.getCellComment());
                    }
                }
            }

            File outputDir = getApplicationContext().getCacheDir();
            File outputFile = File.createTempFile("myTempXlsFile", ".xls", outputDir);

            OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
            wbOut.write(out);
            out.close();

            readXlsFile(outputFile);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        }
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
        getMenuInflater().inflate(R.menu.menu_xls, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.action_expand:
                getSupportActionBar().hide();
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
    public void reqNewInterstitial() {
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

}
