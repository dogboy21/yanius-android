package ml.dogboy.yanius;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ml.dogboy.yanius.upload.UploadJob;
import ml.dogboy.yanius.upload.UploadTask;
import ml.dogboy.yanius.upload.UploadedItem;

public class UploadActivity extends AppCompatActivity {

    private static final int PICK_FILE = 0;
    private String yaniusUrl;
    private String yaniusApiKey;
    private List<UploadedItem> uploadedItems;
    private UploadedItemsAdapter uploadedItemsAdapter;
    private ClipboardManager clipboardManager;
    private UploadedItemsDatabase uploadedItemsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (preferences.getBoolean("pref_darkmode", false)) {
            this.getTheme().applyStyle(R.style.AppTheme_Dark, true);
        }
        this.yaniusUrl = preferences.getString("pref_yanius_instance", "https://ss.example.com");
        this.yaniusApiKey = preferences.getString("pref_yanius_apikey", "changeme");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        this.clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        this.uploadedItemsDatabase = new UploadedItemsDatabase(this);

        this.uploadedItems = new ArrayList<>();
        ListView uploadedItemsList = (ListView) this.findViewById(R.id.uploaded_items);
        Cursor cursor = this.uploadedItemsDatabase.getReadableDatabase().query(false, "uploadeditems", null, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            UploadedItem item = new UploadedItem(cursor.getString(0), cursor.getInt(1));
            item.setSucceeded(cursor.getInt(2) != 0);
            item.setUrl(cursor.getString(3));
            this.uploadedItems.add(item);
        }
        cursor.close();
        this.uploadedItemsAdapter = new UploadedItemsAdapter(this, R.layout.uploaded_item, this.uploadedItems);
        uploadedItemsList.setAdapter(this.uploadedItemsAdapter);
        uploadedItemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UploadedItem item = UploadActivity.this.uploadedItems.get(UploadActivity.this.uploadedItems.size() - 1 - position);
                if (item != null && item.isSucceeded()) {
                    Toast.makeText(UploadActivity.this, UploadActivity.this.getResources().getText(R.string.url_copied), Toast.LENGTH_SHORT).show();
                    UploadActivity.this.clipboardManager.setPrimaryClip(ClipData.newPlainText(item.getFileName(), item.getUrl()));
                }
            }
        });
        uploadedItemsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                UploadedItem item = UploadActivity.this.uploadedItems.get(UploadActivity.this.uploadedItems.size() - 1 - position);
                if (item != null && item.isSucceeded()) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, item.getUrl());
                    sendIntent.setType("text/plain");
                    startActivity(sendIntent);
                    return true;
                }
                return false;
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                try {
                    this.startUpload(imageUri);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void pickFile(View v) {
        Intent pickFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
        pickFileIntent.setType("*/*");
        pickFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
        this.startActivityForResult(Intent.createChooser(pickFileIntent, this.getResources().getText(R.string.choose_file)), PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == UploadActivity.PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this, this.getResources().getText(R.string.no_data_received), Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                this.startUpload(data.getData());
            } catch (FileNotFoundException e) {
                Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this.getBaseContext(), SettingsActivity.class);
            this.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public int getFileSize(Uri uri) {
        if (uri.getScheme().equals("content")) {
            Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if (!cursor.isNull(sizeIndex)) {
                        return cursor.getInt(sizeIndex);
                    } else {
                        return -1;
                    }
                }
            } finally {
                cursor.close();
            }
        }
        return -1;
    }

    public void startUpload(Uri file) throws FileNotFoundException {
        final InputStream inputStream = this.getApplicationContext().getContentResolver().openInputStream(file);
        String fileName = this.getFileName(file);
        int fileSize = this.getFileSize(file);
        final UploadedItem item = new UploadedItem(fileName, fileSize);
        item.setUploading(true);
        this.uploadedItems.add(item);
        this.uploadedItemsAdapter.notifyDataSetChanged();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", fileName);
        contentValues.put("size", fileSize);
        contentValues.put("succeeded", false);
        contentValues.put("url", (String) null);
        final long rowId = this.uploadedItemsDatabase.getWritableDatabase().insert("uploadeditems", null, contentValues);
        Log.d("ml.dogboy.yanius", String.valueOf(rowId));
        new UploadTask(this.yaniusUrl, this.yaniusApiKey).execute(new UploadJob(fileName, inputStream,
                new UploadJob.Callback<String>() {
                    @Override
                    public void run(final String param) {
                        item.setUploading(false);
                        item.setSucceeded(true);
                        item.setUrl(param);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("succeeded", true);
                        contentValues.put("url", param);
                        int rows = UploadActivity.this.uploadedItemsDatabase.getWritableDatabase().update("uploadeditems", contentValues, "ROWID=" + rowId, null);
                        Log.d("ml.dogboy.yanius", String.valueOf(rows));
                        UploadActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UploadActivity.this.uploadedItemsAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                },
                new UploadJob.Callback<Exception>() {
                    @Override
                    public void run(final Exception param) {
                        item.setUploading(false);
                        item.setSucceeded(false);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("url", param.getMessage());
                        int rows = UploadActivity.this.uploadedItemsDatabase.getWritableDatabase().update("uploadeditems", contentValues, "ROWID=" + rowId, null);
                        UploadActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                UploadActivity.this.uploadedItemsAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }));
    }

}
