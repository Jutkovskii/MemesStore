package com.log28.memesstore;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class PreviewSaver extends AsyncTask<String,Void,Void> {

    FileHelper2 fileHelper2;
    PreviewSaver(FileHelper2 fileHelper2){this.fileHelper2 = fileHelper2;}
    @Override
    protected Void doInBackground(String... strings) {
        try {
            InputStream inputStream = (InputStream) new URL("https://img.youtube.com/vi/"+strings[0]+"/hqdefault.jpg").getContent();
            fileHelper2.copyFile(inputStream, fileHelper2.createFile(strings[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
return  null;
    }

}