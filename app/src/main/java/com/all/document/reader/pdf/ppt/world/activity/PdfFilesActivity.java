package com.all.document.reader.pdf.ppt.world.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.all.document.reader.pdf.ppt.world.R;
import com.all.document.reader.pdf.ppt.world.adapter.DocumentAdapter;
import com.all.document.reader.pdf.ppt.world.model.Document;
import com.all.document.reader.pdf.ppt.world.utils.DividerItemDecoration;
import com.all.document.reader.pdf.ppt.world.utils.EmptyRecyclerView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PdfFilesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        DocumentAdapter.DocumentAdapterListener {

    private Toolbar toolbar;
    private EmptyRecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

    private DocumentAdapter adapter;
    private ActionMode actionMode;
    private SharedPreferences preferences;

    private ArrayList<File> fileList;
    private ArrayList<Document> documentList;

    private ActionModeCallback actionModeCallback;

    private AdView adView;
    private InterstitialAd interstitialAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_super);

        BannerAD();
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial));
        reqNewInterstitial();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (EmptyRecyclerView) findViewById(R.id.recycler_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        preferences = getSharedPreferences("files", MODE_PRIVATE);

        fileList = new ArrayList<>();
        documentList = new ArrayList<>();

        recyclerView.setEmptyView(findViewById(R.id.empty_view));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DocumentAdapter(this, documentList, this, "pdf");
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        actionModeCallback = new ActionModeCallback();

        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                fetchFiles();
            }
        });

    }

    private void fetchFiles() {

        swipeRefreshLayout.setRefreshing(true);

        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        getFile(root);

        ArrayList<Document> items = new ArrayList<>();

        for (int i = 0; i < fileList.size(); i++) {

            boolean imp = preferences.getBoolean(fileList.get(i).getPath(), false);

            Document doc = new Document(fileList.get(i), imp);
            items.add(doc);
        }

        Collections.sort(fileList);
        documentList.clear();
        documentList.addAll(items);
        fileList.clear();

        swipeRefreshLayout.setRefreshing(false);

        adapter.notifyDataSetChanged();
    }

    public void getFile(File dir) {
        File listFile[] = dir.listFiles();
        if (listFile != null && listFile.length > 0) {
            for (File aListFile : listFile) {
                if (aListFile.isDirectory()) {
                    getFile(aListFile);
                } else {
                    if (aListFile.getName().endsWith(".pdf")
                            ) {
                        fileList.add(aListFile);
                    }
                }
            }
        }
    }

    private void deleteFiles() {
        adapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                adapter.getSelectedItems();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            adapter.removeFile(selectedItemPositions.get(i));
        }
        adapter.notifyDataSetChanged();
    }

    private void shareFiles() {
        adapter.resetAnimationIndex();
        List<Integer> selectedItemPositions =
                adapter.getSelectedItems();
        List<File> fileList1 = new ArrayList<>();
        for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
            fileList1.add(adapter.getFile(selectedItemPositions.get(i)));
        }
        if (fileList1.size() == 1) {
            Uri outputFileUri = FileProvider.getUriForFile(PdfFilesActivity.this,
                    getResources().getString(R.string.file_provider_authority), fileList1.get(0));

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            sharingIntent.setType("application/pdf");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
            startActivity(Intent.createChooser(sharingIntent, "Share File Using"));
        } else {
            ArrayList<Uri> uriList = new ArrayList<>();
            for (int i = 0; i < fileList1.size(); i++) {
                uriList.add(FileProvider.getUriForFile(PdfFilesActivity.this,
                        getResources().getString(R.string.file_provider_authority), fileList1.get(i)));
            }

            Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            sharingIntent.setType("application/pdf");
            sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            startActivity(Intent.createChooser(sharingIntent, "Share Files Using"));
        }
    }

    private void enableActionMode(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_pdf, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {

            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_search:
                return true;

            case R.id.action_tools:
                startActivity(new Intent(PdfFilesActivity.this, PdfToolsActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        onRefresh();
        super.onResume();
    }

    @Override
    protected void onStart() {
        onRefresh();
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onRefresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fetchFiles();
            }
        });
    }

    @Override
    public void onIconClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }

        toggleSelection(position);
    }

    @Override
    public void onIconImportantClicked(int position) {
        // Star icon is clicked,
        // mark the message as important
        Document document = documentList.get(position);
        document.setImportant(!document.isImportant());

        if (document.isImportant()) {
            preferences.edit().putBoolean(document.getFile().getPath(), true).apply();
        } else {
            preferences.edit().putBoolean(document.getFile().getPath(), false).apply();
        }

        documentList.set(position, document);
        adapter.notifyDataSetChanged();
    }

    private void rowClick(int position) {
        if (adapter.getSelectedItemCount() > 0) {
            enableActionMode(position);
        } else {
            // read the message which removes bold from the row
            Document document = documentList.get(position);
            documentList.set(position, document);
            adapter.notifyDataSetChanged();

            Intent intent = new Intent(this, PdfViewerActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.fromFile(document.getFile()));
            startActivity(intent);
        }
    }

    @Override
    public void onRowClicked(int position) {
        rowClick(position);
    }

    @Override
    public void onRowLongClicked(final int position) {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            enableActionMode(position);
            reqNewInterstitial();
        }

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                enableActionMode(position);
                reqNewInterstitial();
            }
        });
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action_mode, menu);

            // disable swipe refresh if action mode is enabled
            swipeRefreshLayout.setEnabled(false);
            toolbar.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    MaterialDialog.Builder builder = new MaterialDialog.Builder(PdfFilesActivity.this);
                    builder.title("Delete!");
                    builder.content("Are you sure to delete?");
                    builder.positiveText("Yes");
                    builder.positiveColor(getResources().getColor(R.color.colorPrimary));
                    builder.negativeText("No");
                    builder.negativeColor(getResources().getColor(R.color.colorPrimary));
                    builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            deleteFiles();
                            mode.finish();
                        }
                    });
                    builder.show();
                    return true;

                case R.id.action_share:
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
                        shareFiles();
                        mode.finish();
                        reqNewInterstitial();
                    }

                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            shareFiles();
                            mode.finish();
                            reqNewInterstitial();
                        }
                    });
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelections();
            swipeRefreshLayout.setEnabled(true);
            actionMode = null;
            recyclerView.post(new Runnable() {
                @Override
                public void run() {
                    adapter.resetAnimationIndex();
                    adapter.notifyDataSetChanged();
                }
            });
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toolbar.setVisibility(View.VISIBLE);
                }
            }, 400);
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
