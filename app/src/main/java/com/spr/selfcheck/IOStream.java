package com.spr.selfcheck;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IOStream {
    private Context context;
    private Uri uri;
    private OutputStream os;
    private FileOutputStream fos;
    private ContentValues value;
    private File file;

    private String today;
    private String fileName;
    private String docLocation;

    public IOStream(Context context, String today, String fileName) {
        this.context = context;
        this.today = today;
        this.fileName = fileName;
        docLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        file = new File(docLocation + "/SPR", fileName + today + ".txt");
    }

    // Writing data (split into 2 parts since Android 11 and above works differently)
    public void writeToFile(String data) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                value = new ContentValues();
                value.put(MediaStore.MediaColumns.DISPLAY_NAME, "spr-backup-" + today);
                value.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                value.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SPR");
                if (!(new File(docLocation + "/SPR", fileName + today + ".txt").exists())) {
                    uri = context.getContentResolver().insert(MediaStore.Files.getContentUri("external"), value);
                } else {
                    uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                }
                os = context.getContentResolver().openOutputStream(uri, "wa");
                os.write(data.getBytes());
                os.close();
            } else {
                fos = new FileOutputStream(file, true);
                fos.write(data.getBytes());
                fos.close();
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(context, R.string.error_file_create, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, R.string.error_file_inout, Toast.LENGTH_SHORT).show();
        }

    }
}