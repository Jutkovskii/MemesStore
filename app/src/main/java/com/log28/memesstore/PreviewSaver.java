package com.log28.memesstore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

class PreviewSaver extends AsyncTask<String,Void,Void> {

    FileHelper fileHelper;
    PreviewSaver(FileHelper fileHelper){this.fileHelper=fileHelper;}
    @Override
    protected Void doInBackground(String... strings) {
        try {
            InputStream inputStream = (InputStream) new URL("https://img.youtube.com/vi/"+strings[0]+"/hqdefault.jpg").getContent();
            fileHelper.copyFile(inputStream,fileHelper.createFile(strings[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
return  null;
    }

}