package com.all.document.reader.pdf.ppt.world.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.adapter.PdfToolAdapter;
import com.all.document.reader.pdf.ppt.world.helper.GridSpacingItemDecoration;
import com.all.document.reader.pdf.ppt.world.model.Folder;
import com.all.document.reader.pdf.ppt.world.utils.Glide4Engine;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfImageObject;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class PdfToolsActivity extends AppCompatActivity implements PdfToolAdapter.PdfToolAdapterListener {

    private PdfToolAdapter adapter;
    private List<Folder> folderList;

    private AdView adView;
    private InterstitialAd interstitialAd;

    public static final int RequestCameraReadStoragePermissionCode = 2;

    private File dir, file, file2;
    private int pdfType;
    private String filename;
    private boolean success = false;
    private ProgressDialog progressDialog;
    private List<String> uriList;
    private ArrayList<File> fileArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_bar_main);

        BannerAD();
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial));
        reqNewInterstitial();

        initCollapsingToolbar();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_main);
        folderList = new ArrayList<>();
        adapter = new PdfToolAdapter(this, folderList, this);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "/All Document Reader");
        progressDialog = new ProgressDialog(this);
        uriList = new ArrayList<>();
        fileArrayList = new ArrayList<>();

        prepareFolders();

    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle("Pdf Tools");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private void prepareFolders() {
        int[] covers = new int[]{
                R.drawable.pdf_compress,
                R.drawable.pdf_merge,
                R.drawable.pdf_split,
                R.drawable.pdf_text,
                R.drawable.pdf_scan,
                R.drawable.pdf_image};

        Folder a = new Folder("Compress Pdf", covers[0]);
        folderList.add(a);

        Folder b = new Folder("Merge Pdf", covers[1]);
        folderList.add(b);

        Folder c = new Folder("Split Pdf", covers[2]);
        folderList.add(c);

        Folder d = new Folder("Text to Pdf", covers[3]);
        folderList.add(d);

        Folder e = new Folder("Scan", covers[4]);
        folderList.add(e);

        Folder f = new Folder("Images to Pdf", covers[5]);
        folderList.add(f);

        adapter.notifyDataSetChanged();
    }

    private int dpToPx() {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics()));
    }

    @Override
    public void onFolderClick(Folder folder) {
        String filename = folder.getTitle();

        switch (filename) {
            case "Compress Pdf":
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                } else {
                    compress();
                    reqNewInterstitial();
                }

                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        compress();
                        reqNewInterstitial();
                    }
                });
                break;

            case "Merge Pdf":
                merge();
                break;

            case "Split Pdf":
                split();
                break;

            case "Text to Pdf":
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                } else {
                    selectTxt();
                    reqNewInterstitial();
                }

                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        selectTxt();
                        reqNewInterstitial();
                    }
                });
                break;

            case "Scan":
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                } else {
                    scan();
                    reqNewInterstitial();
                }

                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        scan();
                        reqNewInterstitial();
                    }
                });
                break;

            case "Images to Pdf":
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                } else {
                    selectImages();
                    reqNewInterstitial();
                }

                interstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                        selectImages();
                        reqNewInterstitial();
                    }
                });
                break;
        }
    }

    private void compress() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, 6);
    }

    private void merge() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, 5);
    }

    private void split() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, 4);
    }

    private void selectTxt() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("application/text");
        startActivityForResult(intent, 3);
    }

    private void scan() {
        startActivity(new Intent(PdfToolsActivity.this, ScanImageActivity.class));
    }

    private void selectImages() {
        if (!checkCameraReadStoragePermission()) {
            requestCameraReadStoragePermission();
        } else {
            Matisse.from(this)
                    .choose(MimeType.ofImage())
                    .countable(true)
                    .capture(true)
                    .captureStrategy(new CaptureStrategy(true, getApplicationContext().getPackageName()))
                    .maxSelectable(1000)
                    .imageEngine(new Glide4Engine())
                    .forResult(2);
        }
    }

    private void requestCameraReadStoragePermission() {

        ActivityCompat.requestPermissions(this, new String[]{
                CAMERA,
                READ_EXTERNAL_STORAGE,
        }, RequestCameraReadStoragePermissionCode);

    }


    private boolean checkCameraReadStoragePermission() {
        int cameraPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        int readExternalPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
        return cameraPermissionResult == PackageManager.PERMISSION_GRANTED &&
                readExternalPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestCameraReadStoragePermissionCode:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (!cameraPermission && !readExternalPermission) {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    } else {
                        Matisse.from(this)
                                .choose(MimeType.ofImage())
                                .countable(true)
                                .capture(true)
                                .captureStrategy(new CaptureStrategy(true, getApplicationContext().getPackageName()))
                                .maxSelectable(1000)
                                .imageEngine(new Glide4Engine())
                                .forResult(2);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                uriList.clear();
                uriList.addAll(Matisse.obtainPathResult(data));
                pdfType = 2;
                showCreateDialog();
            }

        } else if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                file2 = new File(data.getData().getPath());
                if (file2.getName().endsWith(".txt")) {
                    pdfType = 3;
                    showCreateDialog();
                } else {
                    Toast.makeText(this, "No Text file detected", Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == 4 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                file2 = new File(data.getData().getPath());
                if (file2.getName().endsWith(".pdf")) {
                    pdfType = 4;
                    showCreateDialog();
                } else {
                    Toast.makeText(this, "No Pdf file detected", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == 5 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    fileArrayList.clear();
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        if (uri.getPath().endsWith(".pdf")) {
                            fileArrayList.add(new File(uri.getPath()));
                        }
                    }

                    if (!fileArrayList.isEmpty()) {
                        pdfType = 5;
                        showCreateDialog();
                    }

                } else {
                    Toast.makeText(this, "Less than 2 files detected", Toast.LENGTH_SHORT).show();
                }

            }
        } else if (requestCode == 6 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                file2 = new File(data.getData().getPath());
                if (file2.getName().endsWith(".pdf")) {
                    pdfType = 6;
                    showCreateDialog();
                } else {
                    Toast.makeText(this, "No Pdf file detected", Toast.LENGTH_SHORT).show();
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
                    PdfAsync async = new PdfAsync();
                    async.execute();
                } else {
                    MaterialDialog.Builder builder1 = new MaterialDialog.Builder(PdfToolsActivity.this);
                    builder1.title("Overwrite");
                    builder1.content("File already exist! Do you want to overwrite?");
                    builder1.positiveText("Overwrite");
                    builder1.negativeText("Cancel");
                    builder1.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            PdfAsync async = new PdfAsync();
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

    private class PdfAsync extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            progressDialog.setTitle("Creating Pdf...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            if (pdfType == 2) {
                imgToPdf();
            } else if (pdfType == 3) {
                txtToPdf();
            } else if (pdfType == 4) {
                splitPdf();
            } else if (pdfType == 5) {
                mergePdf();
            } else if (pdfType == 6) {
                compressPdf();
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (!success) {
                Toast.makeText(PdfToolsActivity.this, "Failed to create Pdf file", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(PdfToolsActivity.this, PdfViewerActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.fromFile(file));
                startActivity(intent);
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

            for (int i = 0; i < uriList.size(); i++) {
                Image image = Image.getInstance(uriList.get(i));

                double qualityMod = quality * 0.09;
                image.setCompressionLevel((int) qualityMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(36);

                BitmapFactory.Options options = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(uriList.get(i), options);

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


    private void txtToPdf() {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        document.setMargins(36, 36, 36, 36);

        if (!dir.exists()) {
            dir.mkdir();

        }
        try {
            file = new File(dir, filename + ".pdf");

            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            BufferedReader reader = new BufferedReader(new FileReader(file2));
            String line;
            Paragraph paragraph;
            Font normal = new Font(Font.FontFamily.TIMES_ROMAN, 12);
            Font bold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);
            boolean title = true;
            while ((line = reader.readLine()) != null) {
                paragraph = new Paragraph(line, title ? bold : normal);
                paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                title = line.isEmpty();
                document.add(paragraph);
            }

            document.close();
            success = true;

        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
    }

    private void splitPdf() {

        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            PdfReader reader = new PdfReader(file2.getPath());
            PdfCopy copy;
            Document document;
            int pages = reader.getNumberOfPages();
            for (int i = 1; i <= pages; i++) {
                document = new Document(PageSize.A4, 36, 36, 36, 36);
                document.setMargins(36, 36, 36, 36);

                file = new File(dir, filename + "(" + i + ")" + ".pdf");

                copy = new PdfCopy(document, new FileOutputStream(file));
                document.open();
                copy.addPage(copy.getImportedPage(reader, i));
                document.close();
                success = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
    }

    private void mergePdf() {

        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            PdfReader reader;
            Document document = new Document(PageSize.A4);
            document.setMargins(36, 36, 36, 36);

            file = new File(dir, filename + ".pdf");

            PdfCopy copy = new PdfCopy(document, new FileOutputStream(file));
            document.open();
            int numOfPages;

            if (!fileArrayList.isEmpty()) {
                for (int i = 0; i < fileArrayList.size(); i++) {
                    reader = new PdfReader(fileArrayList.get(i).getPath());
                    numOfPages = reader.getNumberOfPages();
                    for (int page = 1; page <= numOfPages; page++) {
                        copy.addPage(copy.getImportedPage(reader, page));
                    }
                }
            }

            document.close();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
    }


    private void compressPdf() {

        if (!dir.exists()) {
            dir.mkdir();
        }

        try {
            PdfReader reader = new PdfReader(file2.getPath());
            int n = reader.getXrefSize();
            PdfObject object;
            PRStream stream;

            for (int i = 0; i < n; i++) {
                object = reader.getPdfObject(i);
                if (object == null || !object.isStream())
                    continue;
                stream = (PRStream) object;
                PdfObject pdfsubtype = stream.get(PdfName.SUBTYPE);
                System.out.println(stream.type());
                if (pdfsubtype != null && pdfsubtype.toString().equals(PdfName.IMAGE.toString())) {
                    PdfImageObject image = new PdfImageObject(stream);
                    byte[] imageBytes = image.getImageAsBytes();
                    Bitmap bmp;
                    bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (bmp == null) continue;

                    int width = bmp.getWidth();
                    int height = bmp.getHeight();

                    Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas outCanvas = new Canvas(outBitmap);
                    outCanvas.drawBitmap(bmp, 0f, 0f, null);

                    ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();

                    outBitmap.compress(Bitmap.CompressFormat.JPEG, 70, imgBytes);
                    stream.clear();
                    stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
                    stream.put(PdfName.TYPE, PdfName.XOBJECT);
                    stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                    stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                    stream.put(PdfName.WIDTH, new PdfNumber(width));
                    stream.put(PdfName.HEIGHT, new PdfNumber(height));
                    stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                    stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
                }
            }

            reader.removeUnusedObjects();
            // Save altered PDF
            file = new File(dir, filename + ".pdf");
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(file));
            stamper.setFullCompression();
            stamper.close();
            reader.close();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
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

    public void reqNewInterstitial() {
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }
}
