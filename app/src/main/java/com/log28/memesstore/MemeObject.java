package com.log28.memesstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class MemeObject {

    MemesListAdapter memesListAdapter;
    Context context;
    private String memeName;
    private String memeRelativePath, thumbnailFolder;
    private  String memeMimeType;
    private String memeTag;
    private Bitmap memeBitmap, thumbnailBitmap;
    private int memeType;
    private int memeTab;




    MemeObject(MemesListAdapter memesListAdapter,String relativeFilepath) {

        init(memesListAdapter.context,relativeFilepath,"");
    }
    MemeObject(Context context,String relativeFilepath) {
        init(context,relativeFilepath,"");
    }
    MemeObject(Context context,String relativeFilepath,String tag) {
        init(context,relativeFilepath,tag);
    }
    MemeObject(MemesListAdapter memesListAdapter,String relativeFilepath,String tag){
this.memesListAdapter=memesListAdapter;
        init(memesListAdapter.context,relativeFilepath,tag);
    }


    void init(Context context,String relativeFilepath,String tag){
        this.memeRelativePath =relativeFilepath;
        this.memeName=relativeFilepath.substring(relativeFilepath.lastIndexOf("/")+1);
        this.memeTag=tag;
        this.context=context;

       if(memesListAdapter!=null){
            BitmapLoader bitmapLoader=new BitmapLoader();
            bitmapLoader.execute(memeRelativePath);
        }
        memeType=FileClassifier.classfyByName(memeName);
        memeTab=FileClassifier.classifyByTab(memeName);
        memeMimeType=FileClassifier.getMimeType(memeName);
    }
    public String getMemeRelativePath(){
        return this.memeRelativePath;
    }
    public String getName(){return memeName;}
    public String getTag(){return memeTag;}
     public int getMemeTab(){return memeTab;}



    class BitmapLoader extends AsyncTask<String,Void, Bitmap> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            memeBitmap= BitmapFactory.decodeResource(context.getResources(), R.raw.logo);
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        @Override
        protected Bitmap doInBackground(String... strings) {
            return MemeFileHelper.createFileHelper(context, MainActivity.uriFolder).getPreview(strings[0]);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            memeBitmap=bitmap;
            memesListAdapter.notifyDataSetChanged();
        }
    }

    public Bitmap getBitmap(){

        return memeBitmap;
    }

    public Bitmap getThumbnailBitmap(){
        return thumbnailBitmap;
    }


}
