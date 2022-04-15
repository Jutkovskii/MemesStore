package com.log28.memesstore;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileHelper {
    public Context context;
    public static String root;//корневая папка хранилища
    public static String appFolder = "MemesStore2/";//папка с данными приложения
    public static String previews = Environment.DIRECTORY_PICTURES+"/"+appFolder+"Previews/";//папка с превью ютуба
    public static String images= Environment.DIRECTORY_PICTURES+"/"+appFolder+"Images/";//папка с изображениями
    public static String videos=Environment.DIRECTORY_MOVIES+"/"+appFolder+"Videos/";//папка с видео
    public static String gifs=Environment.DIRECTORY_MOVIES+"/"+appFolder+"Gifs/";//папка с Gif

    //категории файлов
    public static final int IMAGE=0;
    public static final int VIDEO=1;
    public static final int HTTPS=2;
    public static final int GIF=3;
    public static final int FILE=-1;
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
        if(filename.toLowerCase().endsWith(".jpg")||filename.toLowerCase().endsWith(".png")||filename.toLowerCase().endsWith(".webp"))
            return IMAGE;
        if(filename.toLowerCase().endsWith(".mp4"))
            return VIDEO;
        if(filename.toLowerCase().endsWith(".gif"))
            return GIF;
        if(filename.contains("db"))
            return FILE;
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
        result=result.replace(".webp.webp.",".webp.");
        result=result.replace(".gif.gif",".gif");
        result=result.replace(".mp4.mp4",".mp4");
        return result;
    }
    //получение полного пути к файлу
    public static String getFullPath(String filename){
        String path="";
        switch (getType(filename)){
            case IMAGE: path=root+images+filename; break;
            case VIDEO: path= root+videos+filename;break;
            case GIF:   path= root+gifs+filename;break;
            case HTTPS: path= root+previews+filename+".jpg";break;
            case FILE: path=root+Environment.DIRECTORY_DOWNLOADS+"/"+filename;break;
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

        return fileHelper.getPreview(filename,getOptions(filename));
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
    @RequiresApi(api = Build.VERSION_CODES.R)
    public String createLocalFile(InputStream inputStream, String filename){
        filename= filename.replaceAll(".webp",".jpg");
     boolean res=this.fileHelper.createLocalFile(inputStream,filename);
     //добавить в виде опции
         // resizeImageForTG(filename);
        return filename;
    }

    public OutputStream createFile(String filename)
    {
        return this.fileHelper.createFile(filename);
    }
    //удаление локального файла
    public void deleteFile(String path){
        this.fileHelper.deleteFile(path);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void resizeImageForTG(String filename){
       if (getType(filename)==IMAGE) {
           BitmapFactory.Options options = getOptions(filename);
           float width = options.outWidth;
           float height = options.outHeight;
           final float WIDTH = 1200;
           final float HEIGHT = 1200;
           float scaleParameter = Math.max(WIDTH / width , HEIGHT / height );
           float criteria = 2;
           if (scaleParameter > criteria )/*|| scaleParameter < 1.0 / criteria) */{
               Bitmap tempBitmap = getPreview(filename);
               tempBitmap = Bitmap.createScaledBitmap(tempBitmap, Math.round(tempBitmap.getWidth() * scaleParameter), Math.round(tempBitmap.getHeight() * scaleParameter), false);
               ByteArrayOutputStream bos = new ByteArrayOutputStream();
               tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
               byte[] bitmapdata = bos.toByteArray();
               ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
               deleteFile(filename);
             // filename= filename.replaceAll(".webp",".jpg");
               this.fileHelper.createLocalFile(bs, filename);

           }
       }

    }


    public Uri getVideoUri(String filename){
        return fileHelper.getVideoUri(filename);
    }

public String zipPack(List<String> files){
    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = new Date(System.currentTimeMillis());
     String zipFilename = formatter.format(date)+".zip";
    try {
        OutputStream outputStream=createFile(zipFilename);
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

    for(String file:files){

        FileInputStream fileInputStream = new FileInputStream(file);
        if(file.contains("data"))
            file=file.substring(file.lastIndexOf("databases")+10);
        else
            file=file.substring(file.lastIndexOf("/")+1);
        ZipEntry entry1=new ZipEntry(file);

        zipOutputStream.putNextEntry(entry1);
        // считываем содержимое файла в массив byte
        byte[] buffer = new byte[fileInputStream.available()];
        fileInputStream.read(buffer);
        // добавляем содержимое к архиву
        zipOutputStream.write(buffer);

        zipOutputStream.flush();
         // закрываем текущую запись для новой записи
        zipOutputStream.closeEntry();
     }
        zipOutputStream.finish();

    } catch (Exception e) {
        e.printStackTrace();
    }

     return zipFilename;
}

public ArrayList<MemeGroup> unzipPack(InputStream inputStream, String zipPath){
    ArrayList<MemeGroup> imported = new ArrayList<>();
        try {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry entry;
        String name;
        //  long size;
        while ((entry = zipInputStream.getNextEntry()) != null) {

            name = entry.getName(); // получим название файла
            // size=entry.getSize();  // получим его размер в байтах
            //Log.d("OLOLOG","File name:"+name+" File size: "+size);
       /*     if (getType(name) == FILE) {
//name=context.getCacheDir().getAbsolutePath();
                SQLiteDatabase importedDB = SQLiteDatabase.openOrCreateDatabase(name,null);
                Cursor cursor = importedDB.rawQuery("SELECT * FROM memesTable", null);

                if (cursor.moveToFirst()) {
                    do {
                       imported.add(new MemeGroup(cursor.getString(1),cursor.getString(2)));
                    } while (cursor.moveToNext());

                }
                }*/ //else
                    {
                // распаковка
                FileOutputStream fout = (FileOutputStream) createFile(name);
                for (int c = zipInputStream.read(); c != -1; c = zipInputStream.read()) {
                    fout.write(c);
                }

                fout.flush();
                zipInputStream.closeEntry();
                fout.close();


                        if (getType(name) == FILE) {
//name=context.getCacheDir().getAbsolutePath();
                            String path=getFullPath(name);
                            SQLiteDatabase importedDB = SQLiteDatabase.openOrCreateDatabase(path,null);
                            Cursor cursor = importedDB.rawQuery("SELECT * FROM memesTable", null);

                            if (cursor.moveToFirst()) {
                                do {
                                    imported.add(new MemeGroup(cursor.getString(1),cursor.getString(2)));
                                } while (cursor.moveToNext());

                            }
                        }
            }
        }
        }
    catch(Exception e){
            e.printStackTrace();
        }
return imported;
}

    public class innerFileHelperOld implements FileHelperInterface {


        public innerFileHelperOld(Context context) {
            root = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
            createDirs();
        }

        public OutputStream createFile(String filename)
        {
FileOutputStream outputStream=null;
            String fullpath = getFullPath(filename);

            try {
                File qwe= new File(fullpath);
                qwe.createNewFile();
                outputStream = new FileOutputStream(fullpath);


            } catch (IOException e) {
                e.printStackTrace();
            }
return outputStream;
        }
        public boolean createLocalFile(InputStream inputStream, String filename) {

            FileOutputStream outputStream = (FileOutputStream) createFile(filename);
            copyFile(inputStream, outputStream);
            return true;
        }

        public void createDirs(){

            new File (root+"/"+previews).mkdirs();
            new File (root+"/"+images).mkdirs();
            new File (root+"/"+gifs).mkdirs();
            new File (root+"/"+videos).mkdirs();
        }


        public void deleteFile(String path) {
new File(getFullPath(path)).delete();

        }

        //возвращает Битмап для создания превью
        @RequiresApi(api = Build.VERSION_CODES.R)
        public Bitmap getPreview(String filename, BitmapFactory.Options options){
            Bitmap preview=null;

            switch(getType(filename)){
                case IMAGE:
                    preview=BitmapFactory.decodeFile(getFullPath(filename),options);
                    break;
                case VIDEO:
                    preview= ThumbnailUtils.createVideoThumbnail(getFullPath(filename),MediaStore.Images.Thumbnails.MINI_KIND);
                    break;
                case GIF:
                    preview=BitmapFactory.decodeFile(getFullPath(filename),options);
                    break;
                case HTTPS:
                    preview=BitmapFactory.decodeFile(getFullPath(filename));
                    break;
            }
            if(preview==null)  preview = BitmapFactory.decodeResource(context.getResources(),R.raw.notfound);
            return preview;
        }

        public Uri getVideoUri(String filename){
Uri uri = Uri.parse(getFullPath(filename));
return uri;
        }

    }

    public class innerFileHelperNew implements  FileHelperInterface {

        public innerFileHelperNew(Context context) {

            root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public OutputStream createFile(String filename)
        { OutputStream outputStream=null;
            try {

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
                case GIF:
                    contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, gifs);
                    contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                    locuri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                    break;
                case HTTPS:
                    contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, previews);
                    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename + ".jpg");
                    locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    break;
                case FILE:
                    String folderPath = Environment.DIRECTORY_DOWNLOADS;//+File.separator + "MemesStoreExport/";
                    contentValues.put(MediaStore.DownloadColumns.RELATIVE_PATH, folderPath);
                    contentValues.put(MediaStore.DownloadColumns.DISPLAY_NAME, filename);
                    //contentValues.put(MediaStore.DownloadColumns.MIME_TYPE, "application/zip");
                    locuri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                    break;
            }

                outputStream = contentResolver.openOutputStream(locuri);

            contentResolver.update(locuri, contentValues, null, null);            }
            catch (Exception e) {
            e.printStackTrace();

        }
            return outputStream;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        public boolean createLocalFile(InputStream inputStream, String filename) {

                OutputStream outputStream=createFile(filename);
                copyFile(inputStream, outputStream);


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
                if (getType(path) == VIDEO|| getType(path) == GIF)
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
        public Bitmap getPreview(String filename, BitmapFactory.Options options) {
            Bitmap preview = null;


            String[] selectionArgs = null;
            switch (getType(filename)) {
                case IMAGE:
                    selectionArgs = new String[]{images};
                    break;
                case VIDEO:
                    selectionArgs = new String[]{videos};
                    break;
                case GIF:
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
                    preview = BitmapFactory.decodeStream(inputStream,new Rect(),options);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (getType(filename) == GIF) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getAssets().open(filename);
                    preview = BitmapFactory.decodeStream(inputStream,new Rect(),options);

                } catch (Exception e) {
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

        public Uri getVideoUri(String filename) {
            String[] selectionArgs = new String[]{Environment.DIRECTORY_MOVIES + "/" + "MemesStore2/" + "Videos/"};
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
            }
return uri;
        }
    }
}
