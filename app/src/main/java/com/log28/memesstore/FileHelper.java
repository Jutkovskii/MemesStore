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
    public String root;//корневая папка хранилища
    public String appFolder = "MemesStore2/";//папка с данными приложения
    public String previews = Environment.DIRECTORY_PICTURES+"/"+appFolder+"Previews/";//папка с превью ютуба
    public String images= Environment.DIRECTORY_PICTURES+"/"+appFolder+"Images/";//папка с изображениями
    public String videos=Environment.DIRECTORY_MOVIES+"/"+appFolder+"Videos/";//папка с видео


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
    //проверка на двойной слеш
    public String checkPath(String path){
        String result=path;
        result=result.replace("//","/");
        result=result.replace(".jpg.jpg",".jpg");
        result=result.replace(".png.png",".png");
        result=result.replace(".mp4.mp4",".mp4");
        return result;
    }
    //получение полного пути к файлу
    public String getFullPath(String filename){
        String path="";
        switch (getType(filename)){
            case IMAGE: path=root+images+filename; break;
            case VIDEO: path= root+videos+filename;break;
            case HTTPS: path= root+previews+filename+".jpg";break;
        }
        path=checkPath(path);
        return path;
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
    if(preview==null)
    {
        FileInputStream fis;
        try {
            fis = new FileInputStream(getFullPath(filename));
            preview = BitmapFactory.decodeStream(fis);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

       return preview;
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
            root = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
            new File (root+"/"+previews).mkdirs();
            new File (root+"/"+images).mkdirs();
            new File (root+"/"+videos).mkdirs();
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
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, images);
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
                    break;
                case VIDEO:
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH,  videos);
                    contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,contentValues);
                    break;
                case HTTPS:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH,  previews);
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
            root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            String[] imageDirectories = new String[]{appFolder + images,  appFolder + previews};
            for (String path : imageDirectories) {
                ContentValues contentValues = new ContentValues();
                ContentResolver contentResolver = context.getContentResolver();
                Uri locUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + path);
                contentResolver.update(locUri, contentValues, null, null);
            }
            ContentValues contentValues = new ContentValues();
            ContentResolver contentResolver = context.getContentResolver();
            Uri locUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/" + appFolder + videos);
            contentResolver.update(locUri, contentValues, null, null);
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
