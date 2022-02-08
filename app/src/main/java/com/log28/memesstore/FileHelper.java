package com.log28.memesstore;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    public Context context;
    public String root;//корневая папка хранилища
    public String appFolder = "MemesStore2/";//папка с данными приложения
    public String previews = "Previews/";//папка с превью ютуба
    public String images= "Images/";//папка с изображениями
    public String videos="Videos/";//папка с видео


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

    public String getFullPath(String filename){
        switch (getType(filename)){
            case IMAGE: return root+"/"+Environment.DIRECTORY_PICTURES+"/"+appFolder+images+filename;
            case VIDEO: return root+"/"+Environment.DIRECTORY_MOVIES+"/"+appFolder+videos+filename;
            case HTTPS: return root+"/"+Environment.DIRECTORY_PICTURES+"/"+appFolder+previews+filename+".jpg";
        }
        return "";
    }
    public String getRelativePath(String filename){
        switch (getType(filename)){
            case IMAGE: return Environment.DIRECTORY_PICTURES+"/"+appFolder+images;
            case VIDEO: return Environment.DIRECTORY_MOVIES+"/"+appFolder+videos;
            case HTTPS: return Environment.DIRECTORY_PICTURES+"/"+appFolder+previews;
        }
        return "";
    }
    //возвращает Битмап для создания превью
    public Bitmap getPreview(String filename){
       Bitmap preview=null;

    switch(getType(filename)){
        case IMAGE:
            preview=BitmapFactory.decodeFile(getFullPath(filename));
            break;
        case VIDEO:
            preview= ThumbnailUtils.createVideoThumbnail(getFullPath(filename),MediaStore.Images.Thumbnails.MINI_KIND);
            break;
        case HTTPS:
            preview=BitmapFactory.decodeFile(getFullPath(filename));
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

    public boolean isExist(String filename){
     return new File(getFullPath(filename)).exists();
    }

    public class innerFileHelperOld implements FileHelperInterface {


        public innerFileHelperOld(Context context) {
            createDirs();
        }


        public boolean createLocalFile(InputStream inputStream, String filename) {
               String fullpath = getFullPath(filename);

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
            new File(root+"/"+Environment.DIRECTORY_PICTURES).mkdir();
            new File(root+"/"+Environment.DIRECTORY_PICTURES+"/"+appFolder).mkdir();
            new File(root+"/"+Environment.DIRECTORY_PICTURES+"/"+appFolder+previews).mkdir();
            new File(root+"/"+Environment.DIRECTORY_PICTURES+"/"+appFolder+images).mkdir();

            new File(root+"/"+Environment.DIRECTORY_MOVIES).mkdir();
            new File(root+"/"+Environment.DIRECTORY_MOVIES+"/"+appFolder).mkdir();
            new File(root+"/"+Environment.DIRECTORY_MOVIES+"/"+appFolder).mkdir();
        }


        public void deleteFile(String path) {
new File(getFullPath(path)).delete();

        }


    }

    public class innerFileHelperNew implements  FileHelperInterface {

        public innerFileHelperNew(Context context) {
            createDirs();
        }

        public boolean createLocalFile(InputStream inputStream, String filename) {

            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            Uri locuri=null;
            switch(getType(filename)){
                case IMAGE:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, getRelativePath(filename));
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                    break;
                case VIDEO:
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH,  getRelativePath(filename));
                    contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,contentValues);
                    break;
                case HTTPS:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,  getRelativePath(filename));
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename+".jpg");
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
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




        public void createDirs() {

            root = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";


            String []imageDirectories = new String[]{appFolder,appFolder+images,appFolder+previews };
            for(String path:imageDirectories) {
                ContentValues contentValues = new ContentValues();
                ContentResolver contentResolver = context.getContentResolver();
                Uri locUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/"+path);
                contentResolver.update(locUri, contentValues, null, null);
            }
            String []videoDirectories = new String[]{appFolder,appFolder+videos };
            for(String path:videoDirectories) {
                ContentValues contentValues = new ContentValues();
                ContentResolver contentResolver = context.getContentResolver();
                Uri locUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES+"/"+path);
                contentResolver.update(locUri, contentValues, null, null);
            }
            root = Environment.getExternalStorageDirectory().getAbsolutePath();

        }


        public void deleteFile(String path) {
            try{
                ContentResolver contentResolver = context.getContentResolver();
            Uri locuri=null;
            if(getType(path)==IMAGE||getType(path)==HTTPS)
            locuri=MediaStore.Images.Media.getContentUri("external");
            if(getType(path)==VIDEO)
                locuri=MediaStore.Video.Media.getContentUri("external");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                contentResolver.delete(locuri, MediaStore.MediaColumns.DATA+"=?",new String[]{getFullPath(path)});
            }
        }
            catch(Exception e){
                e.printStackTrace();
                int y=0;
            }

        }
    }

}
