package com.log28.memesstore;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

class PreviewSaver extends AsyncTask<String,Void,Void> {

    FileHelper fileHelper;


    PreviewSaver(FileHelper fileHelper){this.fileHelper = fileHelper;}
    @Override
    protected Void doInBackground(String... strings) {
        try {
            InputStream inputStream = (InputStream) new URL("https://img.youtube.com/vi/"+strings[0]+"/hqdefault.jpg").getContent();
            if(fileHelper!=null) {
                fileHelper.writeToFile(inputStream, fileHelper.createFile(FileClassifier.getMimeFolder(strings[0])+strings[0]+".jpg"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
return  null;
    }

}