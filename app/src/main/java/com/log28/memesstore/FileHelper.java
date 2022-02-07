package com.log28.memesstore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    public Context context;
    public String root;//корневая папка хранилища
    public String previews;//папка с превью ютуба
    public String images;//папка с изображениями
    public String videos;//папка с видео
    //public String fileLocationForBD;//расположение файла (сохраняется в БД)
    public String filename;//имя файла (сохраняется в БД)
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
    public int getType(String filename){
        if(filename.endsWith(".jpg")||filename.endsWith(".png"))
            return IMAGE;
        if(filename.endsWith(".mp4"))
            return VIDEO;
        if(!filename.contains("."))
            return HTTPS;
        return -1;
    }
    //генерирует полный путь к файлу для записи в БД
    public String getFileLocation(String filename)
    {
        String  fullpath="";
        switch (getType(filename)) {
            case IMAGE:
                fullpath = images + filename;
                break;
            case VIDEO:
                fullpath = videos + filename;
                break;
            case HTTPS:
                fullpath = filename;
        }
        return  fullpath;
    }
    //возвращает Битмап для создания превью
    public Bitmap getPreview(String filename){
       Bitmap preview=null;

    switch(getType(filename)){
        case IMAGE:
            String path = root+"/Pictures/"+images+filename;
            File file1 = new File(path);
            boolean qwe1= file1.exists();
            preview=BitmapFactory.decodeFile(path);
            break;
        case VIDEO:
            File file = new File(filename);
            boolean qwe= file.exists();
            preview= ThumbnailUtils.createVideoThumbnail(videos+filename,MediaStore.Images.Thumbnails.MINI_KIND);
            break;
        case HTTPS:
            preview=BitmapFactory.decodeFile(previews+filename+".jpg");
            break;
    }
       return preview;
    }

    //создание локального файла
    public boolean createLocalFile(InputStream inputStream, String filename){
    return this.fileHelper.createLocalFile(inputStream,filename);

    }
    //копирование данных в файл
    public void copyFile(InputStream inputStream, OutputStream outputStream){
        try {

            int n;
            byte[] buffer = new byte[1024 * 4];
            while (-1 != (n = inputStream.read(buffer)))
                outputStream.write(buffer, 0, n);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            int z=0;
        }
    }
    //содание директории
    public void createDirs(){
        this.fileHelper.createDirs();
    }
    //удаление локального файла
    public void deleteFile(String path){
        this.fileHelper.deleteFile(path);
    }


    public class innerFileHelperOld implements FileHelperInterface {


        public innerFileHelperOld(Context context) {
            createDirs();
        }


        public boolean createLocalFile(InputStream inputStream, String filename) {
            String fullpath = "";
            switch (getType(filename)) {
                case IMAGE:
                    fullpath = images + filename;
                    break;
                case VIDEO:
                    fullpath = videos + filename;
                    break;
                case HTTPS:
                    fullpath = previews + filename + ".jpg";
                    break;
            }
            try {

                FileOutputStream outputStream = new FileOutputStream(fullpath);
                copyFile(inputStream, outputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        public void createDirs(){
            root = Environment.getExternalStorageDirectory().getAbsolutePath();
            String pictures= root+"/Pictures/";
            String homePictures=root+"/Pictures/MemesStore2/";
            previews = root+"/Pictures/MemesStore2/Previews/";
            images = root+"/Pictures/MemesStore2/Images/";

            String movies= root+"/Movies/";
            String homeMovies=root+"/Movies/MemesStore2/";
            videos = root+"/Movies/MemesStore2/Videos/";

            new File(pictures).mkdir();
            new File(homePictures).mkdir();
            new File(previews).mkdir();
            new File(images).mkdir();

            new File(movies).mkdir();
            new File(homeMovies).mkdir();
            new File(videos).mkdir();
        }


        public void deleteFile(String path) {
            new File(path).delete();
        }


    }

    public class innerFileHelperNew implements  FileHelperInterface {

        public innerFileHelperNew(Context context) {
            createDirs();
        }

        public boolean createLocalFile(InputStream inputStream, String filename) {

            String fullpath="";
            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            Uri locuri=null;
            switch(getType(filename)){
                case IMAGE:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+images);
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                    fullpath=Environment.DIRECTORY_PICTURES+images+filename;
                    break;
                case VIDEO:
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES+videos);
                    contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,contentValues);
                    fullpath=Environment.DIRECTORY_MOVIES+videos+filename;
                    break;
                case HTTPS:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+previews);
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename+".jpg");
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                    fullpath=filename;//=Environment.DIRECTORY_PICTURES+previews+filename+".jpg";
                    break;
            }
            try {
                OutputStream outputStream = contentResolver.openOutputStream(locuri);
                copyFile(inputStream,outputStream);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            contentResolver.update(locuri,contentValues,null,null);

            return true;
        }


        public String getFileLocation(String filename) {
            String  fullpath="";
            switch (getType(filename)) {
                case IMAGE:
                    fullpath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_PICTURES+images + filename;
                    break;
                case VIDEO:
                    fullpath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+Environment.DIRECTORY_MOVIES+videos + filename;
                    break;
                case HTTPS:
                    fullpath = filename;
            }
            return  fullpath;

        }


        public void createDirs() {

            root ="";// Environment.getExternalStorageDirectory().getAbsolutePath();
            String homePictures=root+"/MemesStore2/";
            previews = root+"/MemesStore2/Previews/";
            images = root+"/MemesStore2/Images/";

            String homeMovies=root+"/MemesStore2/";
            videos = root+"/MemesStore2/Videos/";


            String []imageDirectories = new String[]{homePictures,images,previews };
            for(String path:imageDirectories) {
                ContentValues contentValues = new ContentValues();
                ContentResolver contentResolver = context.getContentResolver();
                Uri locUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + path);
                contentResolver.update(locUri, contentValues, null, null);
            }
            String []videoDirectories = new String[]{homeMovies,videos };
            for(String path:videoDirectories) {
                ContentValues contentValues = new ContentValues();
                ContentResolver contentResolver = context.getContentResolver();
                Uri locUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + path);
                contentResolver.update(locUri, contentValues, null, null);
            }
            root = Environment.getExternalStorageDirectory().getAbsolutePath();

        }


        public void deleteFile(String path) {
            ContentResolver contentResolver = context.getContentResolver();
            Uri locuri = Uri.parse(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                contentResolver.delete(locuri,null);
            }
        }
    }

}
