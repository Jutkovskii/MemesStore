package com.log28.memesstore;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



public class MemeDatabaseHelper extends SQLiteOpenHelper {
String tableName = "memesTable";
String filepathColumnName="filepath";
    SQLiteDatabase memesDatabase;

    public MemeDatabaseHelper( Context context,  String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        memesDatabase=super.getWritableDatabase();
        return memesDatabase;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        memesDatabase=super.getReadableDatabase();
        return memesDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        memesDatabase=sqLiteDatabase;
        memesDatabase.execSQL("CREATE TABLE "+tableName+" (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+filepathColumnName+" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        memesDatabase=sqLiteDatabase;
    }

    public void insert(String filepath){
        Cursor localCursor = memesDatabase.query(tableName,new String[]{filepathColumnName},filepathColumnName+" = ?", new String[]{filepath},null,null,null);
        if(localCursor.getCount()==0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(this.filepathColumnName, filepath);
            memesDatabase.insert(tableName, null, contentValues);
        }
    }

    public void delete(Integer id){
        memesDatabase.delete(tableName,"_id = ?",new String[] {id.toString()});
    }

    public void delete(String filepath){
        try {
            memesDatabase.delete(tableName,  filepathColumnName + " = ?", new String[]{filepath});
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public Cursor getCursor(){
        return memesDatabase.query(tableName,new String[]{"_id",filepathColumnName},null,null,null,null,null);
    }
}
