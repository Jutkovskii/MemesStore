package com.log28.memesstore;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

public class FileHelper {
    public Context context;
    public static String root;//корневая папка хранилища
    public static String appFolder = "MemesStore2/";//папка с данными приложения
    public static String previews = Environment.DIRECTORY_PICTURES+"/"+appFolder+"Previews/";//папка с превью ютуба
    public static String images= Environment.DIRECTORY_PICTURES+"/"+appFolder+"Images/";//папка с изображениями
    public static String videos=Environment.DIRECTORY_MOVIES+"/"+appFolder+"Videos/";//папка с видео

    //категории файлов
    public static final int IMAGE=0;
    public static final int VIDEO=1;
    public static final int HTTPS=2;
    public  FileHelperInterface fileHelper;
    public FileHelper(Context context){
        this.context=context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            fileHelper = new innerFileHelperNew(context);
        else
            fileHelper = new innerFileHelperOld(context);
    }

    //возвращает тип данных согласно имени файла
    public static int getType(String filename){
        if(filename.toLowerCase().endsWith(".jpg")||filename.toLowerCase().endsWith(".png"))
            return IMAGE;
        if(filename.toLowerCase().endsWith(".mp4"))
            return VIDEO;
        if(!filename.contains("."))
            return HTTPS;
        return -1;
    }
    //проверка на двойной слеш
    public static String checkPath(String path){
        String result=path;
        result=result.replace("//","/");
        result=result.replace(".jpg.jpg",".jpg");
        result=result.replace(".png.png",".png");
        result=result.replace(".mp4.mp4",".mp4");
        return result;
    }
    //получение полного пути к файлу
    public static String getFullPath(String filename){
        String path="";
        switch (getType(filename)){
            case IMAGE: path=root+images+filename; break;
            case VIDEO: path= root+videos+filename;break;
            case HTTPS: path= root+previews+filename+".jpg";break;
        }
        path=checkPath(path);
        return path;
    }
    //проверка существования файла
    public static boolean isExist(String filename){
        return new File(getFullPath(filename)).exists();
    }
    //копирование данных в файл
    public static void copyFile(InputStream inputStream, OutputStream outputStream){
        try {

            int n;
            byte[] buffer = new byte[1024 * 4];
            while (-1 != (n = inputStream.read(buffer)))
                outputStream.write(buffer, 0, n);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //возвращает Битмап для создания превью
    @RequiresApi(api = Build.VERSION_CODES.R)
    public Bitmap getPreview(String filename){
        return fileHelper.getPreview(filename);
      /* Bitmap preview=null;

    switch(getType(filename)){
        case IMAGE:
            preview=BitmapFactory.decodeFile(getFullPath(filename),getOptions(filename));
            if(preview==null)
            {
                FileInputStream fis;
                try {
                    fis= onRead(filename);
                    if(fis==null)
                        fis = new FileInputStream(getFullPath(filename));
                    preview = BitmapFactory.decodeStream(fis);

                } catch (Exception e) {
                    e.printStackTrace();
                    preview = BitmapFactory.decodeResource(context.getResources(),R.raw.notfound);
                }

            }

            break;
        case VIDEO:
            preview= ThumbnailUtils.createVideoThumbnail(getFullPath(filename),MediaStore.Images.Thumbnails.MINI_KIND);
            if(preview==null)
                preview=createThumbnail(filename);
            break;
        case HTTPS:
            preview=BitmapFactory.decodeFile(getFullPath(filename));
            break;
    }
    if(preview==null)
    {
        FileInputStream fis;
        try {
           fis= onRead(filename);
           if(fis==null)
            fis = new FileInputStream(getFullPath(filename));
            preview = BitmapFactory.decodeStream(fis);

        } catch (Exception e) {
            e.printStackTrace();
            preview = BitmapFactory.decodeResource(context.getResources(),R.raw.notfound);
        }

    }

       return preview;*/
    }


    public FileInputStream onRead(String filename) {
        String[] selectionArgs;
        if(!filename.contains("."))
        {
            filename=filename+".jpg";
           selectionArgs = new String[]{Environment.DIRECTORY_PICTURES + "/" + appFolder + "Previews/"};
        }
        else
            if(filename.endsWith("mp4"))
                selectionArgs = new String[]{Environment.DIRECTORY_MOVIES + "/" + appFolder + "Videos/"};
            else
            selectionArgs = new String[]{Environment.DIRECTORY_PICTURES + "/" + appFolder + "Images/"};


        Uri contentUri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";



        Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);

        Uri uri = null;

        if (cursor.getCount() == 0) {
            Toast.makeText(context, "No file found", Toast.LENGTH_LONG).show();
        } else {
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));

                if (fileName.equals(filename)) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

                    uri = ContentUris.withAppendedId(contentUri, id);

                    break;
                }
            }

            if (uri == null) {
                Toast.makeText(context, "file not found", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);


//////////////////////////////////////////////////////////////
return (FileInputStream) inputStream;

                } catch (IOException e) {
                    Toast.makeText(context, "Fail to read file", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return null;
    }

    public  Bitmap createThumbnail(String filename) {
        Bitmap bitmap = null;
        String[] selectionArgs = new String[]{Environment.DIRECTORY_MOVIES + "/" + appFolder + "Videos/"};
        Uri contentUri = MediaStore.Files.getContentUri("external");

        String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";



        Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);

        Uri uri = null;

        if (cursor.getCount() == 0) {
            Toast.makeText(context, "No file found", Toast.LENGTH_LONG).show();
        } else {
            while (cursor.moveToNext()) {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));

                if (fileName.equals(filename)) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));

                    uri = ContentUris.withAppendedId(contentUri, id);

                    break;
                }
            }

            if (uri == null) {
                Toast.makeText(context, "file not found", Toast.LENGTH_SHORT).show();
            } else {




        MediaMetadataRetriever mediaMetadataRetriever = null;

        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(context, uri);
            bitmap = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
            }
        }

        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public BitmapFactory.Options getOptions(String filename) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (getType(filename) != VIDEO){
            BitmapFactory.decodeFile(getFullPath(filename), options);
        int koef = (int) ((float) (options.outWidth) / (float) (context.getDisplay().getWidth()) * 2);
        if (koef % 2 != 0) koef++;
        options.inSampleSize = koef;
    }
        options.inJustDecodeBounds = false;

          return options;

    }
    //создание локального файла
    public boolean createLocalFile(InputStream inputStream, String filename){
    return this.fileHelper.createLocalFile(inputStream,filename);

    }

    //удаление локального файла
    public void deleteFile(String path){
        this.fileHelper.deleteFile(path);
    }



    public class innerFileHelperOld implements FileHelperInterface {


        public innerFileHelperOld(Context context) {
            root = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
            createDirs();
        }


        public boolean createLocalFile(InputStream inputStream, String filename) {
               String fullpath = getFullPath(filename);

            try {
File qwe= new File(fullpath);
qwe.createNewFile();
                FileOutputStream outputStream = new FileOutputStream(fullpath);
                copyFile(inputStream, outputStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }

        public void createDirs(){

            new File (root+"/"+previews).mkdirs();
            new File (root+"/"+images).mkdirs();
            new File (root+"/"+videos).mkdirs();
        }


        public void deleteFile(String path) {
new File(getFullPath(path)).delete();

        }

        //возвращает Битмап для создания превью
        @RequiresApi(api = Build.VERSION_CODES.R)
        public Bitmap getPreview(String filename){
            Bitmap preview=null;

            switch(getType(filename)){
                case IMAGE:
                    preview=BitmapFactory.decodeFile(getFullPath(filename),getOptions(filename));
                    break;
                case VIDEO:
                    preview= ThumbnailUtils.createVideoThumbnail(getFullPath(filename),MediaStore.Images.Thumbnails.MINI_KIND);
                    break;
                case HTTPS:
                    preview=BitmapFactory.decodeFile(getFullPath(filename));
                    break;
            }
            if(preview==null)  preview = BitmapFactory.decodeResource(context.getResources(),R.raw.notfound);
            return preview;
        }

    }

    public class innerFileHelperNew implements  FileHelperInterface {

        public innerFileHelperNew(Context context) {

            root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }

        public boolean createLocalFile(InputStream inputStream, String filename) {

            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            Uri locuri = null;
            switch (getType(filename)) {
                case IMAGE:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, images);
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    break;
                case VIDEO:
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, videos);
                    contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                    break;
                case HTTPS:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, previews);
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename + ".jpg");
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    break;
            }
            try {
                OutputStream outputStream = contentResolver.openOutputStream(locuri);
                copyFile(inputStream, outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            contentResolver.update(locuri, contentValues, null, null);

            return true;
        }

        //Рудимент, создает папки ,но в них пустые файлы
        public void createDirs() {

            String[] imageDirectories = new String[]{ images, previews};
            for (String path : imageDirectories) {
                ContentValues contentValues = new ContentValues();
                ContentResolver contentResolver = context.getContentResolver();
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, path);
                Uri locUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                contentResolver.update(locUri, contentValues, null, null);
            }
            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, videos+"qwe.mp4");
            Uri locUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            contentResolver.update(locUri, contentValues, null, null);
        }

        public void deleteFile(String path) {
            try {
                ContentResolver contentResolver = context.getContentResolver();
                Uri locuri = null;
                if (getType(path) == IMAGE || getType(path) == HTTPS)
                    locuri = MediaStore.Images.Media.getContentUri("external");
                if (getType(path) == VIDEO)
                    locuri = MediaStore.Video.Media.getContentUri("external");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentResolver.delete(locuri, MediaStore.MediaColumns.DATA + "=?", new String[]{getFullPath(path)});
                }
            } catch (Exception e) {
                e.printStackTrace();
                int y = 0;
            }

        }

        //возвращает Битмап для создания превью
        @RequiresApi(api = Build.VERSION_CODES.R)
        public Bitmap getPreview(String filename) {
            Bitmap preview = null;


            String[] selectionArgs = null;
            switch (getType(filename)) {
                case IMAGE:
                    selectionArgs = new String[]{images};
                    break;
                case VIDEO:
                    selectionArgs = new String[]{videos};
                    break;
                case HTTPS:
                    filename=filename+".jpg";
                    selectionArgs = new String[]{previews};
                    break;
            }

            Uri contentUri = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);
            cursor.moveToFirst();

            Uri uri=null;
            do {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                if (fileName.equals(filename)) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    uri = ContentUris.withAppendedId(contentUri, id);
                    break;
                }
            }while (cursor.moveToNext());


            if (getType(filename) == IMAGE) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(uri);
                    preview = BitmapFactory.decodeStream(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
            if (getType(filename) == VIDEO||getType(filename) == HTTPS){
                MediaMetadataRetriever mediaMetadataRetriever = null;

                try {
                    mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(context, uri);
                    preview = mediaMetadataRetriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mediaMetadataRetriever != null) {
                        mediaMetadataRetriever.release();
                    }
                }


            }
            return preview;
        }
    }
}
