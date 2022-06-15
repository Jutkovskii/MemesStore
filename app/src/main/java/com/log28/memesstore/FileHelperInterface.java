package com.log28.memesstore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileHelperInterface {

     OutputStream createFile(String filename);
    boolean isExist(String filename);
    void deleteFile(String path);
    Bitmap getPreview(String filename, BitmapFactory.Options options);
    Uri getVideoUri(String filename);
}
