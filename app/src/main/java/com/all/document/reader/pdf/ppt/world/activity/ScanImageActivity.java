package com.all.document.reader.pdf.ppt.world.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.adapter.ImageRecyclerAdapter;
import com.all.document.reader.pdf.ppt.world.helper.GridSpacingItemDecoration;
import com.all.document.reader.pdf.ppt.world.scan.ScanActivity;
import com.all.document.reader.pdf.ppt.world.utils.EmptyRecyclerView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class ScanImageActivity extends AppCompatActivity {

    public static final int RequestCameraPermissionCode = 3;
    public static final int RequestStoragePermissionCode = 4;

    private ImageRecyclerAdapter adapter;
    private List<Bitmap> bitmapList;
    private String filename;
    private File dir, file;
    private ProgressDialog progressDialog;
    private List<String> stringList;
    private boolean success = false;


    private AdView adView;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_image);

        BannerAD();
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial));
        reqNewInterstitial();

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        bitmapList = new ArrayList<>();
        stringList = new ArrayList<>();

        EmptyRecyclerView recyclerView = (EmptyRecyclerView) findViewById(R.id.recycler_view_scan);
        recyclerView.setEmptyView(findViewById(R.id.empty_view_image));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new ImageRecyclerAdapter(this, bitmapList);
        recyclerView.setAdapter(adapter);

        dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/All Document Reader");
        progressDialog = new ProgressDialog(this);

    }

    private int dpToPx() {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));
    }

    public void onBtnGalleryClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            if (checkStoragePermission()) {
                gallery();
            } else {
                requestStoragePermission();
            }
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (checkStoragePermission()) {
                    gallery();
                } else {
                    requestStoragePermission();
                }
                reqNewInterstitial();
            }
        });

    }

    public void onBtnCameraClick(View view) {
        if (checkCameraPermission()) {
            scan();
        } else {
            requestCameraPermission();
        }
    }

    public void onCreatePdfClick(View view) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            if (adapter.getItemCount() > 0) {
                showCreateDialog();
            } else {
                Toast.makeText(this, "Select Image", Toast.LENGTH_SHORT).show();
            }

            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                if (adapter.getItemCount() > 0) {
                    showCreateDialog();
                } else {
                    Toast.makeText(ScanImageActivity.this, "Select Image", Toast.LENGTH_SHORT).show();
                }

                reqNewInterstitial();
            }
        });
    }

    private boolean checkStoragePermission() {
        int storagePermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return storagePermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkCameraPermission() {
        int cameraPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {

        ActivityCompat.requestPermissions(this, new String[]
                {
                        READ_EXTERNAL_STORAGE,
                }, RequestStoragePermissionCode);

    }

    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this, new String[]
                {
                        CAMERA,
                }, RequestCameraPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraPermissionCode:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!cameraPermission) {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    } else {
                        scan();
                    }
                }
                break;

            case RequestStoragePermissionCode:
                if (grantResults.length > 0) {
                    boolean storagePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (!storagePermission) {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    } else {
                        gallery();
                    }
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void scan() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanActivity.EXTRA_BRAND_IMG_RES, R.drawable.ic_crop_white_24dp); // Set image for title icon - optional
        intent.putExtra(ScanActivity.EXTRA_TITLE, "Edit"); // Set title in action Bar - optional
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(ScanActivity.EXTRA_ACTION_BAR_COLOR, R.color.colorPrimary); // Set title color - optional
        intent.putExtra(ScanActivity.EXTRA_LANGUAGE, "en"); // Set language - optional
        startActivityForResult(intent, 7);
    }

    private void gallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 8);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 7) {
            if (data != null) {
                String imgPath = data.getStringExtra(ScanActivity.RESULT_IMAGE_PATH);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(imgPath, options);
                bitmapList.add(bitmap);
                stringList.add(imgPath);
                adapter.notifyDataSetChanged();

            }
        } else if (requestCode == 8) {
            if (data != null) {

                try {
                    Uri pickedImage = data.getData();
                    String[] filePath = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                    cursor.moveToFirst();
                    String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
                    bitmapList.add(bitmap);
                    stringList.add(imagePath);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Only Images are allowed", Toast.LENGTH_SHORT).show();
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showCreateDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        builder.title("Creating Pdf");
        builder.content("Enter file name");

        builder.input("Example: abc", null, false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                filename = input.toString();
                filename = filename.replaceAll("%20", " ");
                file = new File(dir, filename + ".pdf");

                if (!file.exists()) {
                    ScanAsync async = new ScanAsync();
                    async.execute();
                } else {
                    MaterialDialog.Builder builder1 = new MaterialDialog.Builder(ScanImageActivity.this);
                    builder1.title("Overwrite");
                    builder1.content("File already exist! Do you want to overwrite?");
                    builder1.positiveText("Overwrite");
                    builder1.negativeText("Cancel");
                    builder1.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            ScanAsync async = new ScanAsync();
                            async.execute();
                        }
                    });
                    builder1.show();
                }
            }
        });

        builder.positiveText("Create Pdf");
        builder.negativeText("Cancel");
        builder.show();
    }

    private class ScanAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Creating Pdf...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            imgToPdf();
            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (!success) {
                Toast.makeText(ScanImageActivity.this, "Failed to create Pdf file", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(ScanImageActivity.this, PdfViewerActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.fromFile(file));
                startActivity(intent);
                finish();
            }
        }
    }

    private void imgToPdf() {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        document.setMargins(36, 36, 36, 36);
        Rectangle documentRect = document.getPageSize();

        if (!dir.exists()) {
            dir.mkdir();

        }

        file = new File(dir, filename + ".pdf");

        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            int quality = 30;

            for (int i = 0; i < stringList.size(); i++) {
                Image image = Image.getInstance(stringList.get(i));

                double qualityMod = quality * 0.09;
                image.setCompressionLevel((int) qualityMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(36);

                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(stringList.get(i), options);

                float pageWidth = document.getPageSize().getWidth() - (36 + 36);
                float pageHeight = document.getPageSize().getHeight() - (36 + 36);

                image.scaleToFit(pageWidth, pageHeight);

                image.setAbsolutePosition(
                        (documentRect.getWidth() - image.getScaledWidth()) / 2,
                        (documentRect.getHeight() - image.getScaledHeight()) / 2);

                document.add(image);
                document.newPage();

            }

            document.close();
            success = true;

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
