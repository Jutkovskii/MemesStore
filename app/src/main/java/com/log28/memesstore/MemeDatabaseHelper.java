package com.log28.memesstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static android.widget.Toast.LENGTH_LONG;


public class MemeDatabaseHelper extends SQLiteOpenHelper {
    String tableName = "memesTable";
    String filepathColumnName = "filepath";
    SQLiteDatabase memesDatabase;
    String name;

    public MemeDatabaseHelper(Context context, String name, int version) {

        super(context, name, null, version);
        this.name = name;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        memesDatabase = super.getWritableDatabase();
        return memesDatabase;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        memesDatabase = super.getReadableDatabase();
        return memesDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        memesDatabase = sqLiteDatabase;
        memesDatabase.execSQL("CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + filepathColumnName + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        memesDatabase = sqLiteDatabase;
    }

    public void insert(String filepath) {
        if(memesDatabase==null)
            getWritableDatabase();
        if(!memesDatabase.isOpen())
            getWritableDatabase();
        Cursor localCursor = memesDatabase.query(tableName, new String[]{filepathColumnName}, filepathColumnName + " = ?", new String[]{filepath}, null, null, null);
        if (localCursor.getCount() == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(this.filepathColumnName, filepath);
            memesDatabase.insert(tableName, null, contentValues);
        }
    }

    public void delete(Integer id) {
        memesDatabase.delete(tableName, "_id = ?", new String[]{id.toString()});
    }

    public void delete(String filepath) {
        if(memesDatabase==null)
            getWritableDatabase();
        if(!memesDatabase.isOpen())
            getWritableDatabase();
        try {
            memesDatabase.delete(tableName, filepathColumnName + " = ?", new String[]{filepath});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Cursor getCursor() {
        return memesDatabase.query(tableName, new String[]{"_id", filepathColumnName}, null, null, null, null, null);
    }


    //////////////////////////////////////////////////

    public void exportDB() {
        String DatabaseName = this.name;
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        //String currentDBPath = "/data/"+ "com.log28.memesstore" +"/databases/"+DatabaseName ;
        String currentDBPath = "/data/com.log28.memesstore/databases/" + DatabaseName;
        String backupDBPath = "/Download/database.db";
        File currentDB = new File(data, currentDBPath);
        File[] asd = new File("/data/data/com.log28.memesstore/databases/").listFiles();
        boolean s = currentDB.exists();
        File backupDB = new File(sd, backupDBPath);
        try {
            backupDB.createNewFile();
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        zip(new String[]{sd+backupDBPath}, sd + "/Download/database.meme");
    }

    public void zip(String[] files, String zipFile) {

        int BUFFER = 1024 * 4;
        String[] _files = files;
        String _zipFile = zipFile;

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(_zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.d("add:", _files[i]);
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
