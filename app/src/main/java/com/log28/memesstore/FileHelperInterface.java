package com.log28.memesstore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.InputStream;
import java.io.OutputStream;

public interface FileHelperInterface {

    public boolean createLocalFile(InputStream inputStream, String filename);
    //содание директории
    public void createDirs();
    public void deleteFile(String path);
public Bitmap getPreview(String filename);

}
