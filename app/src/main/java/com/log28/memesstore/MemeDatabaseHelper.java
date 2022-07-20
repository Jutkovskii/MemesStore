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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static android.widget.Toast.LENGTH_LONG;


public class MemeDatabaseHelper extends SQLiteOpenHelper {
    String tableName = "memesTable";
    String filepathColumnName = "filepath";
    String filetagColumnName = "filetag";
    SQLiteDatabase memesDatabase;
    String name;

    public MemeDatabaseHelper(Context context, String name, int version) {

        super(context, name, null, version);
        this.name = name;
        getWritableDatabase();
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
        memesDatabase.execSQL("CREATE TABLE " + tableName + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + filepathColumnName + " TEXT, " + filetagColumnName + " TEXT );");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        memesDatabase = sqLiteDatabase;
    }

    public void insert(String filepath, String  filetag) {
        checkDB();
        Cursor localCursor = memesDatabase.query(tableName, new String[]{filepathColumnName},  filepathColumnName+" =?",new String[]{filepath}, null, null, null);
        if (localCursor.getCount() == 0)
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(this.filepathColumnName, filepath);
            contentValues.put(this.filetagColumnName, filetag);
            memesDatabase.insert(tableName, null, contentValues);
        }
    }
    public void insert(String filepath) {
        insert(filepath,"");
    }

    public void delete(Integer id) {
        memesDatabase.delete(tableName, "_id = ?", new String[]{id.toString()});
    }

    public void delete(String filepath) {
        try {checkDB();
            memesDatabase.delete(tableName, filepathColumnName + " = ?", new String[]{filepath});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(String filename, String filetag){
        ContentValues contentValues = new ContentValues();
        contentValues.put(this.filetagColumnName, filetag);
        memesDatabase.update(tableName,contentValues,filepathColumnName + " = ?",new String[]{filename});
    }

    public Cursor getCursor() {
        checkDB();
        return memesDatabase.query(tableName, new String[]{"_id", filepathColumnName, filetagColumnName}, null, null, null, null, null);
    }

    void checkDB(){
        if(memesDatabase==null)
            getWritableDatabase();
        if(!memesDatabase.isOpen())
            getWritableDatabase();
    }

    //////////////////////////////////////////////////

    public String getDbPath()
    {
        String DatabaseName = this.name;
        String currentDBPath = "/data/data/com.log28.memesstore/databases/" + DatabaseName;
        return currentDBPath;
    }

}
