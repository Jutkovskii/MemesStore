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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.TimeUtils;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
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
    public static Context context = null;
    public static String root;//корневая папка хранилища
    public static String appFolder = "MemesStore2/";//папка с данными приложения
    public static String previews = Environment.DIRECTORY_PICTURES + "/" + appFolder + "Previews/";//папка с превью ютуба
    public static String images = Environment.DIRECTORY_PICTURES + "/" + appFolder + "Images/";//папка с изображениями
    public static String videos = Environment.DIRECTORY_MOVIES + "/" + appFolder + "Videos/";//папка с видео
    public static String gifs = Environment.DIRECTORY_MOVIES + "/" + appFolder + "Gifs/";//папка с Gif
    public static String thumbnails = Environment.DIRECTORY_PICTURES + "/" + appFolder + ".thumbnails/";//папка с превьюшками
    public static String downloads= Environment.DIRECTORY_DOWNLOADS + "/";//корневая папка хранилища
    //категории файлов
    public FileHelperInterface fileHelper;
    //авто определение типа ОС и метода доступа к файлам
    public FileHelper(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            fileHelper = new innerFileHelperNew(context);
        else
            fileHelper = new innerFileHelperOld(context);
    }

    //проверка на двойной слеш
    public static String checkPath(String path) {
        String result = path;
        result = result.replace("//", "/");
        result = result.replace(".jpg.jpg", ".jpg");
        result = result.replace(".png.png", ".png");
        result = result.replace(".webp.webp.", ".webp.");
        result = result.replace(".gif.gif", ".gif");
        result = result.replace(".mp4.mp4", ".mp4");
        return result;
    }

    //получение полного пути к файлу (ЗАМЕНИТЬ!)
    public static String getFullPath(String filename) {
        String path = "";
        switch (MemeObject.classifier(filename)) {
            case MemeObject.IMAGE:
                path = root + images + filename;
                break;
            case MemeObject.VIDEO:
                path = root + videos + filename;
                break;
            case MemeObject.GIF:
                path = root + gifs + filename;
                break;
            case MemeObject.HTTPS:
                path = root + previews + filename + ".jpg";
                break;
            case MemeObject.ARCH:
                path = root + Environment.DIRECTORY_DOWNLOADS + "/" + filename;
                break;
            case MemeObject.DB:
                path = root + Environment.DIRECTORY_DOWNLOADS + "/" + filename;
                break;
            case MemeObject.TEMP:
                // path = ((context == null) ? root + Environment.DIRECTORY_DOWNLOADS : context.getExternalCacheDir().getAbsolutePath()) + "/" + filename;
                path = root + Environment.DIRECTORY_DOWNLOADS + "/" + filename;
                break;
        }
        //path = checkPath(path);

        return path;
    }
    //новая версия (а может и вовсе избавиться?)
    public static String getFullPath(MemeObject memeObject) {
        return memeObject.getPath();
    }
    //проверка существования файла
    public boolean isExist(String filename) {
        return this.fileHelper.isExist(filename);
    }

    //создание файла
    public OutputStream createFile(String filename) {

        return this.fileHelper.createFile(filename);

    }

    //удаление  файла
    public void deleteFile(String path) {
        this.fileHelper.deleteFile(path);
    }
    //копирование данных в файл
    public static void copyFile(InputStream inputStream, OutputStream outputStream) {
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

    //получение uri видеофайла
    public Uri getVideoUri(String filename) {
        return fileHelper.getVideoUri(filename);
    }

    //возвращает Битмап для создания превью(ЗАМЕНИТЬ!)
    @RequiresApi(api = Build.VERSION_CODES.R)
    public Bitmap getPreview(String filename) {

        return fileHelper.getPreview(filename, getOptions(filename));

    }
    //новая версия
    public Bitmap getPreview(MemeObject memeObject){
        return memeObject.getThumbnailBitmap();

    }
    //изменение размера слишком больших файлов для ускорения работы(ЗАМЕНИТЬ!)
    @RequiresApi(api = Build.VERSION_CODES.R)
    public BitmapFactory.Options getOptions(String filename) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (MemeObject.classifier(filename) != MemeObject.VIDEO) {
            BitmapFactory.decodeFile(getFullPath(filename), options);
            int koef = (int) ((float) (options.outWidth) / (float) (context.getDisplay().getWidth()) * 2);
            if (koef % 2 != 0) koef++;
            options.inSampleSize = koef;
        }
        options.inJustDecodeBounds = false;

        return options;

    }

    //рудимент. Использовался для борьбы со стикеризацией изображений в Телеграме(ЗАМЕНИТЬ!)
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void resizeImageForTG(String filename) {
        if (MemeObject.classifier(filename) == MemeObject.IMAGE) {
            BitmapFactory.Options options = getOptions(filename);
            float width = options.outWidth;
            float height = options.outHeight;
            final float WIDTH = 1200;
            final float HEIGHT = 1200;
            float scaleParameter = Math.max(WIDTH / width, HEIGHT / height);
            float criteria = 2;
            if (scaleParameter > criteria)/*|| scaleParameter < 1.0 / criteria) */ {
                Bitmap tempBitmap = getPreview(filename);
                tempBitmap = Bitmap.createScaledBitmap(tempBitmap, Math.round(tempBitmap.getWidth() * scaleParameter), Math.round(tempBitmap.getHeight() * scaleParameter), false);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                tempBitmap.compress(Bitmap.CompressFormat.JPEG, 100 /*ignored for PNG*/, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
                deleteFile(filename);
                // filename= filename.replaceAll(".webp",".jpg");
                FileHelper.copyFile(bs, createFile(filename));

            }
        }

    }
    //архивация указанных файлов в ZIP-архив
    public String zipPack(List<String> files) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String zipFilename = formatter.format(date) + ".zip";
        try {
            OutputStream outputStream = createFile(zipFilename);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            for (String file : files) {

                FileInputStream fileInputStream = new FileInputStream(file);
                if (MemeObject.classifier(file)==MemeObject.DB)
                    file = file.substring(file.lastIndexOf("databases") + 10);
                else
                    file = file.substring(file.lastIndexOf("/") + 1);
                ZipEntry entry1 = new ZipEntry(file);

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
    //разархивирование архива, возврат пар {имя файла; тэги}
    public ArrayList<MemeGroup> unzipPack(InputStream inputStream) {
        ArrayList<MemeGroup> imported = new ArrayList<>();
        ArrayList<String> BDs=new ArrayList<>();
        try {

            ZipInputStream zipStream = new ZipInputStream(inputStream);
            ZipEntry zEntry = null;
            String name;
            while ((zEntry = zipStream.getNextEntry()) != null) {

                {
                    name = zEntry.getName();

                    FileOutputStream fout = (FileOutputStream) createFile(name);
                    //FileOutputStream fout = new FileOutputStream(zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = zipStream.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }

                    zipStream.closeEntry();
                    bufout.close();
                    fout.close();
                }
                if (MemeObject.classifier(name) == MemeObject.DB)
                    BDs.add(name);

            }
            zipStream.close();
            for(String filename: BDs)
            {

                String path = getFullPath(filename);
                SQLiteDatabase importedDB = SQLiteDatabase.openOrCreateDatabase(path, null);
                Cursor cursor = importedDB.rawQuery("SELECT * FROM memesTable", null);

                if (cursor.moveToFirst()) {
                    do {
                        imported.add(new MemeGroup(cursor.getString(1), cursor.getString(2)));
                    } while (cursor.moveToNext());

                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return imported;
    }

    //подклассы, нужный избирается в зависимости от версии ОС
    public class innerFileHelperOld implements FileHelperInterface {


        public innerFileHelperOld(Context context) {
            root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            new File(root + "/" + previews).mkdirs();
            new File(root + "/" + images).mkdirs();
            new File(root + "/" + gifs).mkdirs();
            new File(root + "/" + videos).mkdirs();
        }

        public OutputStream createFile(String filename) {
            FileOutputStream outputStream = null;
            String fullpath = getFullPath(filename);

            try {
                File qwe = new File(fullpath);
                qwe.createNewFile();
                outputStream = new FileOutputStream(fullpath);


            } catch (IOException e) {
                e.printStackTrace();
            }
            return outputStream;
        }

        @Override
        public boolean isExist(String filename) {
            return new File(getFullPath(filename)).exists();
        }

        public void deleteFile(String path) {
            new File(getFullPath(path)).delete();

        }

        public Uri getVideoUri(String filename) {
            Uri uri = Uri.parse(getFullPath(filename));
            return uri;
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        public Bitmap getPreview(String filename, BitmapFactory.Options options) {
            Bitmap preview = null;

            switch (MemeObject.classifier(filename)) {
                case MemeObject.IMAGE:
                    preview = BitmapFactory.decodeFile(getFullPath(filename), options);
                    break;
                case MemeObject.VIDEO:
                    preview = ThumbnailUtils.createVideoThumbnail(getFullPath(filename), MediaStore.Images.Thumbnails.MINI_KIND);
                    break;
                case MemeObject.GIF:
                    preview = BitmapFactory.decodeFile(getFullPath(filename), options);
                    break;
                case MemeObject.HTTPS:
                    preview = BitmapFactory.decodeFile(getFullPath(filename));
                    break;
            }
            if (preview == null)
                preview = BitmapFactory.decodeResource(context.getResources(), R.raw.notfound);
            return preview;
        }


    }

    public class innerFileHelperNew implements FileHelperInterface {

        public innerFileHelperNew(Context context) {

            root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public OutputStream createFile(String filename) {
            OutputStream outputStream = null;
            try {
                if(fileHelper.isExist(filename))
                    fileHelper.deleteFile(filename);
                ContentValues contentValues = new ContentValues();
                ContentResolver contentResolver = context.getContentResolver();
                Uri locuri = null;
                //deleteFile(filename);
                switch (MemeObject.classifier(filename)) {
                    case MemeObject.IMAGE:
                        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, images);
                        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                        locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        break;
                    case MemeObject.VIDEO:
                        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, videos);
                        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                        locuri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                        break;
                    case MemeObject.GIF:
                        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, gifs);
                        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
                        locuri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
                        break;
                    case MemeObject.HTTPS:
                        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, previews);
                        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, filename + ".jpg");
                        locuri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                        break;
                    case MemeObject.ARCH:
                        String folderPath = Environment.DIRECTORY_DOWNLOADS;//+File.separator + "MemesStoreExport/";
                        contentValues.put(MediaStore.DownloadColumns.RELATIVE_PATH, folderPath);
                        contentValues.put(MediaStore.DownloadColumns.DISPLAY_NAME, filename);
                    case MemeObject.DB:
                        String dbfolderPath = Environment.DIRECTORY_DOWNLOADS;//+File.separator + "MemesStoreExport/";
                        contentValues.put(MediaStore.DownloadColumns.RELATIVE_PATH, dbfolderPath);
                        contentValues.put(MediaStore.DownloadColumns.DISPLAY_NAME, filename);
                        //contentValues.put(MediaStore.DownloadColumns.MIME_TYPE, "application/zip");
                        // if(contentResolver.query(MediaStore.Downloads.getContentUri("external"),null,MediaStore.DownloadColumns.RELATIVE_PATH + "=?",new String[]{Environment.DIRECTORY_DOWNLOADS + filename},null).getCount()!=0)
                        locuri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);


                        break;
                }



                outputStream = contentResolver.openOutputStream(locuri);
                contentResolver.update(locuri, contentValues, null, null);
            } catch (Exception e) {
                e.printStackTrace();

            }
            return outputStream;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public boolean isExist(String filename) {
            String path=getFullPath(filename);
            ContentResolver contentResolver = context.getContentResolver();
            Uri locuri = null;
            String[] selectionArgs = null;// new String[]{"+"+filename+"/"};
            if (MemeObject.classifier(filename) == MemeObject.IMAGE|| MemeObject.classifier(filename) == MemeObject.GIF) {
                locuri = MediaStore.Images.Media.getContentUri("external");
                selectionArgs = new String[]{images};
            }
            if (MemeObject.classifier(filename) == MemeObject.HTTPS){
                locuri = MediaStore.Images.Media.getContentUri("external");
                path = path+ ".jpg";
                selectionArgs = new String[]{previews};
            }

            if (MemeObject.classifier(filename) == MemeObject.VIDEO ) {
                locuri = MediaStore.Video.Media.getContentUri("external");
                selectionArgs = new String[]{videos};
            }
            if (MemeObject.classifier(filename)== MemeObject.ARCH) {
                locuri = MediaStore.Downloads.getContentUri("external");
                selectionArgs = new String[]{downloads};
            }
            if (MemeObject.classifier(filename)== MemeObject.DB) {
                locuri = MediaStore.Downloads.getContentUri("external");
                selectionArgs = new String[]{downloads};
            }
            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            //must include "/" in front and end
            Cursor cursor = contentResolver.query(locuri, null, selection, selectionArgs, null);
            if (cursor.getCount() == 0) return false;
            return  true;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public void deleteFile(String path) {
            try {
                ContentResolver contentResolver = context.getContentResolver();
                Uri locuri = null;
                if (MemeObject.classifier(path) == MemeObject.IMAGE || MemeObject.classifier(path) == MemeObject.HTTPS)
                    locuri = MediaStore.Images.Media.getContentUri("external");
                if (MemeObject.classifier(path)  == MemeObject.VIDEO || MemeObject.classifier(path) == MemeObject.GIF)
                    locuri = MediaStore.Video.Media.getContentUri("external");
                if (MemeObject.classifier(path) == MemeObject.ARCH||MemeObject.classifier(path) == MemeObject.DB)
                    locuri = MediaStore.Downloads.getContentUri("external");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentResolver.delete(locuri, MediaStore.MediaColumns.DATA + "=?", new String[]{getFullPath(path)});
                }
            } catch (Exception e) {
                e.printStackTrace();
                int y = 0;
            }

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
        //возвращает Битмап для создания превью
        @RequiresApi(api = Build.VERSION_CODES.R)
        public Bitmap getPreview(String filename, BitmapFactory.Options options) {
            Bitmap preview = null;


            String[] selectionArgs = null;
            switch (MemeObject.classifier(filename)) {
                case MemeObject.IMAGE:
                    selectionArgs = new String[]{images};
                    break;
                case MemeObject.VIDEO:
                    selectionArgs = new String[]{videos};
                    break;
                case MemeObject.GIF:
                    selectionArgs = new String[]{videos};
                    break;
                case MemeObject.HTTPS:
                    filename = filename + ".jpg";
                    selectionArgs = new String[]{previews};
                    break;
            }

            Uri contentUri = MediaStore.Files.getContentUri("external");
            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
            Cursor cursor = context.getContentResolver().query(contentUri, null, selection, selectionArgs, null);
            cursor.moveToFirst();

            Uri uri = null;
            do {
                String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                if (fileName.equals(filename)) {
                    long id = cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
                    uri = ContentUris.withAppendedId(contentUri, id);
                    break;
                }
            } while (cursor.moveToNext());


            if (MemeObject.classifier(filename) == MemeObject.IMAGE) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getContentResolver().openInputStream(uri);
                    preview = BitmapFactory.decodeStream(inputStream, new Rect(), options);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            if (MemeObject.classifier(filename) == MemeObject.GIF) {
                InputStream inputStream = null;
                try {
                    inputStream = context.getAssets().open(filename);
                    preview = BitmapFactory.decodeStream(inputStream, new Rect(), options);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (MemeObject.classifier(filename) == MemeObject.VIDEO || MemeObject.classifier(filename) == MemeObject.HTTPS) {
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
