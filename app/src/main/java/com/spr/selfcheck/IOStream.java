package com.spr.selfcheck;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class IOStream {
    private Context context;
    int today;

    private File file;
    private File location;

    public IOStream(Context context, String today) {
        this.context = context;
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        this.file = new File(folder, "spr-backup-" + today + ".txt");
    }

    // Writing data
    public void writeToFile(String data) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write(data.getBytes());
            Toast.makeText(context, "Done" + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Reading data
    private String readFromFile(Context context) {
        return "";
    }

}
